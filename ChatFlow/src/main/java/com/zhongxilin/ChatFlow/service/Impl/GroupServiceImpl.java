package com.zhongxilin.ChatFlow.service.Impl;

import com.zhongxilin.ChatFlow.mapper.GroupMapper;
import com.zhongxilin.ChatFlow.pojo.*;
import com.zhongxilin.ChatFlow.service.GroupService;
import com.zhongxilin.ChatFlow.utils.UserCodeGenerate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;


    @Override
    public List<Group> getGroupList(Integer userId, String groupName) {
        return groupMapper.selectGroupsByIdAndName(userId, groupName);
    }

    @Override
    public void createGroup(Group group) {
        //1.在cf_group表中插入一条新群组记录
        group.setGroupCode(UserCodeGenerate.generateUserCode("GRP", 6));
        //owner_id即为当前用户id，前端传入

        groupMapper.insertGroup(group);

        //2.在cf_group_member表中插入一条新群组成员记录，设置当前用户为群主
        //根据groupCode查询新群组的id
        Integer groupId = groupMapper.selectGroupIdByCode(group.getGroupCode());

        groupMapper.insertGroupMember(groupId, group.getOwnerId(), 1);

    }

    @Override
    public void editGroupProfile(Integer groupId, Group group) {

        groupMapper.updateGroupProfile(groupId, group);
    }

    @Override
    public Group getGroupProfile(Integer groupId) {
        return groupMapper.selectGroupProfile(groupId);
    }

    @Override
    public GroupJoinResult joinGroup(GroupRequestData groupRequestData) {
        if (groupRequestData == null || groupRequestData.getGroupCode() == null
                || groupRequestData.getGroupCode().isBlank()
                || groupRequestData.getFromUserId() == null
                || groupRequestData.getFromUserId() <= 0) {
            throw new IllegalArgumentException("群号或用户ID无效");
        }

        String groupCode = groupRequestData.getGroupCode();
        Long userId = groupRequestData.getFromUserId();
        Integer groupId = groupMapper.selectGroupIdByCode(groupCode);
        if (groupId == null) {
            throw new IllegalArgumentException("群组不存在");
        }

        Integer joinMode = groupMapper.getJoinModeByGroupCode(groupCode);
        if (joinMode == null || (!Integer.valueOf(0).equals(joinMode)
                && !Integer.valueOf(1).equals(joinMode))) {
            throw new IllegalArgumentException("群加入模式无效");
        }

        // 已是成员时按幂等成功处理，避免重复插入成员记录。
        if (groupMapper.countActiveGroupMember(groupId, userId) > 0) {
            return GroupJoinResult.JOINED;
        }

        if (Integer.valueOf(0).equals(joinMode)) {
            groupMapper.insertGroupMember(groupId, userId, 0);
            return GroupJoinResult.JOINED;
        }

        List<Map<String, Object>> existingRequests =
                groupMapper.findGroupRequest(groupCode, userId);
        if (existingRequests != null) {
            for (Map<String, Object> request : existingRequests) {
                Object status = request.get("status");
                if (status instanceof Number
                        && (((Number) status).intValue() == 0 || ((Number) status).intValue() == 1)) {
                    return GroupJoinResult.DUPLICATE_REQUEST;
                }
            }

            for (Map<String, Object> request : existingRequests) {
                Object status = request.get("status");
                if (status instanceof Number && ((Number) status).intValue() == 2) {
                    groupMapper.updateGroupRequest(groupCode, userId);
                    return GroupJoinResult.REQUEST_SUBMITTED;
                }
                if (!(status instanceof Number)) {
                    throw new IllegalStateException("入群申请状态无效");
                }
                int statusValue = ((Number) status).intValue();
                if (statusValue != 0 && statusValue != 1 && statusValue != 2) {
                    throw new IllegalStateException("入群申请状态无效");
                }
            }
        }

        groupMapper.insertGroupRequest(groupId, userId);
        return GroupJoinResult.REQUEST_SUBMITTED;
    }

    @Override
    public List<GroupRequestFeedback> groupRequestReceivedList(Integer userId) {
        validateRequestListUserId(userId);
        return groupMapper.selectReceivedGroupRequests(userId.longValue());
    }

    @Override
    public List<GroupRequestFeedback> groupRequestSentList(Integer userId) {
        validateRequestListUserId(userId);
        return groupMapper.selectSentGroupRequests(userId.longValue());
    }

    @Override
    @Transactional
    public void acceptGroupRequest(Integer groupId, Integer userId) {
        validateGroupRequestIds(groupId, userId);
        int updatedRows = groupMapper.updatePendingGroupRequestStatus(groupId, userId.longValue(), 1);
        if (updatedRows != 1) {
            throw new IllegalArgumentException("未找到待处理的入群申请");
        }

        if (groupMapper.countActiveGroupMember(groupId, userId.longValue()) == 0) {
            groupMapper.insertGroupMember(groupId, userId.longValue(), 0);
        }
    }

    @Override
    @Transactional
    public void rejectGroupRequest(Integer groupId, Integer userId) {
        validateGroupRequestIds(groupId, userId);
        int updatedRows = groupMapper.updatePendingGroupRequestStatus(groupId, userId.longValue(), 2);
        if (updatedRows != 1) {
            throw new IllegalArgumentException("未找到待处理的入群申请");
        }
    }

    @Override
    public List<GroupMember> getGroupMembersList(Integer groupId, String username) {

        return groupMapper.selectGroupMembers(groupId, username);
    }

    private void validateGroupRequestIds(Integer groupId, Integer userId) {
        if (groupId == null || groupId <= 0 || userId == null || userId <= 0) {
            throw new IllegalArgumentException("群组ID或用户ID无效");
        }
    }

    private void validateRequestListUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
    }
}

package com.zhongxilin.ChatFlow.service;

import com.zhongxilin.ChatFlow.pojo.*;

import java.util.List;

public interface GroupService {
    List<Group> getGroupList(Integer userId, String groupName);

    void createGroup(Group group);

    void editGroupProfile(Integer groupId, Group group);

    Group getGroupProfile(Integer groupId);

    GroupJoinResult joinGroup(GroupRequestData groupRequestData);

    List<GroupRequestFeedback> groupRequestReceivedList(Integer userId);

    List<GroupRequestFeedback> groupRequestSentList(Integer userId);

    void acceptGroupRequest(Integer groupId, Integer userId);

    void rejectGroupRequest(Integer groupId, Integer userId);

    List<GroupMember> getGroupMembersList(Integer groupId, String username);
}

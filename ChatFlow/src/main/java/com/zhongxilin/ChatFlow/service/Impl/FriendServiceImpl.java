package com.zhongxilin.ChatFlow.service.Impl;

import com.zhongxilin.ChatFlow.mapper.FriendMapper;
import com.zhongxilin.ChatFlow.pojo.FriendReqestFeedback;
import com.zhongxilin.ChatFlow.pojo.FriendRequestData;
import com.zhongxilin.ChatFlow.pojo.User;
import com.zhongxilin.ChatFlow.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendMapper friendMapper;


    @Override
    public List<Map<String, Object>> sendRequest(FriendRequestData friendRequestData) {
        // 1.检查是否已经发送过好友请求
        List<Map<String, Object>> existingRequest = friendMapper.findFriendRequest(friendRequestData.getFromUserId(), friendRequestData.getToUserCode());
        //在已发送的请求中检查状态，如果状态为0（待处理）或1（已接受），则不允许重复发送请求；如果状态为2（已拒绝），则允许重新发送请求
        if (existingRequest != null && !existingRequest.isEmpty()) {

            Object status = existingRequest.get(0).get("status");// 获取status
            if (status != null) {
                int statusValue = ((Number) status).intValue();// 将status转换为int类型

                if (statusValue == 0) return existingRequest;
                if (statusValue == 1) {
                    // 已同意的记录仅在好友关系仍有效时阻止再次申请。
                    if (friendMapper.hasActiveFriendship(friendRequestData.getFromUserId(), friendRequestData.getToUserCode()) > 0) return existingRequest;
                    friendMapper.resetAcceptedFriendRequest(friendRequestData.getFromUserId(), friendRequestData.getToUserCode());
                    return null;
                }
                // 如果状态为2（已拒绝），则允许重新发送请求
                if (statusValue == 2) {
                    friendMapper.updateFriednRequest(friendRequestData.getFromUserId(), friendRequestData.getToUserCode());
                    return null;
                }
            }
            return existingRequest;
        }

        // 2.插入新的好友请求
        friendMapper.insertFriendRequest(friendRequestData);
        return null; // 返回null表示成功发送请求
    }

    @Override
    public List<FriendReqestFeedback> listRequests(Integer fromUserId) {
        return friendMapper.selectSentRequestsByUserId(fromUserId);
    }

    @Override
    public List<FriendReqestFeedback> listReceivedRequests(Integer toUserId) {
        return friendMapper.selectReceivedRequestsByUserId(toUserId);
    }

    @Override
    public void acceptRequest(Integer fromUserId, Integer toUserId) {
        // 1.更新好友请求状态为已接受（1）
        friendMapper.updateFriendRequestStatus(fromUserId, toUserId,1);
        // 2.在cf_friendship表中插入双向好友关系
        // 2.1 插入 fromUserId -> toUserId 的关系
        friendMapper.insertFriendship(fromUserId, toUserId);
        // 2.2 插入 toUserId -> fromUserId 的关系
        friendMapper.insertFriendship(toUserId, fromUserId);


    }

    @Override
    public void rejectRequest(Integer fromUserId, Integer toUserId) {
        // 1.更新好友请求状态为已拒绝（2）
        friendMapper.updateFriendRequestStatus(fromUserId, toUserId,2);
    }

    @Override
    public List<User> getFriendsList(Integer id, String username) {
        return friendMapper.selectFriendsByIdAndFriendName(id, username);
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        friendMapper.deleteFriendBy2Id(userId, friendId);
        friendMapper.deleteFriendBy2Id(friendId, userId);
        friendMapper.markFriendRequestsAsRejected(userId, friendId);
    }
}

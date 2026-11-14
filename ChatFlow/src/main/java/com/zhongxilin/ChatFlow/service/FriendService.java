package com.zhongxilin.ChatFlow.service;

import com.zhongxilin.ChatFlow.pojo.FriendReqestFeedback;
import com.zhongxilin.ChatFlow.pojo.FriendRequestData;
import com.zhongxilin.ChatFlow.pojo.User;

import java.util.List;
import java.util.Map;

public interface FriendService {

    List<Map<String, Object>> sendRequest(FriendRequestData friendRequestData);

    List<FriendReqestFeedback> listRequests(Integer fromUserId);

    List<FriendReqestFeedback> listReceivedRequests(Integer toUserId);

    void acceptRequest(Integer fromUserId, Integer toUserId);

    void rejectRequest(Integer fromUserId, Integer toUserId);

    List<User> getFriendsList(Integer id, String username);

    void deleteFriend(Integer userId, Integer friendId);
}

package com.zhongxilin.ChatFlow.mapper;

import com.zhongxilin.ChatFlow.pojo.FriendReqestFeedback;
import com.zhongxilin.ChatFlow.pojo.FriendRequestData;
import com.zhongxilin.ChatFlow.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface FriendMapper {

    // 根据申请人ID和接收方用户编码查询对应的好友请求
    @Select("select from_user_id,to_user_id,status from cf_friend_request " +
            "where from_user_id=#{fromUserId} and to_user_id=(select id from cf_user where user_code=#{toUserCode})")
    List<Map<String, Object>> findFriendRequest(String fromUserId, String toUserCode);

    // 将指定的已拒绝好友请求状态重置为待处理（0）
    @Update("update cf_friend_request set status=0 " +
            "where from_user_id=#{fromUserId} " +
            "and to_user_id=(select id from cf_user where user_code=#{toUserCode}) " +
            "and status=2")
    void updateFriednRequest(@Param("fromUserId") String fromUserId,
                             @Param("toUserCode") String toUserCode);

    // 向好友请求表插入一条状态为待处理（0）的好友请求
    @Insert("insert into cf_friend_request(from_user_id, to_user_id, status)" +
            "values (#{fromUserId}, (select id from cf_user where user_code = #{toUserCode}), 0)")
    void insertFriendRequest(FriendRequestData friendRequestData);

    // 根据申请人ID查询其发出的所有好友请求
    @Select("select from_user_id,to_user_id,status,created_at,handled_at from cf_friend_request where from_user_id=#{fromUserId}")
    List<FriendReqestFeedback> selectSentRequestsByUserId(Integer fromUserId);

    // 根据接收人ID查询其收到的所有好友请求
    @Select("select from_user_id,to_user_id,status,created_at,handled_at from cf_friend_request where to_user_id=#{toUserId}")
    List<FriendReqestFeedback> selectReceivedRequestsByUserId(Integer toUserId);

    // 将指定好友请求的状态更新为已接受（1）
    @Update("update cf_friend_request set status=#{status} where from_user_id=#{fromUserId} and to_user_id=#{toUserId}")
    void updateFriendRequestStatus(@Param("fromUserId") Integer fromUserId, @Param("toUserId") Integer toUserId,@Param("status") Integer status);

    // 向好友关系表插入一条指定方向的好友关系记录
    @Insert("insert into cf_friendship(user_id, friend_id) values (#{fromUserId}, #{toUserId})")
    void insertFriendship(@Param("fromUserId") Integer fromUserId, @Param("toUserId") Integer toUserId);

    // 根据用户ID和可选用户名关键字查询好友列表
    List<User> selectFriendsByIdAndFriendName(@Param("id") Integer id, @Param("username") String username);

    // 将指定好友关系的状态更新为0（逻辑删除）
    @Update("update cf_friendship set status=0 where user_id=#{userId} and friend_id=#{friendId}")
    void deleteFriendBy2Id(@Param("userId") Integer userId, @Param("friendId") Integer friendId);
}

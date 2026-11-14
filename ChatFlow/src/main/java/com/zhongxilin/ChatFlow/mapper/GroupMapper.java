package com.zhongxilin.ChatFlow.mapper;

import com.zhongxilin.ChatFlow.pojo.Group;
import com.zhongxilin.ChatFlow.pojo.GroupMember;
import com.zhongxilin.ChatFlow.pojo.GroupRequestFeedback;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface GroupMapper {

    // 根据用户ID和可选群组名关键字查询群组列表
    List<Group> selectGroupsByIdAndName(Integer userId, String groupName);

    // 插入新群组记录
    @Insert("INSERT INTO cf_group (group_code, group_name, owner_id) VALUES (#{groupCode}, #{groupName}, #{ownerId})")
    void insertGroup(Group group);

    // 根据群组代码查询群组ID
    @Select("SELECT id FROM cf_group WHERE group_code = #{groupCode}")
    Integer selectGroupIdByCode(String groupCode);

    // 插入新群组成员记录
    @Insert("INSERT INTO cf_group_member (group_id, user_id, role) VALUES (#{groupId}, #{userId}, #{role})")
    void insertGroupMember(@Param("groupId") Integer groupId,
                           @Param("userId") Long userId,
                           @Param("role") Integer role);

    // 查询用户是否已在群中
    @Select("SELECT COUNT(*) FROM cf_group_member WHERE group_id = #{groupId} AND user_id = #{userId} AND status = 1")
    int countActiveGroupMember(@Param("groupId") Integer groupId, @Param("userId") Long userId);

    // 更新群主页资料
    @Update("UPDATE cf_group SET avatar = #{group.avatar}, description = #{group.description}, join_mode = #{group.joinMode} WHERE id = #{groupId}")
    void updateGroupProfile(@Param("groupId") Integer groupId, @Param("group") Group group);

    // 根据群组ID查询群主页资料
    @Select("SELECT * FROM cf_group WHERE id = #{groupId}")
    Group selectGroupProfile(@Param("groupId") Integer groupId);
// 根据群组代码查询入群模式
    @Select("SELECT join_mode FROM cf_group WHERE group_code = #{groupCode}")
    Integer getJoinModeByGroupCode(String groupCode);

    // 根据群组代码和申请人ID查询对应的入群请求
    @Select("select group_id,user_id,status from cf_group_join_request " +
            "where user_id=#{fromUserId} and " +
            "group_id=(select id from cf_group where group_code=#{groupCode}) " +
            "order by id desc")
    List<Map<String, Object>> findGroupRequest(@Param("groupCode") String groupCode,
                                               @Param("fromUserId") Long fromUserId);

    // 将指定的已拒绝（2）入群请求状态重置为待处理（0）
    @Update("update cf_group_join_request set status=0 " +
            "where user_id=#{fromUserId} " +
            "and group_id=(select id from cf_group where group_code=#{groupCode}) " +
            "and status=2 order by id desc limit 1")
    void updateGroupRequest(@Param("groupCode") String groupCode,
                            @Param("fromUserId") Long fromUserId);

    // 新增待审核的入群申请
    @Insert("INSERT INTO cf_group_join_request (group_id, user_id, status) VALUES (#{groupId}, #{userId}, 0)")
    void insertGroupRequest(@Param("groupId") Integer groupId, @Param("userId") Long userId);

    // 仅更新待处理的入群申请，并返回受影响行数
    @Update("UPDATE cf_group_join_request SET status = #{status} " +
            "WHERE group_id = #{groupId} AND user_id = #{userId} AND status = 0")
    int updatePendingGroupRequestStatus(@Param("groupId") Integer groupId,
                                        @Param("userId") Long userId,
                                        @Param("status") Integer status);

    // 查询当前用户作为群主收到的待处理入群申请
    @Select("SELECT r.id, r.group_id, g.group_code, g.group_name, g.avatar AS group_avatar, " +
            "r.user_id, u.username, u.avatar AS user_avatar, r.status, r.created_at " +
            "FROM cf_group_join_request r " +
            "JOIN cf_group g ON g.id = r.group_id " +
            "JOIN cf_user u ON u.id = r.user_id " +
            "WHERE g.owner_id = #{userId} AND r.status = 0 " +
            "ORDER BY r.created_at DESC, r.id DESC")
    List<GroupRequestFeedback> selectReceivedGroupRequests(@Param("userId") Long userId);

    // 查询当前用户发出的待处理入群申请
    @Select("SELECT r.id, r.group_id, g.group_code, g.group_name, g.avatar AS group_avatar, " +
            "r.user_id, u.username, u.avatar AS user_avatar, r.status, r.created_at " +
            "FROM cf_group_join_request r " +
            "JOIN cf_group g ON g.id = r.group_id " +
            "JOIN cf_user u ON u.id = r.user_id " +
            "WHERE r.user_id = #{userId} AND r.status = 0 " +
            "ORDER BY r.created_at DESC, r.id DESC")
    List<GroupRequestFeedback> selectSentGroupRequests(@Param("userId") Long userId);

    // 查询群内有效成员，可按用户名模糊查询
    List<GroupMember> selectGroupMembers(@Param("groupId") Integer groupId,
                                         @Param("username") String username);

    // 群主移除普通成员
    @Update("UPDATE cf_group_member SET status = 0, leave_time = NOW(), leave_type = 2 " +
            "WHERE group_id = #{groupId} AND user_id = #{userId} AND status = 1 AND role = 0 " +
            "AND EXISTS (SELECT 1 FROM cf_group g WHERE g.id = #{groupId} " +
            "AND g.owner_id = #{ownerId} AND g.status = 1)")
    int removeGroupMember(@Param("groupId") Integer groupId,
                          @Param("userId") Integer userId,
                          @Param("ownerId") Integer ownerId);

    // 普通成员主动退出群
    @Update("UPDATE cf_group_member SET status = 0, leave_time = NOW(), leave_type = 1 " +
            "WHERE group_id = #{groupId} AND user_id = #{userId} AND status = 1 AND role = 0 " +
            "AND EXISTS (SELECT 1 FROM cf_group g WHERE g.id = #{groupId} AND g.status = 1)")
    int quitGroup(@Param("groupId") Integer groupId, @Param("userId") Integer userId);

    // 群主解散群
    @Update("UPDATE cf_group SET status = 0, dissolved_at = NOW() " +
            "WHERE id = #{groupId} AND owner_id = #{ownerId} AND status = 1")
    int disbandGroup(@Param("groupId") Integer groupId, @Param("ownerId") Integer ownerId);

    // 将解散群组的所有有效成员标记为离开
    @Update("UPDATE cf_group_member SET status = 0, leave_time = NOW(), leave_type = 3 " +
            "WHERE group_id = #{groupId} AND status = 1")
    int markGroupMembersDisbanded(@Param("groupId") Integer groupId);
}

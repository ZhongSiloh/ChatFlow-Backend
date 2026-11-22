package com.zhongxilin.ChatFlow.chat.mapper;

import com.zhongxilin.ChatFlow.chat.dto.ChatMessageView;
import com.zhongxilin.ChatFlow.chat.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    /** 保存一条消息；成功插入后将自增主键回填到 message.id。 */
    @Insert("INSERT INTO cf_chat_message(conversation_type, conversation_id, sender_id, recipient_id, content) " +
            "VALUES(#{conversationType}, #{conversationId}, #{senderId}, #{recipientId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessageEntity message);

    /** 按 ID 查询已保存消息，并联查发送者资料，供实时广播构建完整消息视图。 */
    @Select("SELECT m.id, m.conversation_type AS conversationType, m.conversation_id AS conversationId, " +
            "m.sender_id AS senderId, u.username AS senderName, u.avatar AS senderAvatar, " +
            "m.recipient_id AS recipientId, m.content, m.created_at AS createdAt " +
            "FROM cf_chat_message m JOIN cf_user u ON u.id=m.sender_id WHERE m.id=#{id}")
    ChatMessageView findById(@Param("id") long id);

    /** 查询好友双方最近 50 条私聊；beforeId 提供向旧消息翻页的游标。 */
    @Select("SELECT m.id, m.conversation_type AS conversationType, m.conversation_id AS conversationId, " +
            "m.sender_id AS senderId, u.username AS senderName, u.avatar AS senderAvatar, " +
            "m.recipient_id AS recipientId, m.content, m.created_at AS createdAt " +
            "FROM cf_chat_message m JOIN cf_user u ON u.id=m.sender_id " +
            "WHERE m.conversation_type=1 AND ((m.sender_id=#{userId} AND m.recipient_id=#{friendId}) " +
            "OR (m.sender_id=#{friendId} AND m.recipient_id=#{userId})) " +
            "AND (#{beforeId} IS NULL OR m.id < #{beforeId}) ORDER BY m.id DESC LIMIT 50")
    List<ChatMessageView> findPrivateHistory(@Param("userId") long userId,
                                             @Param("friendId") long friendId,
                                             @Param("beforeId") Long beforeId);

    /** 查询指定群最近 50 条消息；结果为倒序，由服务层转换成时间正序。 */
    @Select("SELECT m.id, m.conversation_type AS conversationType, m.conversation_id AS conversationId, " +
            "m.sender_id AS senderId, u.username AS senderName, u.avatar AS senderAvatar, " +
            "m.recipient_id AS recipientId, m.content, m.created_at AS createdAt " +
            "FROM cf_chat_message m JOIN cf_user u ON u.id=m.sender_id " +
            "WHERE m.conversation_type=2 AND m.conversation_id=#{groupId} " +
            "AND (#{beforeId} IS NULL OR m.id < #{beforeId}) ORDER BY m.id DESC LIMIT 50")
    List<ChatMessageView> findGroupHistory(@Param("groupId") long groupId,
                                           @Param("beforeId") Long beforeId);
}

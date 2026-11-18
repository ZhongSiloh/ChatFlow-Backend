package com.zhongxilin.ChatFlow.ai.mapper;

import com.zhongxilin.ChatFlow.ai.dto.MessageView;
import com.zhongxilin.ChatFlow.ai.dto.SessionView;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiChatMapper {

    // 为用户创建一条会话记录
    @Insert("""
        INSERT INTO cf_ai_chat_session(user_id, title)
        VALUES(#{userId}, #{title})
        """)
    // 将数据库生成的主键写回 SessionRow.id
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSession(SessionRow row);


    // 只列出指定用户自己的会话，并按最近更新时间倒序排列
    @Select("""
        SELECT id, title, created_at AS createdAt, updated_at AS updatedAt
        FROM cf_ai_chat_session
        WHERE user_id = #{userId}
        ORDER BY updated_at DESC
        """)
    List<SessionView> listSessions(@Param("userId") Integer userId);


    // 通过会话 ID 和用户 ID 同时校验会话归属
    @Select("""
        SELECT id, title, created_at AS createdAt, updated_at AS updatedAt
        FROM cf_ai_chat_session
        WHERE id = #{sessionId} AND user_id = #{userId}
        """)
    SessionView findSession(@Param("sessionId") Long sessionId,
                            @Param("userId") Integer userId);

    // 只查询属于该用户、该会话的消息，并按消息 ID 顺序返回
    @Select("""
        SELECT id, role, content, created_at AS createdAt
        FROM cf_ai_chat_message
        WHERE session_id = #{sessionId} AND user_id = #{userId}
        ORDER BY id ASC
        """)
    List<MessageView> listMessages(@Param("sessionId") Long sessionId,
                                   @Param("userId") Integer userId);

    // 保存一条用户消息或 AI 回答
    @Insert("""
        INSERT INTO cf_ai_chat_message(session_id, user_id, role, content)
        VALUES(#{sessionId}, #{userId}, #{role}, #{content})
        """)
    int insertMessage(@Param("sessionId") Long sessionId,
                      @Param("userId") Integer userId,
                      @Param("role") String role,
                      @Param("content") String content);

//    更新会话的最近活动时间，且限定只能更新所属用户的会话
    @Update("""
        UPDATE cf_ai_chat_session
        SET updated_at = CURRENT_TIMESTAMP
        WHERE id = #{sessionId} AND user_id = #{userId}
        """)
    int touchSession(@Param("sessionId") Long sessionId,
                     @Param("userId") Integer userId);

//    更新会话标题，并再次校验会话归属
    @Update("""
        UPDATE cf_ai_chat_session
        SET title = #{title}, updated_at = CURRENT_TIMESTAMP
        WHERE id = #{sessionId} AND user_id = #{userId}
        """)
    int updateTitle(@Param("sessionId") Long sessionId,
                    @Param("userId") Integer userId,
                    @Param("title") String title);


//    删除用户自己的会话；数据库外键会同时删除该会话消息
    @Delete("""
        DELETE FROM cf_ai_chat_session
        WHERE id = #{sessionId} AND user_id = #{userId}
        """)
    int deleteSession(@Param("sessionId") Long sessionId,
                      @Param("userId") Integer userId);


    // 插入会话时使用的简单参数对象。
    class SessionRow {
        // 数据库生成的会话主键
        public Long id;

        // 会话所属用户
        public Integer userId;

        // 会话标题
        public String title;

        // 创建待插入的会话数据
        public SessionRow(Integer userId, String title) {
            this.userId = userId;
            this.title = title;
        }
    }
}
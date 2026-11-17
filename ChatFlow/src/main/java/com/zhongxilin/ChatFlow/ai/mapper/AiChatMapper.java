package com.zhongxilin.ChatFlow.ai.mapper;

import com.zhongxilin.ChatFlow.ai.dto.MessageView;
import com.zhongxilin.ChatFlow.ai.dto.SessionView;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiChatMapper {
    @Insert("""
        INSERT INTO cf_ai_chat_session(user_id, title)
        VALUES(#{userId}, #{title})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSession(SessionRow row);

    @Select("""
        SELECT id, title, created_at AS createdAt, updated_at AS updatedAt
        FROM cf_ai_chat_session
        WHERE user_id = #{userId}
        ORDER BY updated_at DESC
        """)
    List<SessionView> listSessions(@Param("userId") Integer userId);

    @Select("""
        SELECT id, title, created_at AS createdAt, updated_at AS updatedAt
        FROM cf_ai_chat_session
        WHERE id = #{sessionId} AND user_id = #{userId}
        """)
    SessionView findSession(@Param("sessionId") Long sessionId,
                            @Param("userId") Integer userId);

    @Select("""
        SELECT id, role, content, created_at AS createdAt
        FROM cf_ai_chat_message
        WHERE session_id = #{sessionId} AND user_id = #{userId}
        ORDER BY id ASC
        """)
    List<MessageView> listMessages(@Param("sessionId") Long sessionId,
                                   @Param("userId") Integer userId);

    @Insert("""
        INSERT INTO cf_ai_chat_message(session_id, user_id, role, content)
        VALUES(#{sessionId}, #{userId}, #{role}, #{content})
        """)
    int insertMessage(@Param("sessionId") Long sessionId,
                      @Param("userId") Integer userId,
                      @Param("role") String role,
                      @Param("content") String content);

    @Update("""
        UPDATE cf_ai_chat_session
        SET updated_at = CURRENT_TIMESTAMP
        WHERE id = #{sessionId} AND user_id = #{userId}
        """)
    int touchSession(@Param("sessionId") Long sessionId,
                     @Param("userId") Integer userId);

    @Update("""
        UPDATE cf_ai_chat_session
        SET title = #{title}, updated_at = CURRENT_TIMESTAMP
        WHERE id = #{sessionId} AND user_id = #{userId}
        """)
    int updateTitle(@Param("sessionId") Long sessionId,
                    @Param("userId") Integer userId,
                    @Param("title") String title);

    @Delete("""
        DELETE FROM cf_ai_chat_session
        WHERE id = #{sessionId} AND user_id = #{userId}
        """)
    int deleteSession(@Param("sessionId") Long sessionId,
                      @Param("userId") Integer userId);

    class SessionRow {
        public Long id;
        public Integer userId;
        public String title;

        public SessionRow(Integer userId, String title) {
            this.userId = userId;
            this.title = title;
        }
    }
}
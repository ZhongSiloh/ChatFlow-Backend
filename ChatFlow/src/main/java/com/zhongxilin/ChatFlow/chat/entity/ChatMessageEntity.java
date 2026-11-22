package com.zhongxilin.ChatFlow.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEntity {
    /** 消息表自增主键，插入后由 MyBatis 回填。 */
    private Long id;
    /** 消息类型：1 好友私聊，2 群聊。 */
    private Integer conversationType;
    /** 群消息使用群 ID；私聊目前以 0 存储并通过双方 ID 查询。 */
    private Long conversationId;
    /** 发送者用户 ID。 */
    private Long senderId;
    /** 私聊接收者用户 ID；群聊消息为 null。 */
    private Long recipientId;
    /** 持久化的消息正文。 */
    private String content;
    /** 数据库生成的消息创建时间。 */
    private LocalDateTime createdAt;
}

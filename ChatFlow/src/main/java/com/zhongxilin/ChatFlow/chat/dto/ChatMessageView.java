package com.zhongxilin.ChatFlow.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageView {
    private Long id;
    /** 会话类型：1 表示好友私聊，2 表示群聊。 */
    private Integer conversationType;
    /** 会话标识：私聊当前固定为 0 并由双方 ID 定位；群聊为群 ID。 */
    private Long conversationId;
    /** 实际发送者用户 ID，由后端认证身份确定。 */
    private Long senderId;
    /** 发送者登录名，由服务端查询用户资料填充。 */
    private String senderName;
    /** 发送者头像地址，由服务端查询用户资料填充。 */
    private String senderAvatar;
    /** 私聊接收者 ID；群聊消息为空。 */
    private Long recipientId;
    /** 消息正文。 */
    private String content;
    /** 消息在数据库中的创建时间。 */
    private LocalDateTime createdAt;

}

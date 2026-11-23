package com.zhongxilin.ChatFlow.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageRecord {
    private Long id;
    private Integer conversationType;
    private Long conversationId;
    private Long senderId;
    private Long recipientId;
    private String content;
    private LocalDateTime createdAt;
}

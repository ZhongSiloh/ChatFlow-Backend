package com.zhongxilin.ChatFlow.ai.dto;

import java.time.LocalDateTime;

// 返回给前端的一条聊天消息
public record MessageView(
        Long id, // 消息唯一 ID
        String role, // 消息角色，例如 user 或 assistant
        String content, // 消息正文
        LocalDateTime createdAt
) {}
package com.zhongxilin.ChatFlow.ai.dto;

import java.time.LocalDateTime;

// 返回给前端的会话信息，不直接暴露数据库实体
public record SessionView(
        // 会话唯一 ID
        Long id,
        // 前端显示的会话标题
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

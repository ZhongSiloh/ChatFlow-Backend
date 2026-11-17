package com.zhongxilin.ChatFlow.ai.dto;

import java.time.LocalDateTime;
public record SessionView(
        Long id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

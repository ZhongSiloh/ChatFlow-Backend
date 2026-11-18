package com.zhongxilin.ChatFlow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 表示前端发送消息时的请求体
public record SendMessageRequest(
        @NotBlank // 拒绝 null、空字符串和全空格内容
        @Size(max = 12000) // 限制单条消息长度
    String content
) { }

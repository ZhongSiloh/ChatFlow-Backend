package com.zhongxilin.ChatFlow.ai.service;

import com.zhongxilin.ChatFlow.ai.dto.MessageView;
import com.zhongxilin.ChatFlow.ai.dto.SessionView;
import com.zhongxilin.ChatFlow.ai.mapper.AiChatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiChatService {
    private final AiChatMapper mapper;
    private final DeepSeekClient deepSeekClient;

    public AiChatService(AiChatMapper mapper, DeepSeekClient deepSeekClient) {
        this.mapper = mapper;
        this.deepSeekClient = deepSeekClient;
    }

    public List<SessionView> listSessions(Integer userId) {
        return mapper.listSessions(userId);
    }

    @Transactional
    public Long createSession(Integer userId) {
        var row = new AiChatMapper.SessionRow(userId, "新对话");
        mapper.insertSession(row);
        return row.id;
    }

    public List<MessageView> listMessages(Integer userId, Long sessionId) {
        requireSession(userId, sessionId);
        return mapper.listMessages(sessionId, userId);
    }

    public Flux<String> send(Integer userId, Long sessionId, String prompt) {
        requireSession(userId, sessionId);

        String cleanPrompt = prompt == null ? "" : prompt.trim();
        if (cleanPrompt.isEmpty() || cleanPrompt.length() > 12000) {
            throw new IllegalArgumentException("消息不能为空且不能超过 12000 字");
        }

        // 先持久化用户消息，刷新页面后仍能看到提问。
        mapper.insertMessage(sessionId, userId, "user", cleanPrompt);

        List<Map<String, String>> context = new ArrayList<>();
        context.add(Map.of("role", "system",
                "content", "你是 ChatFlow 的 AI 助手。请使用清晰、准确的中文回答。"));

        for (MessageView message : mapper.listMessages(sessionId, userId)) {
            if (message.role().equals("user") || message.role().equals("assistant")) {
                context.add(Map.of("role", message.role(), "content", message.content()));
            }
        }

        mapper.touchSession(sessionId, userId);

        StringBuilder answer = new StringBuilder();
        return deepSeekClient.stream(context)
                .doOnNext(answer::append)
                .doFinally(signal -> {
                    if (!answer.isEmpty()) {
                        mapper.insertMessage(sessionId, userId,
                                "assistant", answer.toString());
                        mapper.touchSession(sessionId, userId);
                    }
                });
    }

    public void deleteSession(Integer userId, Long sessionId) {
        if (mapper.deleteSession(sessionId, userId) == 0) {
            throw new IllegalArgumentException("会话不存在");
        }
    }

    private void requireSession(Integer userId, Long sessionId) {
        if (mapper.findSession(sessionId, userId) == null) {
            throw new IllegalArgumentException("会话不存在");
        }
    }
}
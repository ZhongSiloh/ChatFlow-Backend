package com.zhongxilin.ChatFlow.ai.service;

import com.zhongxilin.ChatFlow.ai.dto.MessageView;
import com.zhongxilin.ChatFlow.ai.dto.SessionView;
import com.zhongxilin.ChatFlow.ai.mapper.AiChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//负责会话归属检查、消息校验、持久化和 AI 对话上下文
@Service
public class AiChatService {

    @Autowired
    private AiChatMapper mapper;

    @Autowired
    private DeepSeekClient deepSeekClient;


    //列出当前用户的会话
    public List<SessionView> listSessions(Integer userId) {
        return mapper.listSessions(userId);
    }


    //创建新会话
    @Transactional
    public Long createSession(Integer userId) {
        var row = new AiChatMapper.SessionRow(userId, "新对话");
        mapper.insertSession(row);
        return row.id;
    }

    //    读取指定会话的消息
    public List<MessageView> listMessages(Integer userId, Long sessionId) {
        requireSession(userId, sessionId);
        return mapper.listMessages(sessionId, userId);
    }


    //核心：“校验 → 落库提问 → 拼上下文 → 调模型 → 流式返回 → 收尾落库”
    public Flux<String> send(Integer userId, Long sessionId, String prompt) {
//        确认“这个会话存在，而且属于这个用户”
        requireSession(userId, sessionId);

//        参数清洗与长度校验
        String cleanPrompt = prompt == null ? "" : prompt.trim();
        if (cleanPrompt.isEmpty() || cleanPrompt.length() > 12000) {
            throw new IllegalArgumentException("消息不能为空且不能超过 12000 字");
        }

        // 先将用户消息写入数据库，刷新页面后仍能看到提问。
        mapper.insertMessage(sessionId, userId, "user", cleanPrompt);


//        上下文组装
        List<Map<String, String>> context = new ArrayList<>();
        context.add(Map.of("role", "system",
                "content", "你是 ChatFlow 的 AI 助手。请使用清晰、准确的中文回答。"));
        for (MessageView message : mapper.listMessages(sessionId, userId)) {
            if (message.role().equals("user") || message.role().equals("assistant")) {
                context.add(Map.of("role", message.role(), "content", message.content()));
            }
        }

//        更新会话的 updated_at
        mapper.touchSession(sessionId, userId);

//        返回流与两个操作符
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


//    私有前置校验方法：确认“这个会话存在，而且属于这个用户”，否则直接抛异常中断流程
    private void requireSession(Integer userId, Long sessionId) {
        if (mapper.findSession(sessionId, userId) == null) {
            throw new IllegalArgumentException("会话不存在");
        }
    }
}
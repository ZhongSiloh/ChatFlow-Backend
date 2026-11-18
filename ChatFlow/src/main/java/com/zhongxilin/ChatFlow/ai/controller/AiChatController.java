package com.zhongxilin.ChatFlow.ai.controller;

import com.zhongxilin.ChatFlow.ai.dto.SendMessageRequest;
import com.zhongxilin.ChatFlow.ai.service.AiChatService;
import com.zhongxilin.ChatFlow.utils.CurrentHolder;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/ai/chat")
public class AiChatController {
    private final AiChatService service;

    public AiChatController(AiChatService service) {
        this.service = service;
    }

    @GetMapping("/sessions")
    public Object sessions() {
        return service.listSessions(currentUserId());
    }

    @PostMapping("/sessions")
    public Object createSession() {
        return Map.of("id", service.createSession(currentUserId()));
    }

    @GetMapping("/sessions/{id}/messages")
    public Object messages(@PathVariable Long id) {
        return service.listMessages(currentUserId(), id);
    }

    @DeleteMapping("/sessions/{id}")
    public Object deleteSession(@PathVariable Long id) {
        service.deleteSession(currentUserId(), id);
        return Map.of("success", true);
    }

    @PostMapping(value = "/sessions/{id}/messages",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@PathVariable Long id,
                           @Valid @RequestBody SendMessageRequest request) {
        Integer userId = currentUserId();
        SseEmitter emitter = new SseEmitter(180_000L);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                service.send(userId, id, request.content()).toStream()
                        .forEach(chunk -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("token").data(chunk));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                // 记录真实异常，便于区分 DeepSeek、数据库及客户端连接问题。
                org.slf4j.LoggerFactory.getLogger(AiChatController.class)
                        .error("AI 流式回复失败，sessionId={}", id, e);
                try {
                    emitter.send(SseEmitter.event().name("error")
                            .data("AI 回复失败，请稍后重试"));
                } catch (IOException ignored) {}
                // 错误已通过 SSE 事件通知前端；正常关闭 emitter，避免再进入全局 JSON 异常处理器。
                emitter.complete();
            }
        });

        return emitter;
    }

    private Integer currentUserId() {
        // 按 CurrentHolder.java 的实际返回类型调整此行。
        return (Integer) CurrentHolder.getCurrentId();
    }
}
package com.zhongxilin.ChatFlow.ai.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 注解包名不变，仅示例
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;           // ← 包名改为 tools.jackson
import tools.jackson.databind.json.JsonMapper;    // ← ObjectMapper → JsonMapper

import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient {
    private final WebClient client;
    private final JsonMapper jsonMapper;          // ← 类型变更
    private final String apiKey;
    private final String model;

    public DeepSeekClient(
            JsonMapper jsonMapper,                // ← Spring Boot 4 自动配置的 Bean
            @Value("${deepseek.api-key}") String apiKey,
            @Value("${deepseek.base-url}") String baseUrl,
            @Value("${deepseek.model}") String model
    ) {
        this.jsonMapper = jsonMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.client = WebClient.builder().baseUrl(baseUrl).build();
    }

    public Flux<String> stream(List<Map<String, String>> messages) {
        Map<String, Object> body = Map.of(
                "model", model,
                "stream", true,
                "messages", messages
        );

        return client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                // 使用 Spring 的 SSE 解码器按事件解析，避免把 TCP 网络分块误当作完整事件。
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(ServerSentEvent::data)
                .filter(data -> data != null && !data.isBlank())
                .takeUntil("[DONE]"::equals)
                .filter(data -> !"[DONE]".equals(data))
                .map(this::extractContent)
                .filter(content -> !content.isEmpty());
    }

    private String extractContent(String json) {
        try {
            JsonNode root = jsonMapper.readTree(json);   // readTree 仍然可用[reference:2]
            JsonNode content = root.path("choices").path(0)
                    .path("delta").path("content");
            // isTextual() → isString()；asText() → asString()
            return content.isString() ? content.asString() : "";
        } catch (Exception e) {
            throw new IllegalStateException("无法解析 DeepSeek 流式响应", e);
        }
    }
}
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


//外部服务适配层，负责向 DeepSeek 发 HTTP 请求并解析它返回的 SSE 事件
@Component
public class DeepSeekClient {
//    WebClient 负责发起 HTTP 请求
private final WebClient client;        // 发起 HTTP 请求
    private final JsonMapper jsonMapper;   // 解析 JSON 数据块
    private final String apiKey;
    private final String model;

    public DeepSeekClient(
            JsonMapper jsonMapper,
            @Value("${deepseek.api-key}") String apiKey,
            @Value("${deepseek.base-url}") String baseUrl,
            @Value("${deepseek.model}") String model
    ) {
        this.jsonMapper = jsonMapper;
        this.apiKey = apiKey;
        this.model = model;
//        设置请求根地址，后续请求路径使用相对路径
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // 发送带上下文的对话请求，并逐段返回 AI 生成的文本
    public Flux<String> stream(List<Map<String, String>> messages) {
//        构造 DeepSeek Chat Completions 请求体
        Map<String, Object> body = Map.of(
                "model", model,
                "stream", true,  //流式开关：DeepSeek 文档明确 stream=true 时以 SSE 发送增量，且消息流以 data: [DONE] 结尾
                "messages", messages
        );

        return client.post()
                // 调用 Chat Completions 接口
                .uri("/chat/completions")
                // 请求内容是 JSON
                .contentType(MediaType.APPLICATION_JSON)
                // 希望服务端返回 SSE 流
                .accept(MediaType.TEXT_EVENT_STREAM)
                // 使用 Bearer Token 认证
                .header("Authorization", "Bearer " + apiKey)
                // 写入模型、消息上下文和流式开关
                .bodyValue(body)
// 将非成功 HTTP 状态转成异常
                .retrieve()


                //SSE 解析链
                // 使用 Spring 的 SSE 解码器按事件解析，避免把 TCP 网络分块误当作完整事件。
                // 读取响应流。生产实现应使用可靠的 SSE 解析器处理分块边界
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(ServerSentEvent::data)
                .filter(data -> data != null && !data.isBlank())
                .takeUntil("[DONE]"::equals)
                .filter(data -> !"[DONE]".equals(data))
                .map(this::extractContent)
                .filter(content -> !content.isEmpty());
    }

//    协议解码终点
    // 从 DeepSeek 的流式 JSON 响应提取 choices[0].delta.content
    private String extractContent(String json) {
        try {
            JsonNode root = jsonMapper.readTree(json);
            JsonNode content = root.path("choices").path(0)
                    .path("delta").path("content");

            // isTextual() → isString()；asText() → asString()
            return content.isString() ? content.asString() : "";
        } catch (Exception e) {
            throw new IllegalStateException("无法解析 DeepSeek 流式响应", e);
        }
    }
}
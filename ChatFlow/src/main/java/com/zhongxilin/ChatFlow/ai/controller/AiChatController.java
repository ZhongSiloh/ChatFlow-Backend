package com.zhongxilin.ChatFlow.ai.controller;

import com.zhongxilin.ChatFlow.ai.dto.SendMessageRequest;
import com.zhongxilin.ChatFlow.ai.service.AiChatService;
import com.zhongxilin.ChatFlow.utils.CurrentHolder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


//把 Web 请求转换为服务调用和 SSE 响应
@RestController
@RequestMapping("/ai/chat")
public class AiChatController {

    @Autowired
    private AiChatService service;


    //列出当前用户的会话
    @GetMapping("/sessions")
    public Object sessions() {
        return service.listSessions(currentUserId());
    }

    //创建新会话
//    返回形如 {"id": ...} 的 Map
    @PostMapping("/sessions")
    public Object createSession() {
        return Map.of("id", service.createSession(currentUserId()));
    }

//    读取指定会话的消息
    @GetMapping("/sessions/{id}/messages")
    public Object messages(@PathVariable Long id) {
        return service.listMessages(currentUserId(), id);
    }

    //删掉指定会话
    @DeleteMapping("/sessions/{id}")
    public Object deleteSession(@PathVariable Long id) {
        service.deleteSession(currentUserId(), id);
        return Map.of("success", true);
    }


    //核心，发送消息
    @PostMapping(value = "/sessions/{id}/messages",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@PathVariable Long id,
                           @Valid @RequestBody SendMessageRequest request) {


        //取用户与创建发射器
        Integer userId = currentUserId();
//        创建发射器，超时 3 分钟
        SseEmitter emitter = new SseEmitter(180_000L);
        //超时只是让 Spring 结束这个异步请求（触发 onTimeout），不会中断你下面那个异步线程，模型调用还在跑，线程还在占着。


        //临时创建一个“只有一个线程”的线程池，把后面那个 lambda 作为任务丢进去异步执行，然后立刻返回
//        等价于“开一条后台线程去跑这段逻辑，不阻塞当前请求线程”
//        Executors（工厂类）、newSingleThreadExecutor()（造池）、submit(...)（交任务）
        Executors.newSingleThreadExecutor().submit(() -> {
//            SseEmitter 是异步返回值，controller方法必须尽快返回，submit把耗时逻辑从 Tomcat 的请求线程上挪走

            try {
                //流式推送主体
//                返回一个流式容器：Reactor 的 Flux<String>
                service.send(userId, id, request.content()).toStream()
//.toStream() 是阻塞式适配：把异步推送的流变成同步拉取的 Stream，
// 每次 forEach 取一个元素时，线程会挂起等待上游下一个 token 到达。
                        .forEach(chunk -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("token").data(chunk));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

//                正常结束发 done 帧收尾
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();

            } catch (Exception e) {
                // 记录真实异常，便于区分 DeepSeek、数据库及客户端连接问题。
//                日志 + error 帧 + complete
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
package com.zhongxilin.ChatFlow.chat.controller;

import com.zhongxilin.ChatFlow.chat.dto.SendChatMessage;
import com.zhongxilin.ChatFlow.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatStompController {
    /** 实际执行业务校验、保存和投递的聊天服务。 */
    private final ChatService chatService;

    /** 构造 STOMP 控制器并注入聊天服务。 */
    public ChatStompController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.private.send")
    /** 处理好友私聊发送帧；发送者身份从认证 Principal 读取，不信任客户端字段。 */
    public void sendPrivate(SendChatMessage request, Principal principal) {
        chatService.sendPrivate(userId(principal), request);
    }

    @MessageMapping("/chat.group.send")
    /** 处理群聊发送帧；服务层会校验发送者的有效群成员身份。 */
    public void sendGroup(SendChatMessage request, Principal principal) {
        chatService.sendGroup(userId(principal), request);
    }

    /** 从已认证 Principal 解析用户 ID；未认证或 ID 格式异常时拒绝操作。 */
    private long userId(Principal principal) {
        if (principal == null) throw new IllegalArgumentException("WebSocket用户未认证");
        return Long.parseLong(principal.getName());
    }
}

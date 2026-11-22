package com.zhongxilin.ChatFlow.chat.controller;

import com.zhongxilin.ChatFlow.chat.dto.ChatMessageView;
import com.zhongxilin.ChatFlow.chat.service.ChatService;
import com.zhongxilin.ChatFlow.pojo.Result;
import com.zhongxilin.ChatFlow.utils.CurrentHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatHistoryController {
    /** 提供私聊和群聊历史查询的业务服务。 */
    private final ChatService chatService;

    /** 构造历史查询控制器并注入聊天服务。 */
    public ChatHistoryController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat/private/{friendId}")
    /** 查询当前登录用户与好友的历史消息，用户身份取自 HTTP token 上下文。 */
    public Result privateHistory(@PathVariable long friendId,
                                 @RequestParam(required = false) Long beforeId) {
        Integer currentId = CurrentHolder.getCurrentId();
        if (currentId == null) return Result.error("未登录");
        List<ChatMessageView> messages = chatService.privateHistory(currentId, friendId, beforeId);
        return Result.success(messages);
    }

    @GetMapping("/chat/group/{groupId}")
    /** 查询当前登录用户有权查看的群历史消息。 */
    public Result groupHistory(@PathVariable long groupId,
                               @RequestParam(required = false) Long beforeId) {
        Integer currentId = CurrentHolder.getCurrentId();
        if (currentId == null) return Result.error("未登录");
        List<ChatMessageView> messages = chatService.groupHistory(currentId, groupId, beforeId);
        return Result.success(messages);
    }
}

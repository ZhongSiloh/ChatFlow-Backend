package com.zhongxilin.ChatFlow.controller;

import com.zhongxilin.ChatFlow.pojo.Result;
import com.zhongxilin.ChatFlow.service.MessageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageRecordController {

    @Autowired
    private MessageRecordService messageRecordService;

    @GetMapping("/chat/messages")
    public Result getChatRecordLists(@RequestParam Integer conversationType,
                                     @RequestParam Long conversationId,
                                     @RequestParam(value = "senderId", required = false) Long senderId,
                                     @RequestParam Long recipientId){
        if (conversationType == null || conversationId == null || recipientId == null) {
            return Result.error("conversationType, conversationId and recipientId are required");
        }
        return Result.success(messageRecordService.getChatRecordLists(conversationType, conversationId, senderId, recipientId));
    }


}

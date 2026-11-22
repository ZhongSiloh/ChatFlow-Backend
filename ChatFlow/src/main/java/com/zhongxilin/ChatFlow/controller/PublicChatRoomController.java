package com.zhongxilin.ChatFlow.controller;

import com.zhongxilin.ChatFlow.pojo.HelloMessage;
import com.zhongxilin.ChatFlow.pojo.PublicChatRoomMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class PublicChatRoomController {

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public PublicChatRoomMessage greeting(HelloMessage message) throws Exception {
        Thread.sleep(500); // simulated delay
        return new PublicChatRoomMessage(HtmlUtils.htmlEscape(message.getName()));
    }

}

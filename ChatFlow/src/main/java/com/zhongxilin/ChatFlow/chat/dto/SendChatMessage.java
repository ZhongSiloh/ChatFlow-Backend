package com.zhongxilin.ChatFlow.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendChatMessage {
    /** 好友私聊的接收方用户 ID；群聊请求中为空。 */
    private Long friendId;
    /** 群聊目标群组 ID；私聊请求中为空。 */
    private Long groupId;
    /** 用户发送的文本正文；服务端会校验非空和长度。 */
    @NotBlank
    @Size(max = 5000)
    private String content;


}

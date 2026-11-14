package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//好友请求查看列表POJO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendReqestFeedback {
    private String fromUserId;
    private String toUserId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
}

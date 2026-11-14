package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//发起好友申请POJO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestData {
    private String fromUserId;
    private String toUserCode;
}

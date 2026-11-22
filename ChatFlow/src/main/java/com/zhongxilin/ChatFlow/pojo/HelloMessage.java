package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HelloMessage {

    private String name;
    private Long userId;
    private String senderName;
    private String avatar;
}

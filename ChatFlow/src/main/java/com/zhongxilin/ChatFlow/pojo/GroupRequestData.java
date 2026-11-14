package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//发起入群申请POJO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequestData {
    private Long fromUserId;
    private String groupCode;
}

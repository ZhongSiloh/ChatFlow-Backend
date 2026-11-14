package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//入群申请列表信息
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequestFeedback {
    private Long id;
    private Long groupId;
    private String groupCode;
    private String groupName;
    private String groupAvatar;
    private Long userId;
    private String username;
    private String userAvatar;
    private Integer status;
    private LocalDateTime createdAt;
}

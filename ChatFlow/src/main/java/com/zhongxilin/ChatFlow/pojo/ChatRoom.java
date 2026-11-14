package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 聊天室实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    /** 主键 */
    private Long id;
    /** 聊天室名称 */
    private String name;
    /** 聊天室描述 */
    private String description;
    /** 聊天室状态：1 开放，0 关闭 */
    private Integer status;
    /** 创建者用户 ID（管理员） */
    private Long createdBy;
    /** 创建时间 */
    private LocalDateTime createdAt;
}

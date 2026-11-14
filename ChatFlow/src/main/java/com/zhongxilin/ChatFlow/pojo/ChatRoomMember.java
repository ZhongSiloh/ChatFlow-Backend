package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 聊天室成员实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomMember {
    /** 主键 */
    private Long id;
    /** 聊天室 ID */
    private Long roomId;
    /** 用户 ID */
    private Long userId;
    /** 加入聊天室时间，也是聊天室消息可见起点 */
    private LocalDateTime joinTime;
    /** 离开聊天室时间，也是聊天室消息可见终点 */
    private LocalDateTime leaveTime;
    /** 成员状态：1 在聊天室，0 已离开 */
    private Integer status;
    /** 数据库生成列：在聊天室时为 1，否则为 NULL；用于约束有效成员唯一，应用不应写入 */
    private Integer activeKey;
}

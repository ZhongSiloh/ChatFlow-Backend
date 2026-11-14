package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    /** 主键 */
    private Long id;
    /** 当前用户 ID */
    private Long userId;
    /** 好友用户 ID */
    private Long friendId;
    /** 好友备注，仅 user_id 对应的用户可见 */
    private String remark;
    /** 关系状态：1 正常，0 已删除 */
    private Integer status;
    /** 成为好友时间，也是私聊记录可见起点 */
    private LocalDateTime createdAt;
    /** 删除好友时间 */
    private LocalDateTime deletedAt;
    /** 数据库生成列：status=1 时为 1，否则为 NULL；用于约束有效关系唯一，应用不应写入 */
    private Integer activeKey;
}

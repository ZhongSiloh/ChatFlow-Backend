package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 群成员实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMember {
    /** 主键 */
    private Long id;
    /** 群 ID */
    private Long groupId;
    /** 用户 ID */
    private Long userId;
    private String username;
    private String avatar;
    private String ownerId;//所在群的群主id
    /** 成员角色：0 普通成员，1 群主 */
    private Integer role;
    /** 群昵称 */
    private String groupNick;
    /** 加入时间，也是群消息可见起点 */
    private LocalDateTime joinTime;
    /** 离开时间，也是群消息可见终点 */
    private LocalDateTime leaveTime;
    /** 离开类型：1 主动退群，2 被踢，3 群解散 */
    private Integer leaveType;
    /** 成员状态：1 在群中，0 已离开 */
    private Integer status;
    /** 数据库生成列：在群时为 1，否则为 NULL；用于约束有效成员唯一，应用不应写入 */
    private Integer activeKey;
}

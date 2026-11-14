package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 群组实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    /** 主键 */
    private Long id;
    /** 群号，唯一 */
    private String groupCode;
    /** 群名称 */
    private String groupName;
    /** 群头像 URL */
    private String avatar;
    /** 群简介或公告 */
    private String description;
    /** 群主用户 ID */
    private Long ownerId;
    /** 最大成员数 */
    private Integer maxMembers;
    /** 加入模式：0 自由加入，1 需群主审核 */
    private Integer joinMode;
    /** 群状态：1 正常，0 已解散 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 解散时间 */
    private LocalDateTime dissolvedAt;
}

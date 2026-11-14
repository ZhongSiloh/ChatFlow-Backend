package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    /** 主键 */
    private Long id;
    /** 用户代码，用于加好友，唯一 */
    private String userCode;
    /** 登录账号 */
    private String username;
    /** BCrypt 加密后的密码 */
    private String password;
    /** 昵称 */
    private String nickname;
    /** 头像 URL */
    private String avatar;
    /** 角色：USER 普通用户，ADMIN 管理员 */
    private String role;
    /** 账号状态：1 正常，0 禁用 */
    private Integer status;
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    /** 最后活跃时间（离线时更新） */
    private LocalDateTime lastActiveTime;
    /** 逻辑删除标记：0 否，1 是 */
    private Integer deleted;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}

package com.zhongxilin.ChatFlow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//封装登录后向前端返回的结果
//和使用Threadlocal保存用户信息的区别
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginInfo {
    private Long id;
    private String username;
    private String nickname;
    private String userCode;
    private String role;
    private String avatar;
    private String token;
}

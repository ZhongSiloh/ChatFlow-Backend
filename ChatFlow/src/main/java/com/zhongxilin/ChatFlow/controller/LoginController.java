package com.zhongxilin.ChatFlow.controller;

import com.zhongxilin.ChatFlow.pojo.LoginInfo;
import com.zhongxilin.ChatFlow.pojo.Result;
import com.zhongxilin.ChatFlow.pojo.User;
import com.zhongxilin.ChatFlow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @Autowired
    private UserService userService;

    //    登录请求
    @PostMapping("/login")
    public Result login(@RequestBody User user) {

        LoginInfo loginInfo = userService.login(user);
        if (loginInfo != null) {
            return Result.success(loginInfo);
        }
        return Result.error("用户名或密码错误");
    }

    //    注册请求
    @PostMapping("/auth/register")
    public Result register(@RequestBody User user) {
        LoginInfo loginInfo = userService.register(user);

        if (loginInfo != null) {
            return Result.success(loginInfo);
        }
        return Result.error("用户已存在");



    }
}

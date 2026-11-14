package com.zhongxilin.ChatFlow.controller;

import com.zhongxilin.ChatFlow.pojo.Group;
import com.zhongxilin.ChatFlow.pojo.Result;
import com.zhongxilin.ChatFlow.pojo.User;
import com.zhongxilin.ChatFlow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //编辑个人资料
    @PutMapping("/me/{id}")
    public Result editUserProfile(@PathVariable Integer id, @RequestBody User user) {
        userService.editUserProfile(id, user);
        return Result.success();
    }
    //查询个人资料/查询回显个人资料
    @GetMapping("/me/{id}")
    public Result getUserProfile(@PathVariable("id") Integer id) {
        User user = userService.getUserProfile(id);

        return Result.success(user);
    }

    //访问他人主页，按 id 查他人
    @GetMapping("/user/{id}")
    public Result getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);


        return Result.success(user);
    }

    //访问群主页，按id查群
    @GetMapping("/group/{id}")
    public Result getGroupById(@PathVariable Integer id) {
        Group group = userService.getGroupById(id);

        return Result.success(group);
    }
}

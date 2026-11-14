package com.zhongxilin.ChatFlow.controller;

import com.zhongxilin.ChatFlow.pojo.Group;
import com.zhongxilin.ChatFlow.pojo.Result;
import com.zhongxilin.ChatFlow.pojo.User;
import com.zhongxilin.ChatFlow.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminController {

    @Autowired
    private AdminService adminService;

    //平台用户管理:刷新列表查询与按用户名模糊查询
    @GetMapping("/admin/users")
    public Result listUser(@RequestParam(value = "username",required = false) String username){
        List<User> list=adminService.findAll(username);

        return Result.success(list);
    }
    //平台用户管理:逻辑删除
    @PostMapping("/admin/users/{id}")
    public Result deleteUser(@PathVariable("id") Integer id){
        adminService.deleteUser(id);

        return Result.success();
    }


    //平台群管理:刷新列表查询与按群名模糊查询
    @GetMapping("/admin/groups")
    public Result listGroup(@RequestParam(value = "groupName", required = false) String groupName){
        List<Group> list=adminService.findAllGroups(groupName);

        return Result.success(list);
    }
    //平台群管理:逻辑删除
    @PostMapping("/admin/groups/{id}")
    public Result deleteGroup(@PathVariable("id") Integer id){

         adminService.deleteGroup(id);

        return Result.success();
    }


}

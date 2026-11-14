package com.zhongxilin.ChatFlow.service;

import com.zhongxilin.ChatFlow.pojo.Group;
import com.zhongxilin.ChatFlow.pojo.User;

import java.util.List;

public interface AdminService {
    List<User> findAll(String username);

    void deleteUser(Integer id);

    List<Group> findAllGroups(String groupName);

    void deleteGroup(Integer id);
}

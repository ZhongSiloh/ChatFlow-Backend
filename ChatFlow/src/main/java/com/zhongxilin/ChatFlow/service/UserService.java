package com.zhongxilin.ChatFlow.service;

import com.zhongxilin.ChatFlow.pojo.Group;
import com.zhongxilin.ChatFlow.pojo.LoginInfo;
import com.zhongxilin.ChatFlow.pojo.User;

public interface UserService {
    LoginInfo login(User user);

    LoginInfo register(User user);

    User getUserProfile(Integer id);

    void editUserProfile(Integer id,User user);

    User getUserById(Integer id);

    Group getGroupById(Integer id);
}

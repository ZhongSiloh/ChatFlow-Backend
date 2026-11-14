package com.zhongxilin.ChatFlow.service.Impl;

import com.zhongxilin.ChatFlow.mapper.AdminMapper;
import com.zhongxilin.ChatFlow.pojo.Group;
import com.zhongxilin.ChatFlow.pojo.User;
import com.zhongxilin.ChatFlow.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public List<User> findAll(String username) {
        return adminMapper.selectUsersByUsername(username);
    }

    @Override
    public void deleteUser(Integer id) {
        adminMapper.deleteUserById(id);
    }

    @Override
    public List<Group> findAllGroups(String groupName) {
        return adminMapper.selectGroupsByGroupName(groupName);
    }

    @Override
    public void deleteGroup(Integer id) {
        adminMapper.deleteGroupById(id);
    }
}

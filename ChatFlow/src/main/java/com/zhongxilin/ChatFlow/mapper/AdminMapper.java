package com.zhongxilin.ChatFlow.mapper;

import com.zhongxilin.ChatFlow.pojo.Group;
import com.zhongxilin.ChatFlow.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminMapper {

    // 根据用户名关键字查询用户列表（XML中关键字为空时查询全部用户）
    List<User> selectUsersByUsername(String username);

    // 根据用户ID逻辑删除用户，将deleted字段设置为1
    @Update("update cf_user set deleted=1 where id=#{id}")
    void deleteUserById(Integer id);

    // 根据群名关键字查询群列表（XML中关键字为空时查询全部群）
    List<Group> selectGroupsByGroupName(String groupName);

    // 根据群ID逻辑删除群，将status字段设置为0
    @Update("update cf_group set status=0 where id=#{id}")
    void deleteGroupById(Integer id);
}

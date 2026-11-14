package com.zhongxilin.ChatFlow.mapper;

import com.zhongxilin.ChatFlow.pojo.Group;
import com.zhongxilin.ChatFlow.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.Set;

@Mapper
public interface UserMapper {

    @Select("select id,username,nickname,user_code,role,avatar,status,deleted from cf_user where username=#{username} and password=#{password}")
    // 查询用户名和密码匹配的用户信息
    User selectByUsernameAndPassword(User user);

    // 根据用户名查询用户信息
    @Select("select * from cf_user where username=#{username}")
    User selectByUsername(String username);

    // 向用户表插入新用户
    @Insert("insert into cf_user(user_code, username, password, nickname) values (#{userCode},#{username},#{password},#{nickname})")
    void insertUser(User user);

    // 查询所有用户的用户编码
    @Select("select user_code from cf_user")
    Set<String> selectAllUserCode();

    // 根据用户ID查询用户昵称、用户名、头像和角色
    @Select("select nickname,username,avatar,role from cf_user where id=#{id}")
    User getById(Integer id);

    // 根据用户ID更新用户名和昵称
    @Update("update cf_user set nickname=#{user.nickname},username=#{user.username} where id=#{id}")
    void updateProfile(@Param("id") Integer id,@Param("user") User user);

    // 根据用户ID查询用户主页的名、昵称、角色、头像和用户编码
    @Select("select username,nickname,role,avatar,user_code from cf_user where id=#{id}")
    User getUserById(Integer id);

    // 根据群ID查询群主页信息
    @Select("select * from cf_group where id=#{id}")
    Group getGroupById(Integer id);
}

package com.zhongxilin.ChatFlow.service.Impl;

import com.zhongxilin.ChatFlow.mapper.UserMapper;
import com.zhongxilin.ChatFlow.pojo.Group;
import com.zhongxilin.ChatFlow.pojo.LoginInfo;
import com.zhongxilin.ChatFlow.pojo.User;
import com.zhongxilin.ChatFlow.service.UserService;
import com.zhongxilin.ChatFlow.utils.JwtUtils;
import com.zhongxilin.ChatFlow.utils.UserCodeGenerate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginInfo login(User user) {

//        使用BCrypt进行校验密码：
        // 1. 按用户名查用户（不再用明文密码参与查询）
        User e = userMapper.selectByUsername(user.getUsername());

        if (e == null) {
            log.info("登录失败：用户不存在");
            return null;
        }
        log.info("查到用户：id={}, username={}", e.getId(), e.getUsername());

        // 2. 状态/删除校验，不通过必须 return
        if (e.getStatus() == null || e.getStatus() != 1) {
            log.info("登录失败：状态异常（status={}）", e.getStatus());
            return null;
        }
        if (e.getDeleted() == null || e.getDeleted() != 0) {
            log.info("登录失败：已删除（deleted={}）", e.getDeleted());
            return null;
        }

        // 3. 关键：用BCrypt- matches(明文, 数据库存的哈希值) 校验密码
        if(!passwordEncoder.matches(user.getPassword(), e.getPassword())) {
            log.info("登录失败：密码错误，username={}", user.getUsername());
            return null;
        }


//4.生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", e.getId());
        claims.put("username", e.getUsername());
        String jwt = JwtUtils.generateToken(claims);

        return new LoginInfo(e.getId(), e.getUsername(), e.getNickname(), e.getUserCode(), e.getRole(), e.getAvatar(), jwt);


    }

    @Override
    public LoginInfo register(User user) {

//        检测是否已注册，按用户名查重
        User u = userMapper.selectByUsername(user.getUsername());
        //注册的用户名和密码的用户已存在
        if (u != null) {
            return null;
        }


        //分配userCode、nickname
        user.setNickname(user.getUsername());

        //查询数据库所有用户的usercode，放入集合
        Set<String> existedCodes = userMapper.selectAllUserCode();
        do {
            user.setUserCode(UserCodeGenerate.generateUserCode("CF", 6));
        } while (existedCodes.contains(user.getUserCode()));

        //关键：BCrypt加密密码后再入库
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userMapper.insertUser(user);

        //存入数据库，得到id后再查
        User registerUser = userMapper.selectByUsername(user.getUsername());
        //生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", registerUser.getId());
        claims.put("username", registerUser.getUsername());
        String jwt = JwtUtils.generateToken(claims);

        return new LoginInfo(registerUser.getId(), registerUser.getUsername(), registerUser.getNickname(), registerUser.getUserCode(), registerUser.getRole(), registerUser.getAvatar(), jwt);
    }

    @Override
    public User getUserProfile(Integer id) {
        return userMapper.getById(id);
    }

    @Override
    public void editUserProfile(Integer id, User user) {

        userMapper.updateProfile(id, user);
    }

    @Override
    public User getUserById(Integer id) {
        return userMapper.getUserById(id);

    }

    @Override
    public Group getGroupById(Integer id) {
        return userMapper.getGroupById(id);
    }


}

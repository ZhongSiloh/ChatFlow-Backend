package com.zhongxilin.ChatFlow.controller;

import com.zhongxilin.ChatFlow.pojo.FriendReqestFeedback;
import com.zhongxilin.ChatFlow.pojo.Result;
import com.zhongxilin.ChatFlow.pojo.FriendRequestData;
import com.zhongxilin.ChatFlow.pojo.User;
import com.zhongxilin.ChatFlow.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class FriendController {

    @Autowired
    private FriendService friendService;

    // 刷新好友列表或按用户名模糊查询好友
    @GetMapping("/friends")
    public Result getFriendsList(@RequestParam("id") Integer id,
                                 @RequestParam(value = "username", required = false) String username) {
        List<User> friends = friendService.getFriendsList(id, username);
        return Result.success(friends);
    }

    // 逻辑删除好友关系
    @PostMapping("/friends/{userId}/{friendId}")
    public Result deleteFriend(@PathVariable Integer userId, @PathVariable Integer friendId) {
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }

//    按 userCode 发起申请添加好友
    //将发送方id、接收方userCode，写入friend_request表
    @PostMapping("/friend-requests")
    public Result sendRequest(@RequestBody FriendRequestData friendRequestData){

        List<Map<String, Object>> requestData = friendService.sendRequest(friendRequestData);

        if(requestData!=null){
            return Result.error("已发送过好友请求，请勿重复发送");
        }
        return Result.success("好友请求发送成功");
    }


    //查找（当前用户）申请的所有用户的好友请求
    @GetMapping("/friend-requests/sent")
    public Result listRequests(@RequestParam Integer fromUserId){
        List<FriendReqestFeedback> friendReqestFeedback=friendService.listRequests(fromUserId);

        if(friendReqestFeedback!=null && !friendReqestFeedback.isEmpty()){
            return Result.success(friendReqestFeedback);
        }
        return Result.error("没有发送好友请求");
    }
    
    //（当前用户）查找收到的所有好友请求
    @GetMapping("/friend-requests/received")
    public Result listReceivedRequests(@RequestParam Integer toUserId){
        List<FriendReqestFeedback> friendReqestFeedback=friendService.listReceivedRequests(toUserId);

        if(friendReqestFeedback!=null && !friendReqestFeedback.isEmpty()){
            return Result.success(friendReqestFeedback);
        }
        return Result.error("没有收到的好友请求");
    }

    //同意并建立双向好友
    //前端已正确渲染收到的所有好友请求，并根据status决定是否显示同意按钮，点击同意按钮后，调用此接口
    @PostMapping("/friend-requests/accept")
    public Result acceptRequest(@RequestParam Integer fromUserId,@RequestParam Integer toUserId){
        friendService.acceptRequest(fromUserId,toUserId);
        return Result.success("好友请求已接受");
    }

    //拒绝好友请求
    @PostMapping("/friend-requests/reject")
    public Result rejectRequest(@RequestParam Integer fromUserId,@RequestParam Integer toUserId){
        // 1.更新好友请求状态为已拒绝（2）
        friendService.rejectRequest(fromUserId, toUserId);
        return Result.success("好友请求已拒绝");

    }



}

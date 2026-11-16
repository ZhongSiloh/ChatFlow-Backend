package com.zhongxilin.ChatFlow.controller;

import com.zhongxilin.ChatFlow.pojo.*;
import com.zhongxilin.ChatFlow.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupController {

    @Autowired
    private GroupService groupService;

    // 刷新群组列表或按群组名模糊查询群组（仅自己加入或创建的，前端实现对自己创建的群的特殊标记）
    @GetMapping("/groups")
    public Result getGroupList(@RequestParam("id") Integer userId,
                               @RequestParam(value = "groupName", required = false) String groupName){
        List<Group> groups = groupService.getGroupList(userId, groupName);
        return Result.success(groups);
    }

    //创建群并成为群主
    //ownerId即为currentUserId，前端传入
    @PostMapping("/groups")
    public Result createGroup(@RequestBody Group group){
        groupService.createGroup(group);
        return Result.success("群创建成功");
    }

    //查询群主页资料/查询回显群主页，返回的ownerId用于前端判断当前用户是否为群主，若是则渲染编辑按钮
//    接口实现在UserController

    //（群主，前端判断当前用户是否为群主）修改群主页资料（avatar、description、join_mode）
    @PostMapping("/group/{groupId}")
    public Result editGroupProfile(@PathVariable("groupId") Integer groupId, @RequestBody Group group){
        groupService.editGroupProfile(groupId, group);
        return Result.success("群资料修改成功");
    }


    //加入群或申请加入群（根据join_mode判断是否需要审核，0自由加入 1需群主审核）
    //自由加入时，直接将用户加入cf_group_member表；
    //需审核时，将申请写入cf_group_join_request表，groupId,userId,status=0
    @PostMapping("/group-requests")
    public Result joinGroup(@RequestBody GroupRequestData groupRequestData){

        GroupJoinResult joinResult = groupService.joinGroup(groupRequestData);
        if (joinResult == GroupJoinResult.DUPLICATE_REQUEST) {
            return Result.error("已加入该群或已有待处理的入群请求");
        }
        if (joinResult == GroupJoinResult.JOINED) {
            return Result.success("已成功加入群聊");
        }
        return Result.success("已提交入群申请");
    }

    //（群主，前端比较currentId=ownerId，提供接口）查找收到的所有加入群的请求
    //前端仅传入currentUserId，后端通过cf_group找到currentUserId=ownerId的群组，cf_group_join_request表查询这些群组的入群请求（status=0）
    @GetMapping("/group-requests/received")
    public Result groupRequestReceivedList(@RequestParam("userId") Integer userId){
        List<GroupRequestFeedback> requests = groupService.groupRequestReceivedList(userId);
        return Result.success(requests);
    }

    //查找（当前用户）申请的所有加入群的请求
    //前端仅传入currentUserId，后端通过cf_group_join_request表，userId=#{currentUserId}查询这些群组的入群请求（status=0）
    @GetMapping("/group-requests/sent")
    public Result groupRequestSentList(@RequestParam("userId") Integer userId){
        List<GroupRequestFeedback> requests = groupService.groupRequestSentList(userId);
        return Result.success(requests);
    }

    //群主同意入群
    @PostMapping("/group-requests/accept")
    public Result acceptGroupRequest(@RequestParam("groupId") Integer groupId, @RequestParam("userId") Integer userId){
        groupService.acceptGroupRequest(groupId, userId);
        return Result.success("已同意入群申请");
    }

    //群主拒绝入群
    @PostMapping("/group-requests/reject")
    public Result rejectGroupRequest(@RequestParam("groupId") Integer groupId, @RequestParam("userId") Integer userId){
        groupService.rejectGroupRequest(groupId, userId);
        return Result.success("已拒绝入群申请");
    }

    //群成员列表,或按群成员名模糊查询
    @GetMapping("/group-members/{groupId}")
    public Result getGroupMembersList(@PathVariable("groupId") Integer groupId,
                                      @RequestParam(value = "username", required = false) String username){
        List<GroupMember> members = groupService.getGroupMembersList(groupId, username);

        return Result.success(members);
    }

    //群主踢出成员（前端判断是否为群主提供接口）
    @DeleteMapping("/group-members/remove/{groupId}/{userId}")
    public Result removeGroupMember(@PathVariable("groupId") Integer groupId, @PathVariable("userId") Integer userId){
        groupService.removeGroupMember(groupId, userId);
        return Result.success("已移除群成员");
    }

    //当前成员(userId)主动退群（若当前成员role=1为群主时则不能退群）
    @DeleteMapping("/group-members/quit/{groupId}/{userId}")
    public Result quitGroup(@PathVariable("groupId") Integer groupId, @PathVariable("userId") Integer userId){
        groupService.quitGroup(groupId, userId);
        return Result.success("已退出群聊");
    }

    //群主解散群（前端判断是否为群主提供接口）
    @DeleteMapping("/group-members/disband/{groupId}")
    public Result disbandGroup(@PathVariable("groupId") Integer groupId){
        groupService.disbandGroup(groupId);
        return Result.success("群聊已解散");
    }







}

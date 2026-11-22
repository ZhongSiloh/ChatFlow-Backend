package com.zhongxilin.ChatFlow.chat.service;

import com.zhongxilin.ChatFlow.chat.dto.ChatMessageView;
import com.zhongxilin.ChatFlow.chat.dto.SendChatMessage;
import com.zhongxilin.ChatFlow.chat.entity.ChatMessageEntity;
import com.zhongxilin.ChatFlow.chat.mapper.ChatMessageMapper;
import com.zhongxilin.ChatFlow.mapper.FriendMapper;
import com.zhongxilin.ChatFlow.mapper.GroupMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service
public class ChatService {
    /** 消息 Mapper：负责消息写入和历史分页查询。 */
    private final ChatMessageMapper messageMapper;
    /** 好友 Mapper：验证私聊双方仍有有效好友关系。 */
    private final FriendMapper friendMapper;
    /** 群 Mapper：验证成员资格并读取群成员。 */
    private final GroupMapper groupMapper;
    /** Spring STOMP 投递工具：向用户队列或群主题发送消息。 */
    private final SimpMessagingTemplate messagingTemplate;

    /** 注入消息数据访问、好友/群权限检查和 STOMP 投递依赖。 */
    public ChatService(ChatMessageMapper messageMapper, FriendMapper friendMapper,
                       GroupMapper groupMapper, SimpMessagingTemplate messagingTemplate) {
        this.messageMapper = messageMapper;
        this.friendMapper = friendMapper;
        this.groupMapper = groupMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    /** 校验好友关系、保存私聊消息，并分别投递给发送者和接收者。 */
    public ChatMessageView sendPrivate(long senderId, SendChatMessage request) {
        Long friendId = request.getFriendId();
        requireContent(request.getContent());
        if (friendId == null || friendId == senderId) throw new IllegalArgumentException("好友ID无效");
        if (friendMapper.countActiveFriendshipByIds(senderId, friendId) == 0
                || friendMapper.countActiveFriendshipByIds(friendId, senderId) == 0)
            throw new IllegalArgumentException("双方不是有效好友，不能私聊");

        ChatMessageView saved = persist(1, 0L, senderId, friendId, request.getContent());
        messagingTemplate.convertAndSendToUser(Long.toString(senderId), "/queue/chat", saved);
        messagingTemplate.convertAndSendToUser(Long.toString(friendId), "/queue/chat", saved);
        return saved;
    }

    @Transactional
    /** 校验群成员资格、保存群消息，并广播到对应群主题。 */
    public ChatMessageView sendGroup(long senderId, SendChatMessage request) {
        Long groupId = request.getGroupId();
        requireContent(request.getContent());
        if (groupId == null || groupMapper.countActiveMemberByIds(groupId, senderId) == 0)
            throw new IllegalArgumentException("你不是该群的有效成员");

        ChatMessageView saved = persist(2, groupId, senderId, null, request.getContent());
        messagingTemplate.convertAndSend("/topic/chat.groups." + groupId, saved);
        return saved;
    }

    /** 验证当前用户有权查看好友会话，再读取一页历史并按时间正序返回。 */
    public List<ChatMessageView> privateHistory(long userId, long friendId, Long beforeId) {
        if (friendMapper.countActiveFriendshipByIds(userId, friendId) == 0
                || friendMapper.countActiveFriendshipByIds(friendId, userId) == 0)
            throw new IllegalArgumentException("双方不是有效好友，不能查看私聊");
        List<ChatMessageView> history = new ArrayList<>(messageMapper.findPrivateHistory(userId, friendId, beforeId));
        Collections.reverse(history);
        return history;
    }

    /** 验证当前用户仍是群成员，再读取一页群消息并按时间正序返回。 */
    public List<ChatMessageView> groupHistory(long userId, long groupId, Long beforeId) {
        if (groupMapper.countActiveMemberByIds(groupId, userId) == 0)
            throw new IllegalArgumentException("你不是该群的有效成员");
        List<ChatMessageView> history = new ArrayList<>(messageMapper.findGroupHistory(groupId, beforeId));
        Collections.reverse(history);
        return history;
    }

    /** 清理并保存正文，随后读取含发送者资料的完整消息视图。 */
    private ChatMessageView persist(int type, long conversationId, long senderId, Long recipientId, String content) {
        ChatMessageEntity message = new ChatMessageEntity();
        message.setConversationType(type);
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        message.setContent(HtmlUtils.htmlEscape(content.trim()));
        messageMapper.insert(message);
        return messageMapper.findById(message.getId());
    }

    /** 拒绝空白正文及超过 5000 字符的消息。 */
    private void requireContent(String content) {
        if (content == null || content.isBlank() || content.length() > 5000)
            throw new IllegalArgumentException("消息不能为空且不能超过5000字");
    }
}

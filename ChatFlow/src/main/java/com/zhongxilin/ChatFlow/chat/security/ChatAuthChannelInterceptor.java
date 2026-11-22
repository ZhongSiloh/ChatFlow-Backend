package com.zhongxilin.ChatFlow.chat.security;

import com.zhongxilin.ChatFlow.mapper.GroupMapper;
import com.zhongxilin.ChatFlow.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
public class ChatAuthChannelInterceptor implements ChannelInterceptor {
    /** 用于在群主题订阅时确认当前用户仍是有效成员。 */
    private final GroupMapper groupMapper;

    /** 创建 STOMP 入站拦截器并注入群成员查询能力。 */
    public ChatAuthChannelInterceptor(GroupMapper groupMapper) {
        this.groupMapper = groupMapper;
    }

    /** 处理入站 STOMP 帧：认证连接，并拦截无权访问的群主题订阅。 */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> values = accessor.getNativeHeader("Authorization");
            if (values == null || values.isEmpty() || !values.get(0).startsWith("Bearer "))
                throw new IllegalArgumentException("WebSocket连接缺少登录令牌");
            Claims claims = JwtUtils.parseToken(values.get(0).substring(7));
            Object id = claims.get("id");
            if (id == null) throw new IllegalArgumentException("登录令牌无用户ID");
            accessor.setUser((Principal) () -> id.toString());
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/chat.groups.")) {
                Principal user = accessor.getUser();
                if (user == null) throw new IllegalArgumentException("请先登录");
                long groupId;
                try {
                    groupId = Long.parseLong(destination.substring("/topic/chat.groups.".length()));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("群聊地址无效");
                }
                if (groupMapper.countActiveMemberByIds(groupId, Long.parseLong(user.getName())) == 0)
                    throw new IllegalArgumentException("非群成员不能订阅群消息");
            }
        }
        return message;
    }
}

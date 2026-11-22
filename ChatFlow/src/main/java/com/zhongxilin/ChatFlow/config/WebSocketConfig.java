package com.zhongxilin.ChatFlow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import com.zhongxilin.ChatFlow.chat.security.ChatAuthChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** 处理 STOMP CONNECT JWT 认证和群主题订阅授权的入站拦截器。 */
    private final ChatAuthChannelInterceptor chatAuthChannelInterceptor;

    /** 构建 WebSocket 配置并注入 STOMP 入站安全拦截器。 */
    public WebSocketConfig(ChatAuthChannelInterceptor chatAuthChannelInterceptor) {
        this.chatAuthChannelInterceptor = chatAuthChannelInterceptor;
    }


    /** 配置服务端消息代理、客户端应用目的地前缀和用户队列前缀。 */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /** 注册 STOMP 帧拦截器，使 CONNECT 与 SUBSCRIBE 经过认证和授权。 */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(chatAuthChannelInterceptor);
    }

    @Override
    /** 注册 WebSocket/STOMP 握手端点，供前端建立实时连接。 */
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/gs-guide-websocket").setAllowedOriginPatterns("*"); // 关键：允许来自 Vue 开发服务器的跨域连接
    }
}

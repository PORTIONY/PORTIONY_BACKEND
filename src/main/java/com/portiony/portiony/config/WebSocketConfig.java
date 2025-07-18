package com.portiony.portiony.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat") // 프론트에서 이 주소로 연결함
                .setAllowedOriginPatterns("*"); // CORS 허용
                //.withSockJS(); // fallback 지원
        // 2. SockJS fallback용 (프론트에서 SockJS 쓸 경우용)
        registry.addEndpoint("/ws-chat-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub"); // 구독 주소 prefix
        registry.setApplicationDestinationPrefixes("/pub"); // 메시지 보낼 때 prefix
    }
}
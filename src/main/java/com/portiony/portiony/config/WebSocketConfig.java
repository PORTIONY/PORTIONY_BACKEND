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
        registry.addEndpoint("/ws-chat") // 클라이언트가 연결하는 웹 소켓의 입구를 정의
                .setAllowedOriginPatterns("*"); // CORS 허용
                //.withSockJS(); // fallback 지원
        // 프론트에서 sockjs 사용하므로 해당 코드를 사용
        registry.addEndpoint("/ws-chat-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 라우팅 경로
        registry.enableSimpleBroker("/sub"); // 구독(받는 사람 쪽) 주소 prefix / 서버 > 클라이언트(subscribe)
        registry.setApplicationDestinationPrefixes("/pub"); // 메시지 보낼 때 prefix / 클라이언트 > 서버(messagemapping)
    }
}
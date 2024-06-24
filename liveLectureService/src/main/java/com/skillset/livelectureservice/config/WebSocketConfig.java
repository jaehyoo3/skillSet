package com.skillset.livelectureservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    // WebSocketHandler를 주입받는 생성자
    public WebSocketConfig(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    // WebSocket 핸들러를 "/ws" 경로에 등록하는 메서드
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws").setAllowedOrigins("*");
    }
}
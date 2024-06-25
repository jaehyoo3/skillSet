package com.skillset.livelectureservice.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;


@Configuration // 이 클래스가 스프링의 설정 클래스임을 나타냅니다.
@EnableWebSocket // 이 애노테이션은 WebSocket을 활성화하는 역할을 합니다.
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    // WebSocketHandler를 주입받는 생성자
    // 이 생성자는 WebSocketHandler를 인자로 받아서 필드에 할당합니다.
    public WebSocketConfig(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    // WebSocket 핸들러를 "/chat/{roomId}" 경로에 등록하는 메서드
    // WebSocket 연결을 특정 경로에 매핑하는 역할을 합니다.
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // WebSocket 핸들러를 "/chat/{roomId}" 경로에 등록하고, 모든 출처의 요청을 허용합니다.
        registry.addHandler(webSocketHandler, "/chat/{roomId}")
                .setAllowedOrigins("*") // 모든 출처에서의 요청을 허용합니다.
                .addInterceptors(new HttpSessionHandshakeInterceptor() {
                    // WebSocket 핸드셰이크 이전에 실행되는 인터셉터를 추가합니다.
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        // 요청이 ServletServerHttpRequest 인스턴스인지 확인합니다.
                        if (request instanceof ServletServerHttpRequest) {
                            // 요청을 ServletServerHttpRequest로 캐스팅합니다.
                            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                            // HttpServletRequest 객체를 얻습니다.
                            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
                            // 요청 URI를 가져옵니다.
                            String uri = httpServletRequest.getRequestURI();

                            // URI에서 roomId를 추출합니다.
                            String roomId = uri.split("/chat/")[1];
                            // roomId가 null이 아닌 경우 attributes 맵에 roomId를 추가합니다.
                            if (roomId != null) {
                                attributes.put("roomId", roomId);
                            } else {
                                // roomId가 없는 경우 예외를 발생시킵니다.
                                throw new IllegalArgumentException("Room ID is missing in the path.");
                            }
                        }
                        // 부모 클래스의 beforeHandshake 메서드를 호출하여 기본 핸들링을 수행합니다.
                        return super.beforeHandshake(request, response, wsHandler, attributes);
                    }
                });
    }
}

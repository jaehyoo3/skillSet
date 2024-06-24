package com.skillset.livelectureservice.config;

import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    // WebSocket 세션을 저장하는 리스트
    private final List<WebSocketSession> sessions = new ArrayList<>();

    // 새로운 WebSocket 연결이 수립되었을 때 호출되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session); // 세션 리스트에 추가
    }

    // 클라이언트로부터 메시지를 수신했을 때 호출되는 메서드
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        for (WebSocketSession webSocketSession : sessions) {
            // 메시지를 보낸 세션을 제외한 다른 모든 세션에 메시지를 전송
            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                webSocketSession.sendMessage(message);
            }
        }
    }

    // WebSocket 연결이 종료되었을 때 호출되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session); // 세션 리스트에서 제거
    }
}
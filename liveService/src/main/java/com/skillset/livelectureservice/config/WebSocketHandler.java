package com.skillset.livelectureservice.config;
import com.skillset.livelectureservice.domain.Classroom;
import com.skillset.livelectureservice.domain.Message;
import com.skillset.livelectureservice.domain.Video;
import com.skillset.livelectureservice.repository.ClassroomRepository;
import com.skillset.livelectureservice.repository.MessageRepository;
import com.skillset.livelectureservice.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
    private final Map<String, List<WebSocketSession>> roomSessions = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MessageRepository messageRepository;
    private final ClassroomRepository classroomRepository;
    private final VideoRepository videoRepository;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomId(session);
        roomSessions.computeIfAbsent(roomId, k -> new ArrayList<>()).add(session);

        // 세션에서 사용자 이름 추출 및 저장 (여기서는 session ID를 사용)
        String userName = session.getId(); // 실제로는 세션에서 사용자 이름을 추출
        session.getAttributes().put("userName", userName);

        String entryMessage = String.format("User %s has entered the room.", userName);

        // JSON 형식으로 입장 메시지를 구성
        Map<String, Object> message = Map.of(
                "type", "entry",
                "message", entryMessage
        );

        broadcastMessage(roomId, "entry", message);

        // 현재 입장한 사용자 목록 전송
        sendUserList(roomId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String roomId = getRoomId(session);
            Map<String, String> msg = objectMapper.readValue(message.getPayload(), Map.class);

            String userName = (String) session.getAttributes().get("userName");

            if ("sdp".equals(msg.get("type"))) {
                // SDP 메시지 처리 (Offer 또는 Answer)
                handleSdpMessage(session, msg, roomId, userName);
            } else if ("ice".equals(msg.get("type"))) {
                // ICE 후보 처리
                handleIceCandidate(session, msg, roomId);
            } else if ("chat".equals(msg.get("type"))) {
                // 채팅 메시지 처리
                handleMessage(session, msg, roomId, userName);
            } else if ("video_start".equals(msg.get("type"))) {
                // 비디오 시작 처리
                handleVideoStart(session, roomId, userName);
            } else if ("video_end".equals(msg.get("type"))) {
                // 비디오 종료 처리
                handleVideoEnd(session, roomId, userName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.sendMessage(new TextMessage("An error occurred: " + e.getMessage()));
        }
    }

    /** /

     */
    public void handleSdpMessage(WebSocketSession session, Map<String, String> msg, String roomId, String userName) throws IOException {
        String sdpType = msg.get("sdpType");
        String sdpDescription = msg.get("sdpDescription");

        // SDP 메시지를 다른 피어에게 전송
        broadcastMessage(roomId, "sdp", Map.of(
                "sender", userName,
                "sdpType", sdpType,
                "sdpDescription", sdpDescription
        ));
    }

    public void handleIceCandidate(WebSocketSession session, Map<String, String> msg, String roomId) throws IOException {
        String candidate = msg.get("candidate");

        // ICE 후보 메시지를 다른 피어에게 전송
        broadcastMessage(roomId, "ice", Map.of(
                "candidate", candidate
        ));
    }

    private void handleMessage(WebSocketSession session, Map<String, String> msg, String roomId, String userName) throws IOException {
        // 채팅 메시지 처리
        String text = msg.get("text");

        // 메시지 엔티티 생성 및 저장
        Message chatMessage = new Message();
        chatMessage.setContent(text);
        chatMessage.setSenderId(session.getId());
        chatMessage.setTimestamp(LocalDateTime.now());

        Long classroomId = Long.parseLong(roomId);
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        chatMessage.setClassroom(classroom);
        messageRepository.save(chatMessage);

        // 모든 사용자에게 메시지 전송
        sendJsonMessage(roomId, "chat", String.format("%s: %s", userName, text));
    }

    private void handleVideoStart(WebSocketSession session, String roomId, String userName) throws Exception {
        // 비디오 시작 처리
        Video videoSession = new Video();
        videoSession.setUserId(session.getId());
        videoSession.setStartTime(LocalDateTime.now());

        Long classroomId = Long.parseLong(roomId);
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        videoSession.setClassroom(classroom);
        videoRepository.save(videoSession);

        // 비디오 시작 알림 메시지 전송
        String videoStartMessage = String.format("User %s has started a video chat.", userName);
        sendJsonMessage(roomId, "video_start", videoStartMessage);
    }

    private void handleVideoEnd(WebSocketSession session, String roomId, String userName) throws IOException {
        // 비디오 종료 처리
        Long classroomId = Long.parseLong(roomId);
        Video videoSession = videoRepository
                .findTopByUserIdAndClassroomIdOrderByStartTimeDesc(session.getId(), classroomId)
                .orElseThrow(() -> new RuntimeException("Video session not found"));

        videoSession.setEndTime(LocalDateTime.now());
        videoRepository.save(videoSession);

        // 비디오 종료 알림 메시지 전송
        String videoEndMessage = String.format("User %s has ended the video chat.", userName);
        sendJsonMessage(roomId, "video_end", videoEndMessage);
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = getRoomId(session);
        roomSessions.get(roomId).remove(session);

        // 사용자 이름 추출
        String userName = (String) session.getAttributes().get("userName");

        // 퇴장 메시지 생성 및 전송
        String exitMessage = String.format("User %s has left the room.", userName);
        Map<String, Object> message = Map.of(
                "type", "exit",
                "message", exitMessage
        );

        broadcastMessage(roomId, "exit", message);
        // 현재 입장한 사용자 목록 전송
        sendUserList(roomId);

        Long classroomId = Long.parseLong(roomId);

        Video videoSession = videoRepository
                .findTopByUserIdAndClassroomIdOrderByStartTimeDesc(session.getId(), classroomId)
                .orElse(null);

        if (videoSession != null) {
            // 비디오 세션이 존재할 경우에만 종료 시간 설정
            videoSession.setEndTime(LocalDateTime.now());
            videoRepository.save(videoSession);
        }
    }

    private void broadcastMessage(String roomId, String type, Map<String, Object> message) throws IOException {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                            "type", type,
                            "message", message
                    ))));
                }
            }
        }
    }

    private String getRoomId(WebSocketSession session) {
        return session.getUri().getPath().split("/")[2];
    }

    private void sendJsonMessage(String roomId, String type, String text) throws IOException {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        Map<String, String> message = new HashMap<>();
        message.put("type", type);
        message.put("text", text);
        String jsonMessage = objectMapper.writeValueAsString(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }

    private void sendUserList(String roomId) throws Exception {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        List<String> userIds = new ArrayList<>();
        for (WebSocketSession session : sessions) {
            userIds.add((String) session.getAttributes().get("userName"));
        }
        String userListMessage = "Current users in the room: " + String.join(", ", userIds);
        sendJsonMessage(roomId, "user_list", userListMessage);
    }
}


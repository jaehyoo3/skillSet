package com.skillset.livelectureservice.config;
import com.skillset.livelectureservice.domain.Classroom;
import com.skillset.livelectureservice.domain.Message;
import com.skillset.livelectureservice.domain.Video;
import com.skillset.livelectureservice.repository.ClassroomRepository;
import com.skillset.livelectureservice.repository.MessageRepository;
import com.skillset.livelectureservice.repository.VideoRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final Map<String, List<WebSocketSession>> roomSessions = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageRepository messageRepository;
    private final ClassroomRepository classroomRepository;
    private final VideoRepository videoRepository;

    public WebSocketHandler(MessageRepository messageRepository, ClassroomRepository classroomRepository, VideoRepository videoRepository) {
        this.messageRepository = messageRepository;
        this.classroomRepository = classroomRepository;
        this.videoRepository = videoRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomId(session);
        roomSessions.computeIfAbsent(roomId, k -> new ArrayList<>()).add(session);

        // 입장 메시지 생성 및 전송
        String entryMessage = String.format("User %s has entered the room.", session.getId());
        broadcastMessage(roomId, entryMessage);

        // 현재 입장한 사용자 목록 전송
        sendUserList(roomId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            System.out.println("Handling message from session: " + session.getId());
            System.out.println("Message payload: " + message.getPayload());

            String roomId = getRoomId(session);
            System.out.println("Room ID: " + roomId);

            List<WebSocketSession> sessions = roomSessions.get(roomId);

            Map<String, String> msg = objectMapper.readValue(message.getPayload(), Map.class);

            if ("chat".equals(msg.get("type"))) {
                System.out.println("Processing chat message");

                // 메시지 엔티티 생성 및 저장
                Message chatMessage = new Message();
                chatMessage.setContent(msg.get("text"));
                chatMessage.setSenderId(session.getId());
                chatMessage.setTimestamp(LocalDateTime.now());

                Long classroomId = Long.parseLong(roomId);
                Classroom classroom = classroomRepository.findById(classroomId)
                        .orElseThrow(() -> new RuntimeException("Classroom not found"));

                chatMessage.setClassroom(classroom);
                messageRepository.save(chatMessage);
            } else if ("video_start".equals(msg.get("type"))) {
                System.out.println("Starting video session");

                Video videoSession = new Video();
                videoSession.setUserId(session.getId());
                videoSession.setStartTime(LocalDateTime.now());

                Long classroomId = Long.parseLong(roomId);
                Classroom classroom = classroomRepository.findById(classroomId)
                        .orElseThrow(() -> new RuntimeException("Classroom not found"));

                videoSession.setClassroom(classroom);
                videoRepository.save(videoSession);
            } else if ("video_end".equals(msg.get("type"))) {
                System.out.println("Ending video session");

                Long classroomId = Long.parseLong(roomId);
                Video videoSession = videoRepository
                        .findTopByUserIdAndClassroomIdOrderByStartTimeDesc(session.getId(), classroomId)
                        .orElseThrow(() -> new RuntimeException("Video session not found"));

                videoSession.setEndTime(LocalDateTime.now());
                videoRepository.save(videoSession);
            }

            for (WebSocketSession webSocketSession : sessions) {
                if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                    webSocketSession.sendMessage(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.sendMessage(new TextMessage("An error occurred: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = getRoomId(session);
        roomSessions.get(roomId).remove(session);

        // 퇴장 메시지 생성 및 전송
        String exitMessage = String.format("User %s has left the room.", session.getId());
        broadcastMessage(roomId, exitMessage);

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

    private String getRoomId(WebSocketSession session) {
        return session.getUri().getPath().split("/")[2];
    }

    private void broadcastMessage(String roomId, String message) throws Exception {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    private void sendUserList(String roomId) throws Exception {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        List<String> userIds = new ArrayList<>();
        for (WebSocketSession session : sessions) {
            userIds.add(session.getId());
        }
        String userListMessage = "Current users in the room: " + String.join(", ", userIds);
        broadcastMessage(roomId, userListMessage);
    }
}

package com.skillset.livelectureservice.service;

import io.openvidu.java.client.*;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Service
public class OpenViduService {

    private static final Logger logger = Logger.getLogger(OpenViduService.class.getName());

    private OpenVidu openVidu;
    private ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    @Value("${openvidu.url}")
    private String openviduUrl;

    @Value("${openvidu.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        this.openVidu = new OpenVidu(openviduUrl, secret);
    }

    public String createSession(String customSessionId) {
        try {
            SessionProperties sessionProperties = new SessionProperties.Builder().customSessionId(customSessionId).build();
            Session session = openVidu.createSession(sessionProperties);
            sessionMap.put(session.getSessionId(), session);
            return session.getSessionId();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            logger.severe("Error creating session: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String generateToken(String sessionId) {
        try {
            Session session = sessionMap.get(sessionId);
            if (session == null) {
                throw new RuntimeException("Session not found");
            }
            ConnectionProperties connectionProperties = new ConnectionProperties.Builder().build();
            return session.createConnection(connectionProperties).getToken();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            logger.severe("Error generating token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
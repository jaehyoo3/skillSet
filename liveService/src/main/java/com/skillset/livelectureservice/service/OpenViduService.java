package com.skillset.livelectureservice.service;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.openvidu.java.client.ConnectionProperties;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.Session;
import io.openvidu.java.client.SessionProperties;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@Service
public class OpenViduService {

    private static final Logger logger = Logger.getLogger(OpenViduService.class.getName());

    private OpenVidu openVidu;
    private CloseableHttpClient httpClient;

    @Value("${openvidu.url}")
    private String openviduUrl;

    @Value("${openvidu.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        this.httpClient = HttpClients.createDefault();

        this.openVidu = new OpenVidu(openviduUrl, secret);
    }

    public String createSession(String customSessionId) {
        try {
            SessionProperties sessionProperties = new SessionProperties.Builder().customSessionId(customSessionId).build();
            Session session = openVidu.createSession(sessionProperties);
            return session.getSessionId();
        } catch (OpenViduHttpException | OpenViduJavaClientException e) {
            logger.severe("Error creating session: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String generateToken(String sessionId) {
        try {
            Session session = openVidu.getActiveSession(sessionId);
            if (session == null) {
                throw new RuntimeException("Session not found");
            }
            ConnectionProperties connectionProperties = new ConnectionProperties.Builder().build();
            return session.createConnection(connectionProperties).getToken();
        } catch (OpenViduHttpException | OpenViduJavaClientException e) {
            logger.severe("Error generating token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getActiveSessions() {
        List<String> sessionIds = new ArrayList<>();
        try {
            HttpGet request = new HttpGet(openviduUrl + "/api/sessions");
            request.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(("OPENVIDUAPP:" + secret).getBytes()));

            HttpResponse response = httpClient.execute(request);
            String responseString = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() == 200) {
                JsonObject responseJson = JsonParser.parseString(responseString).getAsJsonObject();
                JsonArray sessionsArray = responseJson.getAsJsonArray("content");

                for (JsonElement sessionElement : sessionsArray) {
                    JsonObject sessionJson = sessionElement.getAsJsonObject();
                    sessionIds.add(sessionJson.get("sessionId").getAsString());
                }
            } else {
                logger.severe("Failed to fetch active sessions: " + responseString);
            }
        } catch (IOException e) {
            logger.severe("Error fetching active sessions: " + e.getMessage());
            e.printStackTrace();
        }
        return sessionIds;
    }

    public void sendMessage(String sessionId, String message) {
        try {
            HttpPost request = new HttpPost(openviduUrl + "/api/signal");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(("OPENVIDUAPP:" + secret).getBytes()));

            JsonObject json = new JsonObject();
            json.addProperty("session", sessionId);
            json.addProperty("type", "chat");
            json.addProperty("data", message);
            request.setEntity(new StringEntity(json.toString(), "UTF-8"));

            HttpResponse response = httpClient.execute(request);
            String responseString = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() != 200) {
                logger.severe("Failed to send message: " + responseString);
            }
        } catch (IOException e) {
            logger.severe("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

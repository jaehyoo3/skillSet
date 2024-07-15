package com.skillset.livelectureservice.web;

import com.skillset.livelectureservice.service.OpenViduService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/openvidu")
public class OpenViduController {

    private static final Logger logger = Logger.getLogger(OpenViduController.class.getName());

    @Autowired
    private OpenViduService openViduService;

    @PostMapping("/sessions")
    public ResponseEntity<String> createSession(@RequestBody Map<String, Object> params) {
        String customSessionId = (String) params.get("customSessionId");
        logger.info("Creating session with customSessionId: " + customSessionId);
        String sessionId = openViduService.createSession(customSessionId);
        if (sessionId != null) {
            return ResponseEntity.ok(sessionId);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create session");
        }
    }

    @PostMapping("/sessions/{sessionId}/connections")
    public ResponseEntity<String> generateToken(@PathVariable String sessionId) {
        logger.info("Generating token for sessionId: " + sessionId);
        String token = openViduService.generateToken(sessionId);
        if (token != null) {
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate token");
        }
    }
}
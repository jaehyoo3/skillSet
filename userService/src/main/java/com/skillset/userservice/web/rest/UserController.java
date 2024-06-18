package com.skillset.userservice.web.rest;

import com.skillset.userservice.domain.User;
import com.skillset.userservice.service.UserService;
import com.skillset.userservice.service.dto.SignUpDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;

    private UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/join")
    public ResponseEntity<User> create(@RequestBody SignUpDTO sign) {
        return ResponseEntity.ok(userService.signUp(sign));

    }
}

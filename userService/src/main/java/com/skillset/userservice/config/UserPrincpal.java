package com.skillset.userservice.config;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class UserPrincpal extends User {

    private final Long userId;

    public UserPrincpal(com.skillset.userservice.domain.User user) {
        super(user.getEmail(), user.getPassword(),
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("WRITE")
                ));
        this.userId = user.getId();
    }

    public Long getUserId() {
        return userId;
    }
}
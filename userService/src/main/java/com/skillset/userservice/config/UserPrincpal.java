package com.skillset.userservice.config;

import com.skillset.userservice.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class UserPrincpal extends User {

    private final Long userId;

    public UserPrincpal(User user) {
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
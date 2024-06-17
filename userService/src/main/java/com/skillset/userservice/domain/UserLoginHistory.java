package com.skillset.userservice.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name= "user_login_history")

public class UserLoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "login_timestamp", updatable = false)
    private LocalDateTime loginTimestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        this.loginTimestamp = LocalDateTime.now();
    }
}

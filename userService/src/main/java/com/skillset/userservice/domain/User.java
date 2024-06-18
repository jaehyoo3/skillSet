package com.skillset.userservice.domain;

import com.skillset.userservice.domain.enums.ROLE;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

import java.util.List;

@Entity
@Getter
@Table(name= "`USER`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String password;

    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    private ROLE role;

    @Builder
    public User(String name, String email, String password, String phone, ROLE role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    @Override
    public Long getId() {
        return id;
    }

}

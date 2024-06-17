package com.skillset.userservice.domain;

import jakarta.persistence.*;
import lombok.Getter;


import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name= "`user`")
public class User extends AbstractAuditingEntity {

    @Id
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    @Override
    public Long getId() {
        return id;
    }
}

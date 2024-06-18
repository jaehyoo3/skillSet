package com.skillset.userservice.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name= "ROLE")
@Getter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String roleName;
}

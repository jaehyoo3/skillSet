package com.skillset.userservice.service.dto;

import lombok.Getter;

@Getter
public class SignUpDTO {
    private String name;
    private String email;
    private String password;
    private String phone;

    public SignUpDTO() {
    }

    public SignUpDTO(String name, String email, String password, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }
}

package com.skillset.userservice.service;

import com.skillset.userservice.domain.Role;
import com.skillset.userservice.domain.User;
import com.skillset.userservice.domain.enums.ROLE;
import com.skillset.userservice.repository.UserRepository;
import com.skillset.userservice.service.dto.SignUpDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.skillset.userservice.domain.enums.ROLE.USER;


@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signUp(SignUpDTO sign) {

        Optional<User> existingUser = userRepository.findByEmail(sign.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("이미 등록된 이메일입니다."); // 예외 처리 방식을 적절히 변경하세요.
        }

        String encryptedPassword = passwordEncoder.encode(sign.getPassword());

        User user = User.builder()
                .email(sign.getEmail())
                .name(sign.getName())
                .password(encryptedPassword)
                .phone(sign.getPhone())
                .role(USER)
                .build();
        return userRepository.save(user);
    }

}

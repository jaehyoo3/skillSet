package com.skillset.userservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.skillset.userservice.config.handler.Http401Handler;
import com.skillset.userservice.config.handler.Http403Handler;
import com.skillset.userservice.config.handler.LoginFailHandler;
import com.skillset.userservice.domain.User;
import com.skillset.userservice.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/favicon.ico", "/error");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 요청 경로에 대한 인가 설정
                .authorizeHttpRequests(auth -> auth
                        // 로그인 및 회원가입 페이지는 모든 사용자에게 허용
                        .requestMatchers("/login", "/join", "/h2-console/**").permitAll()
                        // /user 경로는 USER 및 ADMIN 역할을 가진 사용자에게 허용
                        .requestMatchers("/user").hasAnyRole("USER", "ADMIN")
                        // /admin 경로는 ADMIN 역할을 가진 사용자에게 허용
                        .requestMatchers("/admin").hasRole("ADMIN")
                        // 그 외의 모든 요청은 인증된 사용자에게만 허용
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정
                .formLogin(form -> form
                        // 커스텀 로그인 페이지 설정
                        .loginPage("/login")
                        // 로그인 처리 URL 설정
                        .loginProcessingUrl("/login")
                        // 로그인 폼의 사용자명 파라미터 이름 설정
                        .usernameParameter("email")
                        // 로그인 폼의 비밀번호 파라미터 이름 설정
                        .passwordParameter("password")
                        // 로그인 성공 시 리다이렉트할 기본 URL 설정
                        .defaultSuccessUrl("/")
                        // 로그인 실패 시 핸들러 설정
                        .failureHandler(new LoginFailHandler(new ObjectMapper()))
                )
                // 예외 처리 설정
                .exceptionHandling(e -> {
                    // 접근 거부 시 핸들러 설정 (403 에러)
                    e.accessDeniedHandler(new Http403Handler(new ObjectMapper()));
                    // 인증되지 않은 접근 시 핸들러 설정 (401 에러)
                    e.authenticationEntryPoint(new Http401Handler(new ObjectMapper()));
                })
                // 자동 로그인(remember-me) 설정
                .rememberMe(rm -> rm
                        // remember-me 파라미터 이름 설정
                        .rememberMeParameter("remember")
                        // 항상 remember-me 기능을 사용할지 여부 설정
                        .alwaysRemember(false)
                        // remember-me 토큰 유효기간 설정 (초 단위, 여기서는 30일)
                        .tokenValiditySeconds(2592000)
                )
                // CSRF 보호 비활성화
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)) // X-Frame-Options 설정
                )
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException(username +"을 찾을 수 없습니다."));

                return new UserPrincpal(user);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new SCryptPasswordEncoder(16,8,1,32,64);
    }
}
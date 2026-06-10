package com.wetube.user_service.service;

import com.wetube.user_service.dto.AuthDto;
import com.wetube.user_service.entity.User;
import com.wetube.user_service.exception.UserAlreadyExistsException;
import com.wetube.user_service.repository.UserRepository;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(AuthDto authDto) {
        User user = null;
        String username = authDto.getUsername();

        if (username.isEmpty()) {
            user = userRepository.findByEmail(authDto.getEmail()).orElseThrow();
        } else {
            user = userRepository.findByUsername(username).orElseThrow();
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), authDto.getPassword()));

        return user;
    }

    public User register(AuthDto authDto) {
        User user = new User();
        String username = authDto.getUsername();
        String email = authDto.getEmail();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(String.format("User With Username %s Already Exists", username));
        }

        if (email != null && userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(String.format("User With Email %s Already Exists", email));
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(authDto.getPassword()));

        return userRepository.save(user);
    }

    public static class TokenCookies {
        private String jwtToken;
        private Duration accessAge;
        private Duration refreshAge;

        public TokenCookies(String jwtToken, Duration accessAge, Duration refreshAge) {
            this.jwtToken = jwtToken;
            this.accessAge = accessAge;
            this.refreshAge = refreshAge;
        }

        public ResponseCookie getAccessToken() {
            return ResponseCookie.from("accessToken", jwtToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(accessAge)
                    .sameSite("Lax")
                    .build();
        }
        public ResponseCookie getRefreshToken() {
            return ResponseCookie.from("refreshToken", jwtToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/auth/refresh")
                    .maxAge(refreshAge)
                    .sameSite("Lax")
                    .build();
        }
    }
}

package com.wetube.user_service.controller;

import com.wetube.user_service.dto.AuthDto;
import com.wetube.user_service.dto.UserDto;
import com.wetube.user_service.entity.User;
import com.wetube.user_service.service.AuthService;
import com.wetube.user_service.service.JwtService;
import com.wetube.user_service.util.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserMapper userMapper;
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(UserMapper userMapper, AuthService authService, JwtService jwtService) {
        this.userMapper = userMapper;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody AuthDto authDto) {
        return ResponseEntity.ok(authService.register(authDto));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody AuthDto authDto) {
        User user = authService.login(authDto);
        String jwtToken = jwtService.generateToken(user);
        AuthService.TokenCookies cookies = new AuthService.TokenCookies(jwtToken, Duration.ofMinutes(30),
                Duration.ofDays(7));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.getAccessToken().toString())
                .header(HttpHeaders.SET_COOKIE, cookies.getRefreshToken().toString())
                .body(userMapper.toDto(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {
        try {
            jwtService.validateToken(refreshToken);

            String newAccessToken = jwtService.generateToken(refreshToken);
            ResponseCookie accessToken = ResponseCookie.from("accessToken", newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .partitioned(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(30))
                    .sameSite("None")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessToken.toString())
                    .body("Token Refreshed");
        } catch (Exception e) {
            ResponseCookie clearRefreshToken = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(true)
                    .partitioned(true)
                    .path("/auth/refresh")
                    .maxAge(0)
                    .sameSite("None")
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, clearRefreshToken.toString())
                    .body("Invalid Or Expired Refresh Token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        AuthService.TokenCookies cookies = new AuthService.TokenCookies("", Duration.ZERO, Duration.ZERO);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.getAccessToken().toString())
                .header(HttpHeaders.SET_COOKIE, cookies.getRefreshToken().toString())
                .body("Logged out");
    }
}

package com.wetube.user_service.controller;

import com.wetube.user_service.dto.UserDto;
import com.wetube.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@RequestHeader(value = "X-Username") String username) {
        System.out.println(username);
        return ResponseEntity.ok().body(userService.me(username));
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<UserDto>> all() {
        return ResponseEntity.ok().body(userService.all());
    }
}

package com.wetube.user_service.controller;

import com.wetube.user_service.dto.UserDto;
import com.wetube.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/@{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getUserSelf(@RequestHeader("X-Username") String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.all());
    }

    @GetMapping("/{channelId}/subscribers")
    public ResponseEntity<List<UserDto>> getChannelSubscribers(@PathVariable("channelId") String channelId) {
        return ResponseEntity.ok(userService.getChannelSubscribers(channelId));
    }

    @GetMapping("/{userId}/subscriptions")
    public ResponseEntity<List<UserDto>> getUserSubscriptions(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.getUserSubscriptions(userId));
    }

    @PostMapping("/subscribe/{channelId}")
    public ResponseEntity<String> subscribeToChannel(@PathVariable String channelId, @RequestHeader("X-User-Id") String subscriberId) {
        return ResponseEntity.ok(userService.subscribe(channelId, subscriberId));
    }

    @PostMapping("/unsubscribe/{channelId}")
    public ResponseEntity<String> unsubscribeFromChannel(@PathVariable String channelId, @RequestHeader("X-User-Id") String subscriberId) {
        return ResponseEntity.ok(userService.unsubscribe(channelId, subscriberId));
    }
}

package com.wetube.video_service.controller;

import com.wetube.video_service.dto.UserDto;
import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.service.VideoRatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/video-rating")
public class VideoRatingController {
    private final VideoRatingService videoRatingService;

    public VideoRatingController(VideoRatingService videoRatingService) {
        this.videoRatingService = videoRatingService;
    }

    @PostMapping("/video/like/{videoId}")
    public ResponseEntity<String> like(@PathVariable String videoId, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(videoRatingService.like(videoId, userId));
    }

    @PostMapping("/video/dislike/{videoId}")
    public ResponseEntity<String> dislike(@PathVariable("videoId") String videoId, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(videoRatingService.dislike(videoId, userId));
    }

    @GetMapping("/video/{videoId}/likes")
    public ResponseEntity<List<UserDto>> getVideoLikedUsers(@PathVariable("videoId") String videoId) {
        return ResponseEntity.ok(videoRatingService.getVideoLikedUsers(UUID.fromString(videoId)));
    }

    @GetMapping("/video/{videoId}/dislikes")
    public ResponseEntity<List<UserDto>> getVideoDislikedUsers(@PathVariable("videoId") String videoId) {
        return ResponseEntity.ok(videoRatingService.getVideoDislikedUsers(UUID.fromString(videoId)));
    }

    @GetMapping("/user/{userId}/likes")
    public ResponseEntity<List<VideoDto>> getUserLikedVideos(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(videoRatingService.getUserLikedVideos(UUID.fromString(userId)));
    }

    @GetMapping("/user/{userId}/dislikes")
    public ResponseEntity<List<VideoDto>> getUserDislikedVideos(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(videoRatingService.getUserDislikedVideos(UUID.fromString(userId)));
    }
}

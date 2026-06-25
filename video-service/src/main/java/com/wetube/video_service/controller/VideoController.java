package com.wetube.video_service.controller;

import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.service.VideoRatingService;
import com.wetube.video_service.service.VideoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/video")
public class VideoController {
    private final VideoService videoService;
    private final VideoRatingService videoRatingService;

    public VideoController(VideoService streamingService, VideoRatingService videoRatingService) {
        this.videoService = streamingService;
        this.videoRatingService = videoRatingService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<VideoDto>> getVideosByQuery(@RequestParam("query") String query) {
        List<VideoDto> videoDtos = videoService.searchByQuery(query);
        return ResponseEntity.ok(videoDtos);
    }

    @GetMapping("/{videoId}/metadata")
    public ResponseEntity<VideoDto> getVideoMetadata(@PathVariable String videoId) {
        VideoDto videoDto = videoService.getVideoMetadata(videoId);
        return ResponseEntity.ok(videoDto);
    }

    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> getMasterManifest(@PathVariable String videoId) {
        Resource manifest = videoService.getMasterManifest(videoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(manifest);
    }

    @GetMapping("/{videoId}/{quality}/playlist.m3u8")
    public ResponseEntity<Resource> getQualityManifest(@PathVariable String videoId, @PathVariable String quality) {
        Resource manifest = videoService.getQualityManifest(videoId, quality);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(manifest);
    }

    @GetMapping("/{videoId}/{quality}/{segment}")
    public ResponseEntity<Resource> getVideoSegment(@PathVariable String videoId, @PathVariable String quality, @PathVariable String segment) {
        Resource segmentResource = videoService.getVideoSegment(videoId, quality, segment);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/MP2T")
                .body(segmentResource);
    }

    @GetMapping("/likes")
    public List<VideoDto> getUserLikedVideos(@RequestParam("userId") String userId) {
        return videoRatingService.getUserLikedVideos(UUID.fromString(userId));
    }

    @GetMapping("/dislikes")
    public List<VideoDto> getUserDislikedVideos(@RequestParam("userId") String userId) {
        return videoRatingService.getUserDislikedVideos(UUID.fromString(userId));
    }

    @PostMapping("/like")
    public ResponseEntity<String> like(@RequestParam("videoId") String videoId, @RequestHeader("X-User-Id") String userId) throws Exception {
        return ResponseEntity.ok(videoRatingService.like(videoId, userId));
    }

    @PostMapping("/dislike")
    public ResponseEntity<String> dislike(@RequestParam("videoId") String videoId, @RequestHeader("X-User-Id") String userId) throws Exception {
        return ResponseEntity.ok(videoRatingService.dislike(videoId, userId));
    }

    @PostMapping("/upload")
    public ResponseEntity<VideoDto> upload(@RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader("X-User-Id") String userId) throws IOException {

        return ResponseEntity.ok(videoService.upload(file, title, description, userId));
    }
}

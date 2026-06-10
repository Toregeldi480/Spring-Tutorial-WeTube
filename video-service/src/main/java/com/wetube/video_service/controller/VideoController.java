package com.wetube.video_service.controller;

import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.service.VideoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/video")
public class VideoController {
    private final VideoService videoService;

    public VideoController(VideoService streamingService) {
        this.videoService = streamingService;
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<String> getVideosByKeyword(@PathVariable String keyword) {
        videoService.searchByKeyword(keyword);
        return ResponseEntity.ok("ok");
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

    @PostMapping("/upload")
    public ResponseEntity<VideoDto> upload(@RequestParam("file") MultipartFile file,
                                                @RequestParam("title") String title,
                                                @RequestParam(value = "description", required = false) String description,
                                                @RequestHeader("X-User-Id") String userId) throws IOException {

        return ResponseEntity.ok(videoService.upload(file, title, description, UUID.fromString(userId)));
    }
}

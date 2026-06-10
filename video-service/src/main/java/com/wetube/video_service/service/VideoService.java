package com.wetube.video_service.service;

import com.wetube.video_service.dto.TranscodingMessage;
import com.wetube.video_service.dto.TranscodingResultMessage;
import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.entity.Video;
import com.wetube.video_service.repository.VideoRepository;
import com.wetube.video_service.utils.VideoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class VideoService {
    @Value("${spring.application.root-location}")
    private String rootLocation;

    private final VideoMapper videoMapper;
    private final VideoRepository videoRepository;
    private final KafkaTemplate<String, TranscodingMessage> kafkaTemplate;

    public VideoService(VideoMapper videoMapper, VideoRepository videoRepository,
            KafkaTemplate<String, TranscodingMessage> kafkaTemplate) {
        this.videoMapper = videoMapper;
        this.videoRepository = videoRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void searchByKeyword(String keyword) {
        List<Video> videos = videoRepository.searchByKeyword(keyword);
        videos.forEach(s -> System.out.println(s.getTitle()));
    }

    @KafkaListener(topics = "video-transcoding-status", groupId = "transcoding-group", containerFactory = "kafkaListenerContainerFactory")
    public void updateStatus(TranscodingResultMessage message) {
        String videoId = message.getVideoId();
        String result = message.getResult();

        System.out.println("Message Received: " + videoId + " " + result);

        Video video = videoRepository.findById(UUID.fromString(videoId)).orElseThrow();
        video.setStatus(Video.VideoStatus.valueOf(result));
        videoRepository.save(video);
    }

    public Resource getMasterManifest(String videoId) {
        Video video = videoRepository.findById(UUID.fromString(videoId)).orElseThrow();

//        if (video.getVisibility() == Video.VideoVisibility.PRIVATE && video.getUserId() == UUID.fromString(userId)) {
//            throw new RuntimeException("You Do Not Have Access To This Video");
//        }

        if (video.getStatus() == Video.VideoStatus.UPLOADING) {
            throw new RuntimeException("Video Is Not Ready");
        }

        Path filePath = Paths.get(rootLocation + "/" + videoId + "/master.m3u8");
        return new FileSystemResource(filePath);
    }

    public Resource getQualityManifest(String videoId, String quality) {
        Path filePath = Paths.get(rootLocation + "/" + videoId + "/" + quality + "/playlist.m3u8");
        return new FileSystemResource(filePath);
    }

    public Resource getVideoSegment(String videoId, String quality, String segment) {
        Path filePath = Paths.get(rootLocation + "/" + videoId + "/" + quality + "/" + segment);
        return new FileSystemResource(filePath);
    }

    public VideoDto upload(MultipartFile file, String title, String description, UUID userId) throws IOException {
        Path storagePath = Paths.get(rootLocation);
        String originalFilename = file.getOriginalFilename();

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setUserId(userId);
        video.setOriginalFilename(originalFilename);
        video.setFileSize(file.getSize());
        video = videoRepository.save(video);

        String videoId = video.getId().toString();

        Path videoDir = storagePath.resolve(videoId);
        Files.createDirectory(videoDir);

        Path originalFilePath = videoDir.resolve(originalFilename);
        Files.copy(file.getInputStream(), originalFilePath,
                StandardCopyOption.REPLACE_EXISTING);

        TranscodingMessage message = new TranscodingMessage();
        message.setVideoId(videoId);
        message.setOriginalFilePath(originalFilePath.toString());
        message.setOutputDirectory(videoDir.toString());

        System.out.println("Message Sent: " + message.getVideoId());
        kafkaTemplate.send("video-transcoding", message);

        return videoMapper.toDto(video);
    }
}

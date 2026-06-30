package com.wetube.video_service.service;

import com.wetube.video_service.dto.TranscodingRequestMessage;
import com.wetube.video_service.dto.TranscodingResultMessage;
import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.entity.Video;
import com.wetube.video_service.exception.ResourceNotReadyException;
import com.wetube.video_service.repository.VideoRepository;
import com.wetube.video_service.util.VideoMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VideoService {
    @Value("${spring.application.root-location}")
    private String rootLocation;

    private final VideoMapper videoMapper;
    private final VideoRepository videoRepository;
    private final KafkaTemplate<String, TranscodingRequestMessage> kafkaTemplate;

    public VideoService(VideoMapper videoMapper, VideoRepository videoRepository,
            KafkaTemplate<String, TranscodingRequestMessage> kafkaTemplate) {
        this.videoMapper = videoMapper;
        this.videoRepository = videoRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "video-transcoding-result", groupId = "transcoding-group", containerFactory = "kafkaListenerContainerFactory")
    public void updateVideoTranscodingResult(TranscodingResultMessage message) {
        String videoId = message.getVideoId();
        String status = message.getStatus();
        Long duration = message.getDuration();

        System.out.println("Message Received: " + videoId + " " + status + " " + duration);

        Video video = videoRepository.findById(UUID.fromString(videoId)).orElseThrow();
        video.setStatus(Video.VideoStatus.valueOf(status));
        video.setDuration(duration);
        videoRepository.save(video);
    }

    public List<VideoDto> searchByQuery(String query) {
        List<Video> videos = videoRepository.findByQuery(query);
        List<VideoDto> videoDtos = new ArrayList<>();
        videos.forEach(v -> videoDtos.add(videoMapper.toDto(v)));
        return videoDtos;
    }

    public VideoDto getVideoMetadata(String videoId) {
        Video video = videoRepository.findById(UUID.fromString(videoId)).orElseThrow();
        return videoMapper.toDto(video);
    }

    public Resource getMasterManifest(String videoId) {
        Video video = videoRepository.findById(UUID.fromString(videoId)).orElseThrow();

        if (video.getStatus() == Video.VideoStatus.UPLOADING) {
            throw new ResourceNotReadyException("Video With ID [" + videoId + "] Is Not Ready");
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

    public VideoDto upload(MultipartFile file, String title, String description, String userId) throws IOException {
        Path storagePath = Paths.get(rootLocation);
        String originalFilename = file.getOriginalFilename();

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setUserId(UUID.fromString(userId));
        video.setOriginalFilename(originalFilename);
        video.setFileSize(file.getSize());
        video = videoRepository.save(video);

        String videoId = video.getId().toString();

        Path videoDir = storagePath.resolve(videoId);
        Files.createDirectory(videoDir);

        Path originalFilePath = videoDir.resolve(originalFilename);
        Files.copy(file.getInputStream(), originalFilePath,
                StandardCopyOption.REPLACE_EXISTING);

        TranscodingRequestMessage message = new TranscodingRequestMessage();
        message.setVideoId(videoId);
        message.setOriginalFilePath(originalFilePath.toString());
        message.setOutputDirectory(videoDir.toString());

        System.out.println("Message Sent: " + message.getVideoId());
        kafkaTemplate.send("video-transcoding-request", message);

        return videoMapper.toDto(video);
    }
}

package com.wetube.transcoding_service.service;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.wetube.transcoding_service.dto.TranscodingMessage;
import com.wetube.transcoding_service.dto.TranscodingResultMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class TranscodingService {
    private final KafkaTemplate<String, TranscodingResultMessage> kafkaTemplate;

    public TranscodingService(KafkaTemplate<String, TranscodingResultMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "video-transcoding", groupId = "transcoding-group", containerFactory = "kafkaListenerContainerFactory")
    public void processVideo(TranscodingMessage message) {
        String videoId = message.getVideoId();
        Path outputDir = Paths.get(message.getOutputDirectory());

        System.out.println("Message Received: " + videoId);

        try {
            encodeVideo(message.getOriginalFilePath(), outputDir);
            createMasterPlaylist(outputDir);
            updateStatus(videoId, "READY");
        } catch (Exception e) {
            updateStatus(videoId, "FAILED");
            throw new RuntimeException(e);
        }
    }

    public void encodeVideo(String videoPath, Path outputDir) throws Exception {
        for (int i = 0; i < 3; i++) {
            Files.createDirectories(outputDir.resolve("stream_" + i));
        }

        int[][] qualities = {
                {  854,  480, 1500 },
                { 1280,  720, 3000 },
                { 1920, 1080, 4500 }
        };

        for (int i = 0; i < 3; i++) {
            int width = qualities[i][0];
            int height = qualities[i][1];
            int bitrateKbps = qualities[i][2];

            FFmpeg.atPath()
                    .addInput(UrlInput.fromPath(Paths.get(videoPath)))
                    .addArguments("-vf", "scale=" + width + ":" + height)
                    .addArguments("-c:v", "libx264")
                    .addArguments("-preset", "fast")
                    .addArguments("-b:v", bitrateKbps + "k")
                    .addArguments("-c:a", "aac")
                    .addArguments("-movflags", "faststart")
                    .addArguments("-f", "hls")
                    .addArguments("-hls_list_size", "0")
                    .addArguments("-hls_time", "10")
                    .addArgument("-hls_segment_filename")
                    .addArgument(outputDir.resolve("stream_" + i).resolve("segment%03d.ts").toString())
                    .addArgument(outputDir.resolve("stream_" + i).resolve("playlist.m3u8").toString())
                    .execute();
        }
    }

    private void createMasterPlaylist(Path outputDir) throws Exception {
        StringBuilder master = new StringBuilder();
        master.append("#EXTM3U\n");
        master.append("#EXT-X-VERSION:3\n\n");

        master.append("#EXT-X-STREAM-INF:BANDWIDTH=5000000,RESOLUTION=1920x1080\n");
        master.append("stream_0/playlist.m3u8\n\n");

        master.append("#EXT-X-STREAM-INF:BANDWIDTH=2800000,RESOLUTION=1280x720\n");
        master.append("stream_1/playlist.m3u8\n\n");

        master.append("#EXT-X-STREAM-INF:BANDWIDTH=1400000,RESOLUTION=854x480\n");
        master.append("stream_2/playlist.m3u8\n");

        Files.writeString(outputDir.resolve("master.m3u8"), master.toString());
    }

    private void updateStatus(String videoId, String status) {
        TranscodingResultMessage message = new TranscodingResultMessage();
        message.setVideoId(videoId);
        message.setResult(status);

        System.out.println("Message Sent: " + videoId + " " + status);
        kafkaTemplate.send("video-transcoding-status", message);
    }
}

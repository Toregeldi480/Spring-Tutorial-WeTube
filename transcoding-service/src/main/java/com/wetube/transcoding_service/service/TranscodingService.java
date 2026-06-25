package com.wetube.transcoding_service.service;

import com.github.kokorin.jaffree.ffmpeg.*;
import com.wetube.transcoding_service.dto.TranscodingRequestMessage;
import com.wetube.transcoding_service.dto.TranscodingResultMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TranscodingService {
    private final KafkaTemplate<String, TranscodingResultMessage> kafkaTemplate;

    public TranscodingService(KafkaTemplate<String, TranscodingResultMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "video-transcoding-request", groupId = "transcoding-group", containerFactory = "kafkaListenerContainerFactory")
    public void processVideo(TranscodingRequestMessage message) {
        String videoId = message.getVideoId();
        Path originalFilePath = Paths.get(message.getOriginalFilePath());
        Path outputDir = Paths.get(message.getOutputDirectory());

        System.out.println("Message Received: " + videoId);

        try {
            encodeVideo(originalFilePath, outputDir);
            createMasterPlaylist(outputDir);
            updateVideoInfo(videoId, "READY", getVideoDuration(originalFilePath));
        } catch (Exception e) {
            updateVideoInfo(videoId, "FAILED", 0L);
            throw new RuntimeException(e);
        }
    }

    public void encodeVideo(Path videoPath, Path outputDir) throws Exception {
        for (int i = 0; i < 3; i++) {
            Files.createDirectories(outputDir.resolve("stream_" + i));
        }

        int[][] qualities = {
                { 854, 480, 1400 },
                { 1280, 720, 2800 },
                { 1920, 1080, 5000 }
        };

        for (int i = 0; i < 3; i++) {
            int width = qualities[i][0];
            int height = qualities[i][1];
            int bitrateKbps = qualities[i][2];

            FFmpeg.atPath()
                    .addInput(UrlInput.fromPath(videoPath))
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

    private Long getVideoDuration(Path videoPath) {
        final AtomicLong duration = new AtomicLong();
        FFmpeg.atPath()
                .addInput(UrlInput.fromPath(videoPath))
                .addOutput(new NullOutput())
                .setProgressListener(new ProgressListener() {
                    @Override
                    public void onProgress(FFmpegProgress progress) {
                        duration.set(progress.getTime(TimeUnit.SECONDS));
                    }
                })
                .execute();

        return duration.get();
    }

    private void updateVideoInfo(String videoId, String status, Long duration) {
        TranscodingResultMessage message = new TranscodingResultMessage();
        message.setVideoId(videoId);
        message.setStatus(status);
        message.setDuration(duration);

        System.out.println("Message Sent: " + videoId + " " + status + " " + duration);
        kafkaTemplate.send("video-transcoding-result", message);
    }
}

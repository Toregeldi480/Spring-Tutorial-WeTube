package com.wetube.transcoding_service.service;

import com.wetube.transcoding_service.dto.TranscodingMessage;
import com.wetube.transcoding_service.dto.TranscodingResultMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

        try {
            Path videoPath = Paths.get(message.getOriginalFilePath());
            Path outputDir = Paths.get(message.getOutputDirectory());

            System.out.println("Message Received: " + videoId);

            generateHLSStream(videoPath, outputDir, videoId);
            updateStatus(videoId, "READY");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateStatus(String videoId, String status) {
        TranscodingResultMessage message = new TranscodingResultMessage();
        message.setVideoId(videoId);
        message.setResult(status);

        System.out.println("Message Sent: " + videoId + " " + status);
        kafkaTemplate.send("video-transcoding-status", message);
    }

    private void generateHLSStream(Path videoPath, Path outputDir, String videoId) throws Exception {
        boolean hasAudio = checkIfVideoHasAudio(videoPath);

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(videoPath.toString());
        command.add("-filter_complex");
        command.add(
                "[0:v]split=3[v1][v2][v3];[v1]scale=w=1920:h=1080[v1out];[v2]scale=w=1280:h=720[v2out];[v3]scale=w=854:h=480[v3out]");

        command.addAll(Arrays.asList(
                "-map", "[v1out]",
                "-c:v:0", "libx264",
                "-b:v:0", "5000k",
                "-maxrate:v:0", "7500k",
                "-bufsize:v:0", "10000k",
                "-profile:v:0", "high",
                "-preset:v:0", "medium",
                "-bf:v:0", "3",
                "-g:v:0", "60",
                "-sc_threshold:v:0", "40",
                "-r:v:0", "60"));
        
        command.addAll(Arrays.asList(
                "-map", "[v2out]",
                "-c:v:1", "libx264",
                "-b:v:1", "2800k",
                "-maxrate:v:1", "4200k",
                "-bufsize:v:1", "5600k",
                "-profile:v:1", "main",
                "-preset:v:1", "medium",
                "-bf:v:1", "3",
                "-g:v:1", "60",
                "-sc_threshold:v:1", "40",
                "-r:v:1", "60"));
        
        command.addAll(Arrays.asList(
                "-map", "[v3out]",
                "-c:v:2", "libx264",
                "-b:v:2", "1400k",
                "-maxrate:v:2", "2100k",
                "-bufsize:v:2", "2800k",
                "-profile:v:2", "main",
                "-preset:v:2", "medium",
                "-bf:v:2", "3",
                "-g:v:2", "30",
                "-sc_threshold:v:2", "40",
                "-r:v:2", "30"));

        if (hasAudio) {
            command.addAll(Arrays.asList(
                "-map", "a:0",
                "-c:a:0", "aac",
                "-b:a:0", "192k",
                "-ac:a:0", "2",
                "-ar:a:0", "48000"));
    
            command.addAll(Arrays.asList(
                    "-map", "a:0",
                    "-c:a:1", "aac",
                    "-b:a:1", "128k",
                    "-ac:a:1", "2",
                    "-ar:a:1", "48000"));
            
            command.addAll(Arrays.asList(
                    "-map", "a:0",
                    "-c:a:2", "aac",
                    "-b:a:2", "96k",
                    "-ac:a:2", "2",
                    "-ar:a:2", "44100"));
        }

        command.addAll(Arrays.asList(
                "-f", "hls",
                "-hls_time", "10",
                "-hls_playlist_type", "vod",
                "-hls_flags", "independent_segments",
                "-hls_segment_type", "mpegts",
                "-hls_segment_filename", outputDir.toString() + "/stream_%v/segment%03d.ts",
                "-master_pl_name", "master.m3u8"));

        if (hasAudio)
            command.addAll(Arrays.asList("-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2"));
        else
            command.addAll(Arrays.asList("-var_stream_map", "v:0 v:1 v:2"));

        command.add(outputDir.toString() + "/stream_%v/playlist.m3u8");

        ProcessBuilder pb = new ProcessBuilder(command.toArray(new String[0]));
        Process process = pb.start();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg Failed With Exit Code: " + exitCode);
        }
    }

    private boolean checkIfVideoHasAudio(Path videoPath) throws Exception {
        String[] command = {
                "ffprobe",
                "-v", "error",
                "-select_streams", "a:0",
                "-show_entries", "stream=codec_type",
                "-of", "csv=p=0",
                videoPath.toString()
        };

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String result = reader.readLine();
        process.waitFor();

        return result != null && result.contains("audio");
    }
}

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
import java.util.concurrent.TimeUnit;

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
            updateStatus(videoId, "FAILED");
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

        for (int i = 0; i < 3; i++) {
            Files.createDirectories(outputDir.resolve("stream_" + i));
        }

        try {
            System.out.println("Starting 1080p variant...");
            encodeVariant(videoPath, outputDir, 0, 1920, 1080, "5000k", "7500k", "10000k", "60",
                    hasAudio ? "192k" : null);

            System.out.println("Starting 720p variant...");
            encodeVariant(videoPath, outputDir, 1, 1280, 720, "2800k", "4200k", "5600k", "60",
                    hasAudio ? "128k" : null);

            System.out.println("Starting 480p variant...");
            encodeVariant(videoPath, outputDir, 2, 854, 480, "1400k", "2100k", "2800k", "30", hasAudio ? "96k" : null);
        } catch (Exception e) {
            updateStatus(videoId, "FAILED");
            throw new RuntimeException(e);
        }

        createMasterPlaylist(outputDir, hasAudio);
        updateStatus(videoId, "READY");

        System.out.println("All transcoding finished for video: " + videoId);
    }

    private void encodeVariant(Path videoPath, Path outputDir, int index,
            int width, int height, String bitrate,
            String maxrate, String bufsize, String framerate,
            String audioBitrate) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(videoPath.toString());
        command.add("-vf");
        command.add(String.format("scale=%d:%d", width, height));

        command.add("-c:v");
        command.add("libx264");
        command.add("-b:v");
        command.add(bitrate);
        command.add("-maxrate");
        command.add(maxrate);
        command.add("-bufsize");
        command.add(bufsize);
        command.add("-preset");
        command.add("fast");
        command.add("-r");
        command.add(framerate);
        command.add("-g");
        command.add(framerate);

        if (audioBitrate != null) {
            command.add("-map");
            command.add("0:v:0");
            command.add("-map");
            command.add("0:a:0");
            command.add("-c:a");
            command.add("aac");
            command.add("-b:a");
            command.add(audioBitrate);
            command.add("-ac");
            command.add("2");
            command.add("-ar");
            command.add("48000");
        }

        command.add("-f");
        command.add("hls");
        command.add("-hls_time");
        command.add("10");
        command.add("-hls_list_size");
        command.add("0");
        command.add("-hls_segment_filename");
        command.add(outputDir.resolve("stream_" + index).resolve("segment%03d.ts").toString());
        command.add(outputDir.resolve("stream_" + index).resolve("playlist.m3u8").toString());

        System.out.println("Variant " + index + " command: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        Thread reader = null;

        reader = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println("[" + index + "] " + line);
                }
            } catch (IOException e) {
            }
        });
        reader.start();

        boolean finished = process.waitFor(15, TimeUnit.MINUTES);

        if (!finished) {
            System.err.println("FFmpeg variant " + index + " timed out!");
            return;
        }

        int exitCode = process.exitValue();

        if (exitCode != 0) {
            throw new RuntimeException("Variant " + index + " failed with exit code: " + exitCode);
        }

        System.out.println("Variant " + index + " completed successfully");

        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
        if (reader != null && reader.isAlive()) {
            reader.interrupt();
        }
    }

    private void createMasterPlaylist(Path outputDir, boolean hasAudio) throws Exception {
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

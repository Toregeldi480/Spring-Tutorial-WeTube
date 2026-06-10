package com.wetube.video_service.utils;

import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.entity.Video;
import org.springframework.stereotype.Component;

@Component
public class VideoMapper {
    public VideoDto toDto(Video video) {
        VideoDto dto = new VideoDto();
        dto.setId(video.getId().toString());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setUserId(video.getUserId().toString());
        dto.setOriginalFilename(video.getOriginalFilename());
        dto.setFileSize(video.getFileSize());
        dto.setStatus(video.getStatus().name());

        return dto;
    }
}

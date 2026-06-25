package com.wetube.video_service.utils;

import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.entity.Video;
import org.springframework.stereotype.Component;

@Component
public class VideoMapper {
    public VideoDto toDto(Video video) {
        VideoDto dto = new VideoDto();
        dto.setId(video.getId().toString());
        dto.setUserId(video.getUserId().toString());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setOriginalFilename(video.getOriginalFilename());
        dto.setDuration(video.getDuration());
        dto.setFileSize(video.getFileSize());
        dto.setLikes(video.getLikes());
        dto.setDislikes(video.getDislikes());
        dto.setVisibility(video.getVisibility().name());
        dto.setStatus(video.getStatus().name());

        return dto;
    }
}

package com.wetube.video_service.service;

import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.entity.Video;
import com.wetube.video_service.entity.VideoLike;
import com.wetube.video_service.repository.VideoLikeRepository;
import com.wetube.video_service.repository.VideoRepository;
import com.wetube.video_service.utils.VideoMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class VideoLikeService {
    private final VideoMapper videoMapper;
    private final VideoRepository videoRepository;
    private final VideoLikeRepository videoLikeRepository;

    public VideoLikeService(VideoMapper videoMapper, VideoRepository videoRepository, VideoLikeRepository videoLikeRepository) {
        this.videoMapper = videoMapper;
        this.videoRepository = videoRepository;
        this.videoLikeRepository = videoLikeRepository;
    }

    public String like(String videoId, String userId) throws Exception {
        UUID videoUUID = UUID.fromString(videoId);
        UUID userUUID = UUID.fromString(userId);
        Video video = videoRepository.findById(videoUUID).orElseThrow();
        Long likeCount = video.getLikes();

        if (videoLikeRepository.existsByVideoIdAndUserId(videoUUID, userUUID)) {
            video.setLikes(likeCount - 1);
            videoLikeRepository.removeByVideoIdAndUserId(videoUUID, userUUID);
            return "Removed like from video with ID: " + videoId;
        } else {
            video.setLikes(likeCount + 1);
            videoLikeRepository.save(new VideoLike(videoUUID, userUUID));
            return "Liked video with ID: " + videoId;
        }
    }

    public List<VideoDto> getUserLikedVideos(UUID userId) {
        List<VideoLike> likedVideos = videoLikeRepository.findByUserId(userId);

        return likedVideos.stream().map(likedVideo -> {
            Video video = likedVideo.getVideo();
            return videoMapper.toDto(video);
        }).collect(Collectors.toList());
    }
}

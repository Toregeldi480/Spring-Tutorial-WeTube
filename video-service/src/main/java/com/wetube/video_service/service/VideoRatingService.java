package com.wetube.video_service.service;

import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.entity.Video;
import com.wetube.video_service.entity.VideoRating;
import com.wetube.video_service.repository.VideoRatingRepository;
import com.wetube.video_service.repository.VideoRepository;
import com.wetube.video_service.utils.VideoMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class VideoRatingService {
    private final VideoMapper videoMapper;
    private final VideoRepository videoRepository;
    private final VideoRatingRepository videoRatingRepository;

    public VideoRatingService(VideoMapper videoMapper, VideoRepository videoRepository, VideoRatingRepository videoRatingRepository) {
        this.videoMapper = videoMapper;
        this.videoRepository = videoRepository;
        this.videoRatingRepository = videoRatingRepository;
    }

    public String like(String videoId, String userId) {
        UUID videoUUID = UUID.fromString(videoId);
        UUID userUUID = UUID.fromString(userId);
        Video video = videoRepository.findById(videoUUID).orElseThrow();
        Long likes = video.getLikes();
        Long dislikes = video.getDislikes();

        Optional<VideoRating> videoRating = videoRatingRepository.findByVideoIdAndUserId(videoUUID, userUUID);

        if (videoRating.isEmpty()) {
            video.setLikes(likes + 1);
            videoRatingRepository.save(new VideoRating(videoUUID, userUUID, true));
            return "Liked video with ID: " + videoId;
        }

        if (videoRating.get().getIsLiked()) {
            video.setLikes(likes - 1);
            videoRatingRepository.removeByVideoIdAndUserId(videoUUID, userUUID);
            return "Removed like from video with ID: " + videoId;
        } else {
            video.setDislikes(dislikes - 1);
            video.setLikes(likes + 1);
            videoRatingRepository.save(new VideoRating(videoUUID, userUUID, true));
            return "Removed dislike and liked video with ID: " + videoId;
        }
    }

    public String dislike(String videoId, String userId) {
        UUID videoUUID = UUID.fromString(videoId);
        UUID userUUID = UUID.fromString(userId);
        Video video = videoRepository.findById(videoUUID).orElseThrow();
        Long likes = video.getLikes();
        Long dislikes = video.getDislikes();

        Optional<VideoRating> videoRating = videoRatingRepository.findByVideoIdAndUserId(videoUUID, userUUID);

        if (videoRating.isEmpty()) {
            video.setDislikes(dislikes + 1);
            videoRatingRepository.save(new VideoRating(videoUUID, userUUID, false));
            return "Disliked video with ID: " + videoId;
        }

        if (!videoRating.get().getIsLiked()) {
            video.setDislikes(dislikes - 1);
            videoRatingRepository.removeByVideoIdAndUserId(videoUUID, userUUID);
            return "Removed dislike from video with ID: " + videoId;
        } else {
            video.setDislikes(dislikes + 1);
            video.setLikes(likes - 1);
            videoRatingRepository.save(new VideoRating(videoUUID, userUUID, false));
            return "Removed like and disliked video with ID: " + videoId;
        }
    }

    public List<VideoDto> getUserLikedVideos(UUID userId) {
        List<VideoRating> likedVideos = videoRatingRepository.findLikesByUserId(userId);

        return likedVideos.stream().map(likedVideo -> {
            Video video = likedVideo.getVideo();
            return videoMapper.toDto(video);
        }).collect(Collectors.toList());
    }

    public List<VideoDto> getUserDislikedVideos(UUID userId) {
        List<VideoRating> likedVideos = videoRatingRepository.findDislikesByUserId(userId);

        return likedVideos.stream().map(likedVideo -> {
            Video video = likedVideo.getVideo();
            return videoMapper.toDto(video);
        }).collect(Collectors.toList());
    }
}

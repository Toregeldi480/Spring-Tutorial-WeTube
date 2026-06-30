package com.wetube.video_service.service;

import com.wetube.video_service.dto.UserDto;
import com.wetube.video_service.dto.VideoDto;
import com.wetube.video_service.entity.User;
import com.wetube.video_service.entity.Video;
import com.wetube.video_service.entity.VideoRating;
import com.wetube.video_service.repository.VideoRatingRepository;
import com.wetube.video_service.repository.VideoRepository;
import com.wetube.video_service.util.UserMapper;
import com.wetube.video_service.util.VideoMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class VideoRatingService {
    private final UserMapper userMapper;
    private final VideoMapper videoMapper;
    private final VideoRepository videoRepository;
    private final VideoRatingRepository videoRatingRepository;

    public VideoRatingService(UserMapper userMapper, VideoMapper videoMapper, VideoRepository videoRepository, VideoRatingRepository videoRatingRepository) {
        this.userMapper = userMapper;
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
            return "Liked Video With ID: " + videoId;
        }

        if (videoRating.get().getIsLiked()) {
            video.setLikes(likes - 1);
            videoRatingRepository.deleteByVideoIdAndUserId(videoUUID, userUUID);
            return "Removed Like From Video With ID: " + videoId;
        } else {
            video.setDislikes(dislikes - 1);
            video.setLikes(likes + 1);
            videoRatingRepository.save(new VideoRating(videoUUID, userUUID, true));
            return "Removed Dislike And Liked Video With ID: " + videoId;
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
            return "Disliked Video With ID: " + videoId;
        }

        if (!videoRating.get().getIsLiked()) {
            video.setDislikes(dislikes - 1);
            videoRatingRepository.deleteByVideoIdAndUserId(videoUUID, userUUID);
            return "Removed Dislike From Video With ID: " + videoId;
        } else {
            video.setDislikes(dislikes + 1);
            video.setLikes(likes - 1);
            videoRatingRepository.save(new VideoRating(videoUUID, userUUID, false));
            return "Removed Like And Disliked Video With ID: " + videoId;
        }
    }

    public List<UserDto> getVideoLikedUsers(UUID videoId) {
        List<VideoRating> likedUsers = videoRatingRepository.findLikesByVideoId(videoId);

        return likedUsers.stream().map(user ->
                userMapper.toDto(user.getUser())
        ).collect(Collectors.toList());
    }

    public List<UserDto> getVideoDislikedUsers(UUID videoId) {
        List<VideoRating> likedUsers = videoRatingRepository.findDislikesByVideoId(videoId);

        return likedUsers.stream().map(user ->
                userMapper.toDto(user.getUser())
        ).collect(Collectors.toList());
    }

    public List<VideoDto> getUserLikedVideos(UUID userId) {
        List<VideoRating> likedVideos = videoRatingRepository.findLikesByUserId(userId);

        return likedVideos.stream().map(video ->
            videoMapper.toDto(video.getVideo())
        ).collect(Collectors.toList());
    }

    public List<VideoDto> getUserDislikedVideos(UUID userId) {
        List<VideoRating> likedVideos = videoRatingRepository.findDislikesByUserId(userId);

        return likedVideos.stream().map(video ->
                videoMapper.toDto(video.getVideo())
        ).collect(Collectors.toList());
    }
}

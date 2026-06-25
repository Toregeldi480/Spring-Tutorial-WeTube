package com.wetube.video_service.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Table(name = "video_ratings")
@Entity
@IdClass(VideoRatingId.class)
public class VideoRating {
    public VideoRating() {

    }

    public VideoRating(UUID videoId, UUID userId, Boolean isLiked) {
        this.videoId = videoId;
        this.userId = userId;
        this.isLiked = isLiked;
    }

    @Id
    @Column(name = "video_id")
    private UUID videoId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "is_liked")
    private Boolean isLiked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", insertable = false, updatable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    public UUID getVideoId() {
        return videoId;
    }

    public void setVideoId(UUID videoId) {
        this.videoId = videoId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Boolean getIsLiked() {
        return this.isLiked;
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

package com.wetube.video_service.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Table(name = "videos")
@Entity
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    @Column(nullable = false)
    private String originalFilename;

    private Long duration;

    private Long fileSize;

    private Long likes = 0L;

    @Column(columnDefinition = "tsvector", insertable = false, updatable = false)
    private String tsvectorColumn;

    @Enumerated(EnumType.STRING)
    private VideoStatus status = VideoStatus.UPLOADING;

    @Enumerated(EnumType.STRING)
    private VideoVisibility visibility = VideoVisibility.PUBLIC;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }

    public VideoVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(VideoVisibility visibility) {
        this.visibility = visibility;
    }

    public enum VideoStatus {
        UPLOADING, READY, FAILED
    }

    public enum VideoVisibility {
        PUBLIC, PRIVATE
    }
}

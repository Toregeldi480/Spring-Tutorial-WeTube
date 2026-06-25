package com.wetube.video_service.entity;

import jakarta.persistence.Entity;

import java.io.Serializable;
import java.util.UUID;

public class VideoRatingId implements Serializable {
  private UUID videoId;
  private UUID userId;

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
}

package com.wetube.video_service.dto;

public class TranscodingResultMessage {
    private String videoId;
    private String result;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}

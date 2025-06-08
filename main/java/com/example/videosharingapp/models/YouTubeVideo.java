/**
 * Author: Nathan Addison
 * For BIT603 Assessment 3
 */


package com.example.videosharingapp.models;

import com.google.gson.annotations.SerializedName;

public class YouTubeVideo {
    @SerializedName("id")
    private VideoId id;

    @SerializedName("snippet")
    private VideoSnippet snippet;

    public VideoId getId() { return id; }
    public void setId(VideoId id) { this.id = id; }

    public VideoSnippet getSnippet() { return snippet; }
    public void setSnippet(VideoSnippet snippet) { this.snippet = snippet; }

    // Helper method to get video ID as string
    public String getVideoId() {
        if (id != null) {
            return id.getVideoId();
        }
        return null;
    }

    public static class VideoId {
        @SerializedName("kind")
        private String kind;

        @SerializedName("videoId")
        private String videoId;

        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }

        public String getVideoId() { return videoId; }
        public void setVideoId(String videoId) { this.videoId = videoId; }
    }

    public static class VideoSnippet {
        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("channelId")
        private String channelId;

        @SerializedName("channelTitle")
        private String channelTitle;

        @SerializedName("publishedAt")
        private String publishedAt;

        @SerializedName("thumbnails")
        private YouTubeChannel.Thumbnails thumbnails;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getChannelId() { return channelId; }
        public void setChannelId(String channelId) { this.channelId = channelId; }

        public String getChannelTitle() { return channelTitle; }
        public void setChannelTitle(String channelTitle) { this.channelTitle = channelTitle; }

        public String getPublishedAt() { return publishedAt; }
        public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

        public YouTubeChannel.Thumbnails getThumbnails() { return thumbnails; }
        public void setThumbnails(YouTubeChannel.Thumbnails thumbnails) { this.thumbnails = thumbnails; }
    }
}

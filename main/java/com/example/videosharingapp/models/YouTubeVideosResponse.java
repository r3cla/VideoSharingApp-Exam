package com.example.videosharingapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class YouTubeVideosResponse {
    @SerializedName("items")
    private List<YouTubeVideo> items;

    @SerializedName("nextPageToken")
    private String nextPageToken;

    public List<YouTubeVideo> getItems() { return items; }
    public void setItems(List<YouTubeVideo> items) { this.items = items; }

    public String getNextPageToken() { return nextPageToken; }
    public void setNextPageToken(String nextPageToken) { this.nextPageToken = nextPageToken; }
}
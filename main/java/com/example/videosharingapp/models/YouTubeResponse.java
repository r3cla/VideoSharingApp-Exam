package com.example.videosharingapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class YouTubeResponse {
    @SerializedName("items")
    private List<YouTubeChannel> items;

    @SerializedName("pageInfo")
    private PageInfo pageInfo;

    public List<YouTubeChannel> getItems() { return items; }
    public void setItems(List<YouTubeChannel> items) { this.items = items; }

    public PageInfo getPageInfo() { return pageInfo; }
    public void setPageInfo(PageInfo pageInfo) { this.pageInfo = pageInfo; }

    public static class PageInfo {
        @SerializedName("totalResults")
        private int totalResults;

        @SerializedName("resultsPerPage")
        private int resultsPerPage;

        public int getTotalResults() { return totalResults; }
        public void setTotalResults(int totalResults) { this.totalResults = totalResults; }

        public int getResultsPerPage() { return resultsPerPage; }
        public void setResultsPerPage(int resultsPerPage) { this.resultsPerPage = resultsPerPage; }
    }
}
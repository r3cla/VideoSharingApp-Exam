/**
 * Author: Nathan Addison
 * Student ID: 5084052
 * For BIT603 Assessment 3
 */

package com.example.videosharingapp.api;

import com.example.videosharingapp.models.YouTubeResponse;
import com.example.videosharingapp.models.YouTubeVideosResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.ArrayList;

public interface YouTubeApiService {

    String BASE_URL = "https://www.googleapis.com/youtube/v3/";

    /**
     * Get channel information by channel ID
     */
    @GET("channels")
    Call<YouTubeResponse> getChannelInfoPublic(
            @Query("part") String part,
            @Query("id") String channelId,
            @Query("key") String apiKey
    );

    /**
     * Get channel information by username
     */
    @GET("channels")
    Call<YouTubeResponse> getChannelInfoByUsernamePublic(
            @Query("part") String part,
            @Query("forUsername") String username,
            @Query("key") String apiKey
    );

    /**
     * Search for channels
     */
    @GET("search")
    Call<YouTubeVideosResponse> searchChannelsPublic(
            @Query("part") String part,
            @Query("q") String query,
            @Query("type") String type,
            @Query("maxResults") int maxResults,
            @Query("key") String apiKey
    );

    /**
     * Get videos from a channel
     */
    @GET("search")
    Call<YouTubeVideosResponse> getChannelVideosPublic(
            @Query("part") String part,
            @Query("channelId") String channelId,
            @Query("type") String type,
            @Query("order") String order,
            @Query("maxResults") int maxResults,
            @Query("key") String apiKey
    );
}
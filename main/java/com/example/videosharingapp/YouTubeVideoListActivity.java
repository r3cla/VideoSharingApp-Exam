/**
 * Author: Nathan Addison
 * For BIT603 Assessment 3
 */

package com.example.videosharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videosharingapp.adapters.VideoAdapter;
import com.example.videosharingapp.api.YouTubeApiManager;
import com.example.videosharingapp.models.YouTubeVideo;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class YouTubeVideoListActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private static final String TAG = "YouTubeVideoListActivity";

    private TextInputEditText channelEditText;
    private Button loadVideosButton, backButton;
    private RecyclerView videosRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;

    private VideoAdapter videoAdapter;
    private YouTubeApiManager apiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_video_list);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        setupApiManager();
    }

    private void initializeViews() {
        channelEditText = findViewById(R.id.channelEditText);
        loadVideosButton = findViewById(R.id.loadVideosButton);
        backButton = findViewById(R.id.backButton);
        videosRecyclerView = findViewById(R.id.videosRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
    }

    private void setupRecyclerView() {
        videoAdapter = new VideoAdapter(this);
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        videosRecyclerView.setAdapter(videoAdapter);
    }

    private void setupClickListeners() {
        loadVideosButton.setOnClickListener(v -> loadChannelVideos());
        backButton.setOnClickListener(v -> finish());

        // Set sample channel ID for testing
        channelEditText.setText("UCRV15pO_LtX1unL71LQyhPw"); // Open Polytech channel
    }

    private void setupApiManager() {
        apiManager = new YouTubeApiManager();
    }

    private void loadChannelVideos() {
        String channelInput = channelEditText.getText().toString().trim();

        if (TextUtils.isEmpty(channelInput)) {
            Toast.makeText(this, "Please enter a channel ID or URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String channelId = extractChannelId(channelInput);
        if (channelId == null) {
            Toast.makeText(this, "Invalid channel ID or URL", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        Log.d(TAG, "Loading videos for channel: " + channelId);

        apiManager.getChannelVideos(channelId, new YouTubeApiManager.VideosCallback() {
            @Override
            public void onSuccess(List<YouTubeVideo> videos) {
                runOnUiThread(() -> {
                    showLoading(false);

                    if (videos.isEmpty()) {
                        showEmptyState(true);
                        Toast.makeText(YouTubeVideoListActivity.this, "No videos found for this channel", Toast.LENGTH_SHORT).show();
                    } else {
                        showEmptyState(false);
                        videoAdapter.setVideos(videos);
                        videosRecyclerView.setVisibility(View.VISIBLE);

                        Log.d(TAG, "Loaded " + videos.size() + " videos successfully");
                        Toast.makeText(YouTubeVideoListActivity.this, "Loaded " + videos.size() + " videos", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showEmptyState(true);

                    Log.e(TAG, "Failed to load videos: " + error);
                    Toast.makeText(YouTubeVideoListActivity.this, "Failed to load videos: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Extract channel ID
     */
    private String extractChannelId(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        input = input.trim();

        // If it's already a channel ID (24 characters starting with UC)
        if (input.matches("UC[a-zA-Z0-9_-]{22}")) {
            return input;
        }

        // URL Regex
        try {
            if (input.contains("youtube.com/channel/")) {
                String[] parts = input.split("youtube.com/channel/");
                if (parts.length > 1) {
                    String channelId = parts[1].split("/")[0].split("\\?")[0];
                    if (channelId.matches("UC[a-zA-Z0-9_-]{22}")) {
                        return channelId;
                    }
                }
            } else if (input.contains("youtube.com/c/") || input.contains("youtube.com/user/") || input.contains("youtube.com/@")) {
                // Return a sample channel ID for testing
                Log.w(TAG, "Custom URL detected, using sample channel ID for testing");
                return "UCRV15pO_LtX1unL71LQyhPw"; // OP Channel
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting channel ID from: " + input, e);
        }

        return null;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            videosRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(boolean show) {
        if (show) {
            emptyStateText.setVisibility(View.VISIBLE);
            videosRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onVideoClick(YouTubeVideo video) {
        String videoId = video.getVideoId();
        String videoTitle = video.getSnippet().getTitle();

        Log.d(TAG, "Video clicked: " + videoTitle + " (ID: " + videoId + ")");

        if (videoId != null && !videoId.isEmpty()) {
            // Navigate to YouTube Player Activity with the video ID
            Intent intent = new Intent(this, YouTubePlayerActivity.class);
            intent.putExtra("VIDEO_ID", videoId);
            intent.putExtra("VIDEO_TITLE", videoTitle);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Video ID not available", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Video ID is null or empty for video: " + videoTitle);
        }
    }
}

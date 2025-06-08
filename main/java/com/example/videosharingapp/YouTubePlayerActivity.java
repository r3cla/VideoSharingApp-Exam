/**
 * Author: Nathan Addison
 * Student ID: 5084052
 * For BIT603 Assessment 3
 */

package com.example.videosharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubePlayerActivity extends AppCompatActivity {

    private static final String TAG = "YouTubePlayerActivity";

    private YouTubePlayerView youTubePlayerView;
    private TextInputEditText urlEditText;
    private Button playButton, backButton;
    private YouTubePlayer youTubePlayer;
    private String pendingVideoId; // Store video ID until player is ready

    // YouTube URL patterns for validation
    private static final String YOUTUBE_URL_PATTERN =
            "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([a-zA-Z0-9_-]{11}).*$";

    private static final Pattern pattern = Pattern.compile(YOUTUBE_URL_PATTERN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        initializeViews();
        setupYouTubePlayer();
        setupClickListeners();

        // Handle incoming video from video list
        handleIncomingIntent();
    }

    private void initializeViews() {
        youTubePlayerView = findViewById(R.id.youtubePlayerView);
        urlEditText = findViewById(R.id.urlEditText);
        playButton = findViewById(R.id.playButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupYouTubePlayer() {
        // Add YouTubePlayerView to lifecycle for proper management
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer player) {
                youTubePlayer = player;
                Log.d(TAG, "YouTube Player is ready");

                // If we have a pending video ID, play it now
                if (pendingVideoId != null) {
                    Log.d(TAG, "Playing pending video: " + pendingVideoId);
                    playVideoById(pendingVideoId);
                    pendingVideoId = null; // Clear the pending video
                }
            }

            @Override
            public void onError(@NonNull YouTubePlayer player, @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError error) {
                Log.e(TAG, "YouTube Player error: " + error.toString());
                Toast.makeText(YouTubePlayerActivity.this,
                        "Error playing video: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        playButton.setOnClickListener(v -> playVideo());

        backButton.setOnClickListener(v -> {
            finish(); // Return to previous activity
        });
    }

    /**
     * Handle incoming intent from video list
     */
    private void handleIncomingIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String videoId = intent.getStringExtra("VIDEO_ID");
            String videoTitle = intent.getStringExtra("VIDEO_TITLE");

            if (videoId != null && !videoId.isEmpty()) {
                Log.d(TAG, "Received video ID from intent: " + videoId);
                Log.d(TAG, "Video title: " + videoTitle);

                // Construct YouTube URL and set it in the EditText
                String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
                urlEditText.setText(youtubeUrl);

                // If player is ready, play immediately. Otherwise, store for later
                if (youTubePlayer != null) {
                    playVideoById(videoId);
                } else {
                    pendingVideoId = videoId;
                    Log.d(TAG, "Player not ready yet, storing video ID for later play");
                }
            }
        }
    }

    private void playVideo() {
        String url = urlEditText.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            showToast("Please enter a YouTube URL");
            return;
        }

        String videoId = extractVideoId(url);

        if (videoId == null) {
            showToast("Invalid YouTube URL. Please enter a valid YouTube video URL.");
            return;
        }

        playVideoById(videoId);
    }

    /**
     * Play video by ID
     */
    private void playVideoById(String videoId) {
        if (youTubePlayer != null) {
            youTubePlayerView.setVisibility(View.VISIBLE);
            youTubePlayer.loadVideo(videoId, 0);
            Log.d(TAG, "Loading video with ID: " + videoId);
            showToast("Loading video...");
        } else {
            showToast("Player not ready yet. Please try again in a moment.");
            // Store the video ID to play when ready
            pendingVideoId = videoId;
        }
    }

    /**
     * Extract video ID from various YouTube URL formats including embeds, short links and normal links
     */
    private String extractVideoId(String url) {
        try {
            Matcher matcher = pattern.matcher(url);

            if (matcher.matches()) {
                String videoId = matcher.group(4); // Group 4 contains the video ID
                Log.d(TAG, "Extracted video ID: " + videoId + " from URL: " + url);
                return videoId;
            }

            // Additional parsing for edge cases
            if (url.contains("youtube.com/watch?v=")) {
                String[] parts = url.split("v=");
                if (parts.length > 1) {
                    String videoId = parts[1].split("&")[0]; // Remove any additional parameters
                    if (videoId.length() == 11) { // YouTube video IDs are 11 characters
                        return videoId;
                    }
                }
            } else if (url.contains("youtu.be/")) {
                String[] parts = url.split("youtu.be/");
                if (parts.length > 1) {
                    String videoId = parts[1].split("\\?")[0]; // Remove any parameters
                    if (videoId.length() == 11) {
                        return videoId;
                    }
                }
            }

            Log.w(TAG, "Could not extract video ID from URL: " + url);
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error extracting video ID from URL: " + url, e);
            return null;
        }
    }

    /**
     * Validate if the provided URL is a valid YouTube URL
     */
    private boolean isValidYouTubeUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        Matcher matcher = pattern.matcher(url);
        boolean isValid = matcher.matches();

        Log.d(TAG, "URL validation for '" + url + "': " + isValid);
        return isValid;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // YouTube player will be automatically cleaned up by the lifecycle observer
        Log.d(TAG, "YouTubePlayerActivity destroyed");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
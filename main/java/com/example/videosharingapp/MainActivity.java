/**
 * Author: Nathan Addison
 * Student ID: 5084052
 * For BIT603 Assessment 3
 */

package com.example.videosharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videosharingapp.api.YouTubeApiManager;
import com.example.videosharingapp.models.YouTubeChannel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private CloudDatabaseService cloudService;
    private MaterialCardView watchVideoCard, listVideosCard;
    private TextView userInfo;
    private ListenerRegistration userDataListener;
    private ListenerRegistration channelDataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Cloud Service
        mAuth = FirebaseAuth.getInstance();
        cloudService = new CloudDatabaseService();

        // Initialize UI components
        watchVideoCard = findViewById(R.id.watchVideoCard);
        listVideosCard = findViewById(R.id.listVideosCard);
        userInfo = findViewById(R.id.userInfo);

        // Set up services
        setupUserInfo();
        setupServiceClickListeners();
        setupCloudDatabase();

        // Test YouTube API (uncomment to test)
        // testYouTubeApi();
    }

    private void setupUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                userInfo.setText("Welcome, " + displayName);
            } else {
                userInfo.setText("Welcome, " + currentUser.getEmail());
            }
        } else {
            userInfo.setText("Logged in successfully");
        }
    }

    private void setupServiceClickListeners() {
        // Watch a YouTube Video service
        watchVideoCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, YouTubePlayerActivity.class);
            startActivity(intent);
        });

        // List a YouTube Channel Video service
        listVideosCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, YouTubeVideoListActivity.class);
            startActivity(intent);
        });
    }

    private void setupCloudDatabase() {
        // Write user data to cloud database
        cloudService.writeUserData(new CloudDatabaseService.WriteCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "User data written to cloud successfully");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to write user data to cloud", e);
                Toast.makeText(MainActivity.this, "Failed to sync user data", Toast.LENGTH_SHORT).show();
            }
        });

        // TEST FUNCTION
        // addSampleChannelData();

        // Set up real-time listeners
        setupRealtimeListeners();
    }

    private void addSampleChannelData() {
        // Add sample channel data to demonstrate cloud database functionality
        cloudService.writeChannelData(
                "UC_sample_channel_id",
                "Sample Tech Channel",
                "A sample technology channel for testing purposes",
                10000,
                new CloudDatabaseService.WriteCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Sample channel data added successfully");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to add sample channel data", e);
                    }
                }
        );
    }

    private void setupRealtimeListeners() {
        // Set up user data listener
        userDataListener = cloudService.setupUserDataListener(new CloudDatabaseService.UserDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                Log.d(TAG, "User data updated: " + userData.toString());
                // Update UI with new user data if needed
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "User data listener failed", e);
            }
        });

        // Set up channel data listener
        channelDataListener = cloudService.setupChannelDataListener(new CloudDatabaseService.ChannelDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> channels) {
                Log.d(TAG, "Channel data updated. Found " + channels.size() + " channels");
                for (Map<String, Object> channel : channels) {
                    Log.d(TAG, "Channel: " + channel.get("channelName"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Channel data listener failed", e);
            }
        });
    }

    /**
     * Test API function
     */
    private void testYouTubeApi() {
        YouTubeApiManager apiManager = new YouTubeApiManager(); // No account parameter needed

        // Test with a known channel ID
        String channelId = "UCBJycsmduvYEL83R_U4JriQ"; // YouTube Spotlight channel

        Log.d(TAG, "Starting YouTube API test with channel ID: " + channelId);

        apiManager.getChannelInfo(channelId, new YouTubeApiManager.ChannelInfoCallback() {
            @Override
            public void onSuccess(YouTubeChannel channel) {
                Log.d(TAG, "YouTube API Test Results:");
                Log.d(TAG, "Channel ID: " + channel.getId());
                Log.d(TAG, "Channel Name: " + channel.getSnippet().getTitle());

                String subscriberCount = channel.getStatistics().getSubscriberCount();
                Log.d(TAG, "Subscriber Count: " + subscriberCount);

                String description = channel.getSnippet().getDescription();
                if (description != null && description.length() > 100) {
                    description = description.substring(0, 100) + "...";
                }
                Log.d(TAG, "Description: " + description);

                // Show success message on UI thread
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "YouTube API test successful!\nChannel: " + channel.getSnippet().getTitle(),
                            Toast.LENGTH_LONG).show();
                });

                // Save to cloud database
                try {
                    int subCount = 0;
                    if (subscriberCount != null && !subscriberCount.isEmpty()) {
                        try {
                            subCount = Integer.parseInt(subscriberCount);
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "Could not parse subscriber count: " + subscriberCount + ", using 0");
                        }
                    }

                    String finalDescription = channel.getSnippet().getDescription();
                    if (finalDescription == null) {
                        finalDescription = "No description available";
                    }

                    cloudService.writeChannelData(
                            channel.getId(),
                            channel.getSnippet().getTitle(),
                            finalDescription,
                            subCount,
                            new CloudDatabaseService.WriteCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "YouTube channel data saved to cloud successfully");
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this,
                                                "Channel data saved to database!",
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e(TAG, "Failed to save YouTube channel data", e);
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this,
                                                "Failed to save to database: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Error processing channel data", e);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "YouTube API test failed: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "YouTube API test failed: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Test YouTube API with video fetching
     * Additional test method for video list functionality
     */
    private void testYouTubeApiWithVideos() {
        YouTubeApiManager apiManager = new YouTubeApiManager();
        String channelId = "UCBJycsmduvYEL83R_U4JriQ"; // YouTube Spotlight channel

        // First get channel info
        apiManager.getChannelInfo(channelId, new YouTubeApiManager.ChannelInfoCallback() {
            @Override
            public void onSuccess(YouTubeChannel channel) {
                Log.d(TAG, "Channel info fetched successfully: " + channel.getSnippet().getTitle());

                // Then get videos from the channel
                apiManager.getChannelVideos(channelId, new YouTubeApiManager.VideosCallback() {
                    @Override
                    public void onSuccess(List<com.example.videosharingapp.models.YouTubeVideo> videos) {
                        Log.d(TAG, "Successfully fetched " + videos.size() + " videos from channel");
                        for (com.example.videosharingapp.models.YouTubeVideo video : videos) {
                            Log.d(TAG, "Video: " + video.getSnippet().getTitle());
                        }

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "Fetched " + videos.size() + " videos from channel!",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to fetch videos: " + error);
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "Failed to fetch videos: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to fetch channel info: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Failed to fetch channel info: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, redirect to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners to prevent memory leaks
        if (userDataListener != null) {
            userDataListener.remove();
            Log.d(TAG, "User data listener removed");
        }
        if (channelDataListener != null) {
            channelDataListener.remove();
            Log.d(TAG, "Channel data listener removed");
        }
    }
}
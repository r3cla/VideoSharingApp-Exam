/**
 * Author: Nathan Addison
 * Student ID: 5084052
 * For BIT603 Assessment 3
 */

package com.example.videosharingapp;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudDatabaseService {

    private static final String TAG = "CloudDatabaseService";
    private static final String USERS_COLLECTION = "users";
    private static final String YOUTUBE_CHANNELS_COLLECTION = "youtube_channels";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Callback interfaces
    public interface UserDataCallback {
        void onSuccess(Map<String, Object> userData);
        void onFailure(Exception e);
    }

    public interface ChannelDataCallback {
        void onSuccess(List<Map<String, Object>> channels);
        void onFailure(Exception e);
    }

    public interface WriteCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public CloudDatabaseService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Write user information to cloud database
     * Performance tracking: Measures time between request and response
     */
    public void writeUserData(WriteCallback callback) {
        long startTime = System.currentTimeMillis();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure(new Exception("User not authenticated"));
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", currentUser.getEmail());
        userData.put("name", currentUser.getDisplayName());
        userData.put("profileImageUrl", currentUser.getPhotoUrl() != null ?
                currentUser.getPhotoUrl().toString() : "");
        userData.put("lastLoginTime", System.currentTimeMillis());
        userData.put("userId", currentUser.getUid());

        db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    Log.d(TAG, "User data write completed in: " + duration + "ms");
                    // Performance factors: Network latency, document size, server load
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    Log.e(TAG, "User data write failed after: " + duration + "ms", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Read user information from cloud database
     */
    public void readUserData(UserDataCallback callback) {
        long startTime = System.currentTimeMillis();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure(new Exception("User not authenticated"));
            return;
        }

        db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    Log.d(TAG, "User data read completed in: " + duration + "ms");

                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.getData());
                    } else {
                        callback.onFailure(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    Log.e(TAG, "User data read failed after: " + duration + "ms", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Write YouTube channel data to cloud database
     */
    public void writeChannelData(String channelId, String channelName,
                                 String description, int subscriberCount, WriteCallback callback) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> channelData = new HashMap<>();
        channelData.put("channelId", channelId);
        channelData.put("channelName", channelName);
        channelData.put("description", description);
        channelData.put("subscriberCount", subscriberCount);
        channelData.put("addedBy", auth.getCurrentUser() != null ?
                auth.getCurrentUser().getUid() : "unknown");
        channelData.put("timestamp", System.currentTimeMillis());

        db.collection(YOUTUBE_CHANNELS_COLLECTION)
                .add(channelData)
                .addOnSuccessListener(documentReference -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    Log.d(TAG, "Channel data write completed in: " + duration + "ms");
                    /*
                     * Performance factors affecting cloud data access:
                     * 1. Network latency and bandwidth
                     * 2. Document size and complexity
                     * 3. Firestore server load and geographic location
                     * 4. Device processing power and memory
                     * 5. Number of concurrent operations
                     * 6. Index optimization for queries
                     */
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    Log.e(TAG, "Channel data write failed after: " + duration + "ms", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Read YouTube channel data from cloud database
     */
    public void readChannelData(ChannelDataCallback callback) {
        long startTime = System.currentTimeMillis();

        db.collection(YOUTUBE_CHANNELS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10) // Optimization: Limit results for better performance
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    Log.d(TAG, "Channel data read completed in: " + duration + "ms");

                    List<Map<String, Object>> channels = queryDocumentSnapshots.getDocuments()
                            .stream()
                            .map(doc -> doc.getData())
                            .collect(java.util.stream.Collectors.toList());

                    callback.onSuccess(channels);
                })
                .addOnFailureListener(e -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    Log.e(TAG, "Channel data read failed after: " + duration + "ms", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Set up listener for channel data updates
     * This callback method is triggered when data changes in the cloud database
     */
    public ListenerRegistration setupChannelDataListener(ChannelDataCallback callback) {
        Log.d(TAG, "Setting up real-time listener for channel data");

        return db.collection(YOUTUBE_CHANNELS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Channel data listener failed", e);
                        callback.onFailure(e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        Log.d(TAG, "Channel data updated via real-time listener");
                        List<Map<String, Object>> channels = queryDocumentSnapshots.getDocuments()
                                .stream()
                                .map(doc -> doc.getData())
                                .collect(java.util.stream.Collectors.toList());

                        callback.onSuccess(channels);
                    }
                });
    }

    /**
     * Set up listener for user data updates
     */
    public ListenerRegistration setupUserDataListener(UserDataCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure(new Exception("User not authenticated"));
            return null;
        }

        Log.d(TAG, "Setting up real-time listener for user data");

        return db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "User data listener failed", e);
                        callback.onFailure(e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Log.d(TAG, "User data updated via real-time listener");
                        callback.onSuccess(documentSnapshot.getData());
                    }
                });
    }
}
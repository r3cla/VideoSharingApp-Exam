/**
 * Author: Nathan Addison
 * For BIT603 Assessment 3
 */

package com.example.videosharingapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videosharingapp.R;
import com.example.videosharingapp.models.YouTubeVideo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<YouTubeVideo> videos;
    private OnVideoClickListener listener;

    public interface OnVideoClickListener {
        void onVideoClick(YouTubeVideo video);
    }

    public VideoAdapter(OnVideoClickListener listener) {
        this.videos = new ArrayList<>();
        this.listener = listener;
    }

    public void setVideos(List<YouTubeVideo> videos) {
        this.videos = videos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        YouTubeVideo video = videos.get(position);
        holder.bind(video);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        private ImageView videoThumbnail;
        private TextView videoTitle;
        private TextView channelName;
        private TextView publishDate;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            videoTitle = itemView.findViewById(R.id.videoTitle);
            channelName = itemView.findViewById(R.id.channelName);
            publishDate = itemView.findViewById(R.id.publishDate);

            // Set up click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVideoClick(videos.get(position));
                }
            });
        }

        public void bind(YouTubeVideo video) {
            videoTitle.setText(video.getSnippet().getTitle());
            channelName.setText(video.getSnippet().getChannelTitle());

            // Format and display publish date
            String formattedDate = formatPublishDate(video.getSnippet().getPublishedAt());
            publishDate.setText(formattedDate);

            // Shows a placeholder for thumbnail
            // In a real production, we would use an image loading library
            videoThumbnail.setImageResource(R.drawable.ic_video_placeholder);
        }

        private String formatPublishDate(String publishedAt) {
            try {
                // YouTube API returns dates in ISO 8601 format
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

                Date date = inputFormat.parse(publishedAt);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                // If parsing fails, just return the original string
                return publishedAt;
            }
            return publishedAt;
        }
    }
}

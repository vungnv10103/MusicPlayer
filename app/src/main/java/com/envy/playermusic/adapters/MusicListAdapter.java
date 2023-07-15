package com.envy.playermusic.adapters;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.envy.playermusic.R;
import com.envy.playermusic.listeners.IMusicListener;
import com.envy.playermusic.models.SongModel;
import com.envy.playermusic.utils.MyMediaPlayer;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.viewHolder>{
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static List<SongModel> list;
    private List<SongModel> listOld;

    private static IMusicListener iMusicListener;
    private Filter filter;


    public MusicListAdapter(Context context, List<SongModel> list, IMusicListener iMusicListener) {
        MusicListAdapter.context = context;
        MusicListAdapter.list = list;
        this.listOld = list;
        MusicListAdapter.iMusicListener = iMusicListener;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, @SuppressLint("RecyclerView") int position) {
        // sort list name a -> z

        Collections.sort(list, SongModel.sortSong);

        SongModel songData = list.get(position);

        Uri albumArtwork = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(songData.getAlbumId()));
//        if (context instanceof Activity && !((Activity) context).isFinishing()) {
        Glide.with(context)
                .load(albumArtwork)
                .placeholder(R.drawable.icon_music) // Ảnh placeholder
                .error(R.drawable.icon_music) // Ảnh hiển thị khi lỗi
                .into(holder.img);
//        }
        holder.tvNameSong.setText(songData.getTitle());
        holder.tvArtist.setText(songData.getArtist());
        holder.tvLengthSong.setText(convertToMMSS(songData.getDuration()));
//        Log.d("log", "duration: " + songData.getDuration() + " -name: " + songData.getTitle());
        holder.tvSize.setText(convertFileSize(songData.getSize()));
        if (MyMediaPlayer.currentIndex == position) {
            holder.tvNameSong.setTextColor(Color.parseColor("#FF0000"));

        } else {
            holder.tvNameSong.setTextColor(Color.parseColor("#FFFFFF"));
        }
        holder.imgMore.setOnClickListener(v -> Toast.makeText(context, "updating", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView tvNameSong, tvArtist, tvSize, tvLengthSong;
        ImageView img, imgMore;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgSong);
            tvNameSong = itemView.findViewById(R.id.tvNameSong);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvSize = itemView.findViewById(R.id.tvSizeSong);
            tvLengthSong = itemView.findViewById(R.id.tvLengthSong);
            imgMore = itemView.findViewById(R.id.imgMore);
            itemView.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
                iMusicListener.onClick(list, list.get(currentPosition));
            });
        }
    }

    @NonNull
    private String convertSizeSongMB(long bytes) {
        String size;

        double k = bytes / 1024.0;
        double m = ((bytes / 1024.0) / 1024.0);
        double g = (((bytes / 1024.0) / 1024.0) / 1024.0);
        double t = ((((bytes / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");
        if (t > 1) {
            size = dec.format(t).concat(" TB");
        } else if (g > 1) {
            size = dec.format(t).concat(" GB");
        } else if (m > 1) {
            size = dec.format(t).concat(" MB");
        } else if (k > 1) {
            size = dec.format(t).concat(" KB");
        } else {
            size = dec.format(t).concat(" Bytes");
        }
        return size;
    }


    @SuppressLint("DefaultLocale")
    @NonNull
    private static String convertToMMSS(String duration) {
        long millis = Long.parseLong(duration);
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        long milliseconds = millis % 1000;
//        return String.format("%02d:%02d:%03d", minutes, seconds, milliseconds);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    public static String convertFileSize(long sizeInBytes) {
        final long kiloBytes = 1024;
        final long megaBytes = kiloBytes * 1024;
        final long gigaBytes = megaBytes * 1024;
        final long teraBytes = gigaBytes * 1024;

        if (sizeInBytes >= teraBytes) {
            return String.format("%.2f TB", (float) sizeInBytes / teraBytes);
        } else if (sizeInBytes >= gigaBytes) {
            return String.format("%.2f GB", (float) sizeInBytes / gigaBytes);
        } else if (sizeInBytes >= megaBytes) {
            return String.format("%.2f MB", (float) sizeInBytes / megaBytes);
        } else if (sizeInBytes >= kiloBytes) {
            return String.format("%.2f KB", (float) sizeInBytes / kiloBytes);
        } else {
            return String.format("%d Bytes", sizeInBytes);
        }
    }


}
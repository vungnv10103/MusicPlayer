package com.envy.playermusic.adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.envy.playermusic.MainActivity;
import com.envy.playermusic.PlayerMusicService;
import com.envy.playermusic.R;
import com.envy.playermusic.listeners.IMusicListener;
import com.envy.playermusic.models.SongModel;
import com.envy.playermusic.utils.MyMediaPlayer;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import org.jetbrains.annotations.Contract;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.viewHolder> implements Filterable {
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static List<SongModel> list;
    private final List<SongModel> listOld;

    private final int layoutItem;
    private static IMusicListener iMusicListener;
    private static ExoPlayer player;
    private static ConstraintLayout playerView;


    public MusicListAdapter(Context context, int layoutItem, List<SongModel> list, IMusicListener iMusicListener, ExoPlayer player, ConstraintLayout playerView) {
        MusicListAdapter.context = context;
        MusicListAdapter.list = list;
        this.listOld = list;
        this.layoutItem = layoutItem;
        MusicListAdapter.player = player;
        MusicListAdapter.iMusicListener = iMusicListener;
        MusicListAdapter.playerView = playerView;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutItem, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, @SuppressLint("RecyclerView") int position) {
        // sort list name a -> z

        Collections.sort(list, SongModel.sortSong);

        SongModel songData = list.get(position);

        Uri albumArtwork = artWorkSong(songData.getAlbumId());
//        if (context instanceof Activity && !((Activity) context).isFinishing()) {
        Glide.with(context)
                .load(albumArtwork)
                .placeholder(R.drawable.icon_music) // Ảnh placeholder
                .error(R.drawable.icon_music) // Ảnh hiển thị khi lỗi
                .into(holder.img);
//        }
        holder.tvNameSong.setText(songData.getTitle());
        holder.tvArtist.setText(songData.getArtist());
        try {
            String length = songData.getDuration();
            holder.tvLengthSong.setText(convertToMMSS(length    ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.tvLengthSong.setText(convertToMMSS(songData.getDuration()));
//        Log.d("log", "duration: " + songData.getDuration() + " -name: " + songData.getTitle());
        holder.tvSize.setText(convertFileSize(songData.getSize()));
        if (MyMediaPlayer.currentIndex == position) {
            holder.tvNameSong.setTextColor(Color.parseColor("#FF0000"));

        } else {
            holder.tvNameSong.setTextColor(Color.parseColor("#FFFFFF"));
        }
        holder.imgMore.setOnClickListener(v -> Toast.makeText(context, "updating", Toast.LENGTH_SHORT).show());

        holder.itemView.setOnClickListener(v -> {
            iMusicListener.onClick(list, list.get(position));
//            playerView.setVisibility(View.VISIBLE);

            // check permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                    PackageManager.PERMISSION_GRANTED){
                ((MainActivity) context).recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String str = constraint.toString();
                if (str.isEmpty()) {
                    list = listOld;
                } else {
                    List<SongModel> mList = new ArrayList<>();
                    for (SongModel item : listOld) {
                        if (item.getTitle().toLowerCase().contains(str.toLowerCase())) {
                            mList.add(item);
                        }
                    }
                    list = mList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = list;
                return filterResults;
            }


            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list = (List<SongModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static Uri artWorkSong(String albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumId));
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

            });
        }

    }
    @NonNull
    private List<MediaItem> getMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (SongModel song : list) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getPath())
                    .setMediaMetadata(getMetaData(song))
                    .build();

            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    @NonNull
    @Contract("_ -> new")
    private MediaMetadata getMetaData(@NonNull SongModel song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(artWorkSong(song.getAlbumId()))
                .build();
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

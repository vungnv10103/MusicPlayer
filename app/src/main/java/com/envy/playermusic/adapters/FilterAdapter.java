package com.envy.playermusic.adapters;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.envy.playermusic.R;
import com.envy.playermusic.listeners.IMusicListener;
import com.envy.playermusic.models.SongModel;
import com.envy.playermusic.utils.MyMediaPlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.viewHolder> {
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static List<String> list;


    public FilterAdapter(Context context, List<String> list) {
        FilterAdapter.context = context;
        FilterAdapter.list = list;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, @SuppressLint("RecyclerView") int position) {
        // sort list name a -> z

//        Collections.sort(list, SongModel.sortSong);
        String type = list.get(position);
        holder.tvFilter.setText(type);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView tvFilter;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            tvFilter = itemView.findViewById(R.id.tvFilter);
            itemView.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
            });
        }
    }


}

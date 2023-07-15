package com.envy.playermusic.presenters;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.envy.playermusic.listeners.IGetMusic;
import com.envy.playermusic.models.SongModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetMusicPresenter {
    private final IGetMusic iGetMusic;
    private final Context context;

    public GetMusicPresenter(Context context, IGetMusic iGetMusic) {
        this.context = context;
        this.iGetMusic = iGetMusic;
    }

    @SuppressLint("Recycle")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void getMusicInLocal() {
        final List<SongModel> songsList = new ArrayList<>();
        Uri mediaStorageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaStorageUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            mediaStorageUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //define projection
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.SIZE
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = context.getContentResolver().query(mediaStorageUri, projection, selection, null, null);
        while (cursor.moveToNext()) {

            String title = cursor.getString(0);
            String path = cursor.getString(1);
            String duration = cursor.getString(2);
            String albumId = cursor.getString(3);
            String artist = cursor.getString(4);
            long size = cursor.getInt(5);

//            SongModel songData = new SongModel(path, title, duration);
            Uri albumArtwork = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumId));
            SongModel songData = new SongModel(path, title, artist, albumId, size, duration);
            if (new File(songData.getPath()).exists()) songsList.add(songData);
        }
        if (songsList.size() == 0) {
            iGetMusic.onError("No Music Founds");
        } else {
            iGetMusic.onSuccess(songsList);
        }

    }
}

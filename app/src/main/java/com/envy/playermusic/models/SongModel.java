package com.envy.playermusic.models;

import java.io.Serializable;
import java.util.Comparator;

public class SongModel implements Serializable {
    private String path;
    private String title;
    private String artist;
    private String albumId;
    //    private Uri albumArtwork;
    private long size;

    private String duration;

    public SongModel() {
    }

    public SongModel(String path, String title, String duration) {
        this.path = path;
        this.title = title;
        this.duration = duration;
    }

    public SongModel(String path, String title, String artist, String albumId, long size, String duration) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.albumId = albumId;
        this.size = size;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }


    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }


    // Usage of comparator
    public static final Comparator<SongModel> sortSong = (s1, s2) -> {
        String song1
                = s1.getTitle().toLowerCase();
        String song2
                = s2.getTitle().toLowerCase();

        // Returning in ascending order
        return song1.compareTo(
                song2);
    };

}

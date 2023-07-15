package com.envy.playermusic.listeners;

import com.envy.playermusic.models.SongModel;

import java.util.List;

public interface IMusicListener {
    void onClick(List<SongModel> listSong, SongModel currentSong);
}

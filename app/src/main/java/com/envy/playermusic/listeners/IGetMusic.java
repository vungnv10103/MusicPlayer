package com.envy.playermusic.listeners;

import com.envy.playermusic.models.SongModel;

import java.util.List;

public interface IGetMusic {
    void onSuccess(List<SongModel> listSong);
    void onError(String message);
}

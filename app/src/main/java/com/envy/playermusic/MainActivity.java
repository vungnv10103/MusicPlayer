package com.envy.playermusic;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.envy.playermusic.adapters.MusicListAdapter;
import com.envy.playermusic.databinding.ActivityMainBinding;
import com.envy.playermusic.listeners.IGetMusic;
import com.envy.playermusic.listeners.IMusicListener;
import com.envy.playermusic.models.SongModel;
import com.envy.playermusic.presenters.GetMusicPresenter;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements IGetMusic, IMusicListener {

    private ActivityMainBinding binding;

    private GetMusicPresenter getMusicPresenter;

    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getMusicPresenter = new GetMusicPresenter(this, this);


        if (!checkPermission()) {
            requestPermission();
            return;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getMusicPresenter.getMusicInLocal();
            }
        }


    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showToast("READ PERMISSION IS REQUIRED, PLEASE ALLOW FROM SETTINGS");
        }
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_OPEN_DOCUMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    getMusicPresenter.getMusicInLocal();
                }
            } else {
                showToast("READ PERMISSION DENIED");
            }

        }
    }

    @Override
    public void onSuccess(List<SongModel> listSong) {
        MusicListAdapter musicListAdapter = new MusicListAdapter(this, listSong, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        binding.rcvSongs.setLayoutManager(linearLayoutManager);
        if (binding.rcvSongs.getItemDecorationCount() > 0) {
            binding.rcvSongs.removeItemDecorationAt(0);
        }

        binding.rcvSongs.setAdapter(musicListAdapter);
        binding.progressBar.setVisibility(View.GONE);
        binding.rcvSongs.setVisibility(View.VISIBLE);

    }

    @Override
    public void onError(String message) {
        binding.rcvSongs.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(List<SongModel> listSong, SongModel currentSong) {
        showToast(currentSong.getTitle());
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_head, menu);
        MenuItem searchItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private Timer timer = new Timer();
            private final long DELAY = 500;

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                if (newText.length() > 0) {
                                    showToast(newText);
                                }
                            }
                        },
                        DELAY
                );
                return true;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }
}
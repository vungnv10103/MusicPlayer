package com.envy.playermusic;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.envy.playermusic.adapters.FilterAdapter;
import com.envy.playermusic.adapters.MusicListAdapter;
import com.envy.playermusic.databinding.ActivityMainBinding;
import com.envy.playermusic.listeners.IGetMusic;
import com.envy.playermusic.listeners.IMusicListener;
import com.envy.playermusic.models.SongModel;
import com.envy.playermusic.presenters.GetMusicPresenter;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements IGetMusic, IMusicListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;

    private GetMusicPresenter getMusicPresenter;
    private MusicListAdapter musicListAdapter;
    private List<SongModel> songList = new ArrayList<>();
    private MenuItem notificationItem;
    private View actionViewNotification;

    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;
    private boolean isGirdView = false;
    private int badgeCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getMusicPresenter = new GetMusicPresenter(this, this);


        setDataFilter();

        if (!checkPermission()) {
            requestPermission();
            return;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getMusicPresenter.getMusicInLocal();
            }
        }

        binding.imgChangeLayout.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            isGirdView = !isGirdView;
            if (isGirdView) {
                binding.imgChangeLayout.setImageResource(R.drawable.icon_view_list_24);
            } else {
                binding.imgChangeLayout.setImageResource(R.drawable.icon_grid_view_24);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getMusicPresenter.getMusicInLocal();
            }
        });

//        ExtendedFloatingActionButton extendedFab = binding.extendedFab;
        final ExtendedFloatingActionButton extendedFloatingActionButton = binding.extFloatingActionButton;
        extendedFloatingActionButton.setOnLongClickListener(v -> {
            badgeCount--;
            updateBadgeCountNew(notificationItem, badgeCount);
            return true;
        });
        extendedFloatingActionButton.setOnClickListener(v -> {
            badgeCount++;
            updateBadgeCountNew(notificationItem, badgeCount);
        });


        binding.rcvSongs.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
//                    extendedFab.shrink(); // Shrink the FloatingActionButton
                    extendedFloatingActionButton.shrink();


                } else {
//                    extendedFab.extend(); // Extend the FloatingActionButton
                    extendedFloatingActionButton.extend();
                }
            }
        });


    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showToast("READ PERMISSION IS REQUIRED, PLEASE ALLOW FROM SETTINGS");
        }
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_OPEN_DOCUMENT);
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

    private void setAnimationRecyclerview(int animResource) {
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, animResource);
        binding.rcvSongs.setLayoutAnimation(animationController);
    }

    @NonNull
    private List<String> fakeList() {
        List<String> listFilter = new ArrayList<>();
        listFilter.add("Danh sách phát");
        listFilter.add("Đĩa nhạc");
        listFilter.add("Nghệ sĩ");
        listFilter.add("Danh sách phát");
        listFilter.add("Đĩa nhạc");
        listFilter.add("Nghệ sĩ");
        return listFilter;
    }

    private void setDataFilter() {
        FilterAdapter filterAdapter = new FilterAdapter(this, fakeList());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        binding.rcvFilter.setLayoutManager(linearLayoutManager);
        binding.rcvFilter.setAdapter(filterAdapter);

    }

    @Override
    public void onSuccess(List<SongModel> listSong) {
        this.songList = listSong;
        runOnUiThread(() -> {
            setAnimationRecyclerview(R.anim.layout_animation_up_to_down);
            if (isGirdView) {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
                gridLayoutManager.setSmoothScrollbarEnabled(true);
                musicListAdapter = new MusicListAdapter(this, R.layout.item_song_horizontal, listSong, this);
                binding.rcvSongs.setLayoutManager(gridLayoutManager);
            } else {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                linearLayoutManager.setSmoothScrollbarEnabled(true);
                musicListAdapter = new MusicListAdapter(this, R.layout.item_song_vertical, listSong, this);
                binding.rcvSongs.setLayoutManager(linearLayoutManager);
            }

            if (binding.rcvSongs.getItemDecorationCount() > 0) {
                binding.rcvSongs.removeItemDecorationAt(0);
            }

            binding.rcvSongs.setAdapter(musicListAdapter);
            binding.progressBar.setVisibility(View.GONE);
            binding.rcvSongs.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onError(String message) {
        binding.rcvSongs.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(List<SongModel> listSong, @NonNull SongModel currentSong) {
        showToast(currentSong.getTitle());
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void updateBadgeCountNew(@NonNull MenuItem menuItem, int count) {
        badgeCount = count;

        actionViewNotification = menuItem.getActionView();
        actionViewNotification.setOnClickListener(v -> showToast("Open Notification Activity"));
        ImageView iconImageView = actionViewNotification.findViewById(R.id.iconImageView);
        TextView badgeTextView = actionViewNotification.findViewById(R.id.badgeTextView);

        if (count <= 0) {
            badgeTextView.setVisibility(View.GONE);
        } else {
            badgeTextView.setVisibility(View.VISIBLE);
            badgeTextView.setText(String.valueOf(count));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_head, menu);

        notificationItem = menu.findItem(R.id.actionNotification);
        updateBadgeCountNew(notificationItem, 0);

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
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

//                                    showToast(newText);
                        runOnUiThread(() -> musicListAdapter.getFilter().filter(newText));
                    }
                }, DELAY);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}
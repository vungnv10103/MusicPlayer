package com.envy.playermusic;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.envy.playermusic.adapters.FilterAdapter;
import com.envy.playermusic.adapters.MusicListAdapter;
import com.envy.playermusic.databinding.ActivityMainBinding;
import com.envy.playermusic.listeners.IGetMusic;
import com.envy.playermusic.listeners.IMusicListener;
import com.envy.playermusic.models.SongModel;
import com.envy.playermusic.presenters.GetMusicPresenter;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.FullScreenDialog;
import com.kongzue.dialogx.interfaces.OnBindView;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements IGetMusic, IMusicListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final Set<Integer> generatedNumbers = new HashSet<>();

    private ActivityMainBinding binding;
    private boolean isDataLoaded = false;

    private GetMusicPresenter getMusicPresenter;
    private MusicListAdapter musicListAdapter;
    private List<SongModel> songList = new ArrayList<>();
    private MenuItem notificationItem;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;
    private boolean isGirdView = false;
    private int badgeCount = 0;

    private ExoPlayer exoPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        exoPlayer = new ExoPlayer.Builder(this).build();
        getMusicPresenter = new GetMusicPresenter(this, this);


        binding.imgChangeLayout.setOnClickListener(v -> {
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
//            updateBadgeCountNew(notificationItem, badgeCount);
            int randomIndexSong = getRandomNumberInRange(0, songList.size());
            showControllerSong(songList.get(randomIndexSong));
            if (!exoPlayer.isPlaying()) {
                exoPlayer.setMediaItems(getMediaItems(), randomIndexSong, 0);
            } else {
                exoPlayer.pause();
                exoPlayer.seekTo(randomIndexSong, 0);
            }

            exoPlayer.prepare();
            exoPlayer.play();

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

    @NonNull
    private List<MediaItem> getMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (SongModel song : songList) {
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

    private void checkDataLoaded() {
        if (isDataLoaded) {
            binding.progressBar.setVisibility(View.GONE);
            binding.viewMain.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.viewMain.setVisibility(View.GONE);
        }
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

        isDataLoaded = true;
        runOnUiThread(() -> {
            setAnimationRecyclerview(R.anim.layout_animation_up_to_down);
            if (isGirdView) {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
                gridLayoutManager.setSmoothScrollbarEnabled(true);
                musicListAdapter = new MusicListAdapter(this, R.layout.item_song_horizontal, listSong, this, exoPlayer);
                binding.rcvSongs.setLayoutManager(gridLayoutManager);
            } else {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                linearLayoutManager.setSmoothScrollbarEnabled(true);
                musicListAdapter = new MusicListAdapter(this, R.layout.item_song_vertical, listSong, this, exoPlayer);
                binding.rcvSongs.setLayoutManager(linearLayoutManager);
            }

            if (binding.rcvSongs.getItemDecorationCount() > 0) {
                binding.rcvSongs.removeItemDecorationAt(0);
            }

            // Library Animation Recyclerview
//            ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(musicListAdapter);
//            scaleInAnimationAdapter.setDuration(5000);
//            scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());
//            scaleInAnimationAdapter.setFirstOnly(false);

            binding.rcvSongs.setAdapter(musicListAdapter);

            setDataFilter();

        });
        checkDataLoaded();
    }

    @Override
    public void onError(String message) {
        Log.d(TAG, "onError: " + message);
        isDataLoaded = false;
        checkDataLoaded();
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

    public static Uri artWorkSong(String albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumId));
    }

    @Override
    public void onClick(List<SongModel> listSong, @NonNull SongModel currentSong) {
//        showToast(currentSong.getTitle());

        FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.music_player) {
            @Override
            public void onBind(FullScreenDialog dialog, View v) {
                //View childView = v.findViewById(resId)...
                final int[] rotation = {0};
                TextView tvNameSong = v.findViewById(R.id.tvNameSong);
                tvNameSong.setSelected(true);
                ImageView imgSong = v.findViewById(R.id.imgSong);
                SeekBar seekBar = v.findViewById(R.id.seekBar);
                TextView tvCurrentTime = v.findViewById(R.id.tvCurrentTime);
                TextView tvTotalTime = v.findViewById(R.id.tvTotalTime);
                ImageView imagePlayPause = v.findViewById(R.id.play_pause);

                tvNameSong.setText(currentSong.getTitle());
                Uri albumArtwork = artWorkSong(currentSong.getAlbumId());
                Glide.with(v)
                        .load(albumArtwork)
                        .placeholder(R.drawable.icon_music) // Ảnh placeholder
                        .error(R.drawable.icon_music) // Ảnh hiển thị khi lỗi
                        .into(imgSong);
                tvTotalTime.setText(convertToMMSS(currentSong.getDuration()));

                exoPlayer.addListener(new Player.Listener() {

                    @Override
                    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                        Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                    }

                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        Player.Listener.super.onPlaybackStateChanged(playbackState);
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (exoPlayer != null) {
                            int temp = rotation[0]++;
                            seekBar.setProgress((int) exoPlayer.getCurrentPosition() / 1200);
                            tvCurrentTime.setText(convertToMMSS(String.valueOf(exoPlayer.getCurrentPosition())));
                            if (exoPlayer.isPlaying()) {
                                imgSong.setRotation(temp);
                                imagePlayPause.setImageResource(R.drawable.icon_action_pause_24);
                            } else {
                                imagePlayPause.setImageResource(R.drawable.icon_action_play_24);
                            }
                        }
                        new Handler().postDelayed(this, 50);

                    }
                });
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (exoPlayer != null && fromUser) {
                            exoPlayer.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

            }
        });
        showControllerSong(currentSong);

    }

    private void showControllerSong(SongModel currentSong) {
        binding.layoutControlSong.setVisibility(View.VISIBLE);
        binding.tvNameSong.setText(currentSong.getTitle());
        binding.tvNameSong.setSelected(true);
        Uri albumArtwork = artWorkSong(currentSong.getAlbumId());
        Glide.with(this)
                .load(albumArtwork)
                .placeholder(R.drawable.icon_music) // Ảnh placeholder
                .error(R.drawable.icon_music) // Ảnh hiển thị khi lỗi
                .into(binding.imgSong);

        binding.tvArtist.setText(currentSong.getArtist());

        binding.imgPlayPause.setOnClickListener(v -> {
            if (exoPlayer.isPlaying()) {
                exoPlayer.stop();
                binding.imgPlayPause.setImageResource(R.drawable.icon_play);
            } else {
                exoPlayer.prepare();
                binding.imgPlayPause.setImageResource(R.drawable.icon_pause);
            }
        });
        binding.layoutControlSong.setOnClickListener(v -> {
            FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.music_player) {
                @Override
                public void onBind(FullScreenDialog dialog, View v) {
                    //View childView = v.findViewById(resId)...
                    final int[] rotation = {0};
                    TextView tvNameSong = v.findViewById(R.id.tvNameSong);
                    tvNameSong.setSelected(true);
                    ImageView imgSong = v.findViewById(R.id.imgSong);
                    SeekBar seekBar = v.findViewById(R.id.seekBar);
                    TextView tvCurrentTime = v.findViewById(R.id.tvCurrentTime);
                    TextView tvTotalTime = v.findViewById(R.id.tvTotalTime);
                    ImageView imagePlayPause = v.findViewById(R.id.play_pause);

                    tvNameSong.setText(currentSong.getTitle());
                    Uri albumArtwork = artWorkSong(currentSong.getAlbumId());
                    Glide.with(v)
                            .load(albumArtwork)
                            .placeholder(R.drawable.icon_music) // Ảnh placeholder
                            .error(R.drawable.icon_music) // Ảnh hiển thị khi lỗi
                            .into(imgSong);
                    tvTotalTime.setText(convertToMMSS(currentSong.getDuration()));

                    exoPlayer.addListener(new Player.Listener() {

                        @Override
                        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                            Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                        }

                        @Override
                        public void onPlaybackStateChanged(int playbackState) {
                            Player.Listener.super.onPlaybackStateChanged(playbackState);
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (exoPlayer != null) {
                                int temp = rotation[0]++;
                                seekBar.setProgress((int) exoPlayer.getCurrentPosition() / 1200);
                                tvCurrentTime.setText(convertToMMSS(String.valueOf(exoPlayer.getCurrentPosition())));
                                if (exoPlayer.isPlaying()) {
                                    imgSong.setRotation(temp);
                                    imagePlayPause.setImageResource(R.drawable.icon_action_pause_24);
                                } else {
                                    imagePlayPause.setImageResource(R.drawable.icon_action_play_24);
                                }
                            }
                            new Handler().postDelayed(this, 50);

                        }
                    });
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (exoPlayer != null && fromUser) {
                                exoPlayer.seekTo(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                }
            });
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void changeLayoutController() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BottomDialog.show(
                            new OnBindView<BottomDialog>(R.layout.layout_controller_music) {
                                @Override
                                public void onBind(BottomDialog dialog, View v) {
                                    //v.findViewById...
                                }
                            })
                    .setBackgroundColor(getColor(R.color.gray_dark));
        }

    }

    private int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }

        Random random = new Random();
        if (generatedNumbers.size() == songList.size()) {
            generatedNumbers.clear();
        }

        int randomNumber;

        do {
            randomNumber = random.nextInt(max) + min;

        } while (!generatedNumbers.add(randomNumber));

        return randomNumber;
    }

    private void updateBadgeCountNew(@NonNull MenuItem menuItem, int count) {
        badgeCount = count;

        View actionViewNotification = menuItem.getActionView();
        actionViewNotification.setOnClickListener(v -> {
            showToast("Open Notification Activity");
            changeLayoutController();
        });
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

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                timer.cancel();
                timer = new Timer();
                long DELAY = 500;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> musicListAdapter.getFilter().filter(newText));
                    }
                }, DELAY);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        // check permission

        if (!checkPermission()) {
            requestPermission();
            return;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getMusicPresenter.getMusicInLocal();
                showToast("onStart");
            }
        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer.isPlaying()) {
            exoPlayer.stop();
        }
        exoPlayer.release();
    }

    @Override
    protected void onResume() {
        showToast("onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        showToast("onReStart");
        super.onRestart();
    }
}
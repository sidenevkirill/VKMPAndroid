package ru.lisdevs.vkmp;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.lisdevs.vkmp.friends.FriendsFragment;
import ru.lisdevs.vkmp.friends.FriendsSearchFragment;
import ru.lisdevs.vkmp.groups.GroupsTabsFragment;
import ru.lisdevs.vkmp.music.MusicListFragment;
import ru.lisdevs.vkmp.music.MyMusicFragment;
import ru.lisdevs.vkmp.music.MyMusicTabsFragment;
import ru.lisdevs.vkmp.player.MiniPlayer;
import ru.lisdevs.vkmp.player.PlayerBottomSheetFragment;
import ru.lisdevs.vkmp.playlists.VkPlaylistsFragment;
import ru.lisdevs.vkmp.search.MusicSearchFragment;
import ru.lisdevs.vkmp.service.MusicPlayerService;
import ru.lisdevs.vkmp.utils.TokenManager;

public class BaseActivity extends AppCompatActivity
        implements MusicListFragment.OnMusicControlListener,
        PlayerBottomSheetFragment.PlayerInteractionListener {

    private MiniPlayer miniPlayer;
    private PlayerBottomSheetFragment playerBottomSheet;
    private MusicPlayerService musicService;
    private boolean isServiceBound = false;
    private BroadcastReceiver playerStateReceiver;
    private BottomNavigationView bottomNavigationView; // Добавляем переменную

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        initializeMiniPlayer();
        setupBottomNavigation();
        bindMusicService();
        setupPlayerStateReceiver();
        setupProfileTabIcon(); // Добавляем настройку иконки профиля
    }

    private void setupProfileTabIcon() {
        Menu menu = bottomNavigationView.getMenu();
        MenuItem profileItem = menu.findItem(R.id.item_account); // Используем существующий ID

        // Создаем кастомный View для иконки
        LayoutInflater inflater = LayoutInflater.from(this);
        View customView = inflater.inflate(R.layout.profile_tab_icon, null);
        ImageView profileIcon = customView.findViewById(R.id.profile_icon);

        // Устанавливаем кастомный View
        profileItem.setActionView(customView);

        // Загружаем аватар
        loadProfileAvatar(profileIcon);

        // Обработчик клика уже есть в setupBottomNavigation, поэтому не дублируем
    }

    private void loadProfileAvatar(ImageView profileIcon) {
        String accessToken = TokenManager.getInstance(this).getToken();
        if (accessToken == null) {
            // Устанавливаем placeholder если токена нет
            profileIcon.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        String url = "https://api.vk.com/method/users.get" +
                "?access_token=" + accessToken +
                "&v=5.131" +
                "&fields=photo_50";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ProfileTab", "Failed to load avatar", e);
                runOnUiThread(() -> profileIcon.setImageResource(R.drawable.ic_profile_placeholder));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (json.has("response")) {
                            JSONArray users = json.getJSONArray("response");
                            if (users.length() > 0) {
                                String photoUrl = users.getJSONObject(0).optString("photo_50");
                                runOnUiThread(() -> {
                                    Glide.with(BaseActivity.this)
                                            .load(photoUrl)
                                            .circleCrop()
                                            .placeholder(R.drawable.ic_profile_placeholder)
                                            .error(R.drawable.ic_profile_placeholder)
                                            .into(profileIcon);
                                });
                            } else {
                                runOnUiThread(() -> profileIcon.setImageResource(R.drawable.ic_profile_placeholder));
                            }
                        } else {
                            runOnUiThread(() -> profileIcon.setImageResource(R.drawable.ic_profile_placeholder));
                        }
                    } catch (JSONException e) {
                        Log.e("ProfileTab", "Error parsing avatar", e);
                        runOnUiThread(() -> profileIcon.setImageResource(R.drawable.ic_profile_placeholder));
                    }
                } else {
                    runOnUiThread(() -> profileIcon.setImageResource(R.drawable.ic_profile_placeholder));
                }
            }
        });
    }

    private void initializeMiniPlayer() {
        LinearLayout miniPlayerContainer = findViewById(R.id.mini_player_container);
        TextView trackTitle = findViewById(R.id.currentTrackText);
        TextView artistName = findViewById(R.id.currentTrackTitle);
        ImageView playPauseButton = findViewById(R.id.playPauseButton);
        ImageButton nextButton = findViewById(R.id.miniPlayerNext);
        ImageButton prevButton = findViewById(R.id.miniPlayerprevious);

        miniPlayer = new MiniPlayer(this, miniPlayerContainer, artistName,
                trackTitle, playPauseButton, nextButton, prevButton);

        miniPlayerContainer.setOnClickListener(v -> showFullPlayer());
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.item_friends:
                    changeFragment(new MyMusicTabsFragment());
                    return true;
                case R.id.item_groups:
                    changeFragment(new MusicSearchFragment());
                    return true;
                case R.id.item_search:
                    changeFragment(new FriendsFragment());
                    return true;
                case R.id.item_account:
                    changeFragment(new MyMusicTabsFragment());
                    return true;
            }
            return false;
        });

        if (getSupportFragmentManager().findFragmentById(R.id.container) == null) {
            changeFragment(new MyMusicFragment());
        }
    }

    private void bindMusicService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
            updatePlayerUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void setupPlayerStateReceiver() {
        playerStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updatePlayerUI();
                if (playerBottomSheet != null && playerBottomSheet.isVisible()) {
                    playerBottomSheet.updatePlayerUI();
                }
            }
        };

        IntentFilter filter = new IntentFilter("PLAYER_STATE_CHANGED");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playerStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(playerStateReceiver, filter);
        }
    }

    private void showFullPlayer() {
        if (musicService == null || musicService.getCurrentTrackTitle() == null ||
                musicService.getCurrentTrackTitle().equals("Unknown Track")) {
            return;
        }

        if (playerBottomSheet == null) {
            playerBottomSheet = PlayerBottomSheetFragment.newInstance();
        }
        playerBottomSheet.show(getSupportFragmentManager(), "player_bottom_sheet");
    }

    private void updatePlayerUI() {
        if (isServiceBound && musicService != null) {
            TextView trackTitle = findViewById(R.id.currentTrackText);
            TextView artistName = findViewById(R.id.currentTrackTitle);
            ImageView playPauseButton = findViewById(R.id.playPauseButton);
            LinearLayout miniPlayerContainer = findViewById(R.id.mini_player_container);

            trackTitle.setText(musicService.getCurrentTrackTitle());
            artistName.setText(musicService.getCurrentArtist());
            playPauseButton.setImageResource(
                    musicService.isPlaying() ? R.drawable.pause_black : R.drawable.play_black);

            if (musicService.getCurrentTrackTitle() != null &&
                    !musicService.getCurrentTrackTitle().equals("Unknown Track")) {
                miniPlayerContainer.setVisibility(View.VISIBLE);
            } else {
                miniPlayerContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPlayAudio(String url, String title) {
        if (isServiceBound && musicService != null) {
            Intent intent = new Intent(this, MusicPlayerService.class);
            intent.setAction(MusicPlayerService.ACTION_PLAY);
            intent.putExtra("URL", url);
            intent.putExtra("TITLE", title);
            intent.putExtra("ARTIST", (String) null);
            intent.putExtra("COVER_URL", (String) null);
            startService(intent);

            updatePlayerUI();
            showFullPlayer();
        }
    }

    @Override
    public void onTogglePlayPause() {
        if (isServiceBound && musicService != null) {
            musicService.togglePlayPause();
        }
    }

    @Override
    public void onNext() {
        if (isServiceBound && musicService != null) {
            if (musicService.getPlaylist() == null || musicService.getPlaylist().isEmpty()) {
                showToast("Нет треков для воспроизведения");
                return;
            }

            musicService.playNext();

            new Handler().postDelayed(() -> {
                updatePlayerUI();
                if (playerBottomSheet != null && playerBottomSheet.isVisible()) {
                    playerBottomSheet.updatePlayerUI();
                }
            }, 300);
        }
    }

    @Override
    public void onPrevious() {
        if (isServiceBound && musicService != null) {
            if (musicService.getPlaylist() == null || musicService.getPlaylist().isEmpty()) {
                showToast("Нет треков для воспроизведения");
                return;
            }

            musicService.playPrevious();

            new Handler().postDelayed(() -> {
                updatePlayerUI();
                if (playerBottomSheet != null && playerBottomSheet.isVisible()) {
                    playerBottomSheet.updatePlayerUI();
                }
            }, 300);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNextClicked() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_NEXT);
        startService(intent);
    }

    @Override
    public void onPreviousClicked() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_PREVIOUS);
        startService(intent);
    }

    @Override
    public void onPlayPauseClicked() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_TOGGLE);
        startService(intent);
    }

    @Override
    public void onSeekTo(int position) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_SEEK);
        intent.putExtra("POSITION", position);
        startService(intent);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        // Реализация при необходимости
    }

    @Override
    public void onShuffleModeChanged(boolean shuffleMode) {
        // Реализация при необходимости
    }

    @Override
    public void onPlayerClosed() {
        // Обработка закрытия плеера
    }

    private void changeFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onPlaylists(View v) {
        VkPlaylistsFragment nextFrag = new VkPlaylistsFragment();

        BaseActivity.this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, nextFrag, "findThisFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        if (playerStateReceiver != null) {
            unregisterReceiver(playerStateReceiver);
        }
        if (miniPlayer != null) {
            miniPlayer.release();
        }
    }
}
package ru.lisdevs.vkmp.local;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import ru.lisdevs.vkmp.R;
import ru.lisdevs.vkmp.friends.FriendsFragment;
import ru.lisdevs.vkmp.friends.FriendsSearchFragment;
import ru.lisdevs.vkmp.music.LocalMusicFragment;
import ru.lisdevs.vkmp.music.MusicListFragment;
import ru.lisdevs.vkmp.player.MiniPlayer;


public class LocalActivity extends AppCompatActivity implements MusicListFragment.OnMusicControlListener {
    private static final Long GROUP_ID = Long.valueOf(71746274);
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";

    private MiniPlayer miniPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_local);

        SharedPreferences prefs = getSharedPreferences("VK", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        // Инициализация MiniPlayer
        LinearLayout miniPlayerContainer = findViewById(R.id.mini_player_container);
        TextView artistTextView = findViewById(R.id.miniPlayerTitle);
        TextView titleTextView = findViewById(R.id.currentTrackText);
        @SuppressLint("WrongViewCast") ImageView playPauseButton = findViewById(R.id.playPauseButton);
        ImageButton nextButton = findViewById(R.id.miniPlayerNext);
        ImageButton prevButton = findViewById(R.id.miniPlayerprevious);

        miniPlayer = new MiniPlayer(this, miniPlayerContainer, artistTextView,
                titleTextView, playPauseButton, nextButton, prevButton);

        //showBottomSheetIfFirstLaunch();

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.item_friends:
                    changeFragment(new LocalMusicFragment());
                    return true;
                case R.id.item_groups:
                    changeFragment(new LocalPlaylistsFragment());
                    return true;
                case R.id.item_search:
                    changeFragment(new FriendsFragment());
                    return true;
                case R.id.item_account:
                    changeFragment(new FriendsFragment());
                    return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            changeFragment(new LocalMusicFragment());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (miniPlayer != null) {
            miniPlayer.release();
        }
    }

    @Override
    public void onPlayAudio(String url, String title) {
        if (miniPlayer != null) {
            miniPlayer.play(url, title, null);
        }
    }

    @Override
    public void onTogglePlayPause() {
        if (miniPlayer != null) {
            miniPlayer.togglePlayPause();
        }
    }

    @Override
    public void onNext() {
        if (miniPlayer != null) {
            miniPlayer.playNext();
        }
    }

    @Override
    public void onPrevious() {
        if (miniPlayer != null) {
            miniPlayer.playPrevious();
        }
    }

    // Остальные методы остаются без изменений
    private void changeFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void showBottomSheetIfFirstLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);

        if (isFirstLaunch) {
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
            showBottomSheet();
        }
    }

    private void showBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_subscribe, null);
        bottomSheetDialog.setContentView(view);

        Button btnSubscribeBottom = view.findViewById(R.id.btnSubscribeBottom);
        btnSubscribeBottom.setOnClickListener(v -> {
            new LocalActivity.SubscribeTask().execute(GROUP_ID);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private class SubscribeTask extends AsyncTask<Long, Void, Void> {
        @Override
        protected Void doInBackground(Long... params) {

            return null;
        }
    }
}
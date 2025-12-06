package ru.lisdevs.vkmp.settings;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.lisdevs.vkmp.R;
import ru.lisdevs.vkmp.about.AboutFragment;
import ru.lisdevs.vkmp.auth.AuthActivity;
import ru.lisdevs.vkmp.playlists.VkPlaylistsFragment;
import ru.lisdevs.vkmp.utils.TokenManager;

public class SettingsFragment extends Fragment {

    private static final String PREF_NAME = "VK_PREFS";
    private static final String PREF_DARK_THEME = "dark_theme_enabled";
    private static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";

    private Toolbar toolbar;
    private SwitchCompat themeSwitch;
    private SwitchCompat notificationsSwitch;

    // Добавляем элементы для отображения профиля
    private TextView userNameTextView;
    private ImageView userAvatarImageView;
    private String userId;
    private String userFirstName = "";
    private String userLastName = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Устанавливаем флаг, что фрагмент имеет меню опций
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Инициализируем Toolbar
        toolbar = view.findViewById(R.id.toolbar);
        setupToolbar();

        // Инициализация элементов профиля
        initProfileViews(view);

        // Настройка переключателя темы
        setupThemeSwitch(view);

        // Настройка переключателя уведомлений
        setupNotificationsSwitch(view);

        RelativeLayout logoutButton = view.findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(v -> {
            logout(requireContext());
            navigateToLogin();
        });

        RelativeLayout equalizerButton = view.findViewById(R.id.btn_equalizer);
        if (equalizerButton != null) {
            equalizerButton.setOnClickListener(v -> openSystemEqualizer());
        }

        RelativeLayout aboutButton = view.findViewById(R.id.btn_about);
        if (aboutButton != null) {
            aboutButton.setOnClickListener(v -> navigateAbout());
        }

        // Загружаем данные пользователя
        loadUserProfile();

        return view;
    }

    private void navigateAbout() {
        AboutFragment aboutFragment = new AboutFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, aboutFragment)
                .addToBackStack(null)
                .commit();
    }

    @SuppressLint("WrongViewCast")
    private void initProfileViews(View view) {
        userNameTextView = view.findViewById(R.id.user_name);
        userAvatarImageView = view.findViewById(R.id.avatars);

        // Получаем userId из TokenManager
        userId = TokenManager.getInstance(requireContext()).getUserId();

        // Устанавливаем стандартные значения
        if (userId != null) {
            userNameTextView.setText("Загрузка...");
        } else {
            userNameTextView.setText("Гость");
        }
    }

    private void setupToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);

            // ВАЖНО: Устанавливаем стрелку назад
            if (activity.getSupportActionBar() != null) {
                // Показываем кнопку "Назад"
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

                // Устанавливаем заголовок (опционально)
                activity.getSupportActionBar().setTitle("Настройки");

                // Убираем заголовок приложения (опционально)
                activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
        }

        toolbar.setNavigationOnClickListener(v -> {
            // Возвращаемся назад
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Обработка нажатия на стрелку "Назад"
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupThemeSwitch(View view) {
        themeSwitch = view.findViewById(R.id.theme_switch);

        // Загружаем сохраненное состояние темы
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isDarkTheme = prefs.getBoolean(PREF_DARK_THEME, false);

        // Устанавливаем состояние переключателя
        themeSwitch.setChecked(isDarkTheme);

        // Обработчик изменения темы
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveThemePreference(isChecked);
            applyTheme(isChecked);
        });
    }

    private void setupNotificationsSwitch(View view) {
        notificationsSwitch = view.findViewById(R.id.notifications_switch);

        // Загружаем сохраненное состояние уведомлений
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, true); // По умолчанию включены

        // Устанавливаем состояние переключателя
        notificationsSwitch.setChecked(notificationsEnabled);

        // Обработчик изменения состояния уведомлений
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationsPreference(isChecked);
            applyNotificationsSetting(isChecked);
        });
    }

    private void saveThemePreference(boolean isDarkTheme) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_DARK_THEME, isDarkTheme).apply();
    }

    private void saveNotificationsPreference(boolean notificationsEnabled) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_NOTIFICATIONS_ENABLED, notificationsEnabled).apply();
    }

    private void applyTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Перезапускаем активность для применения темы
        requireActivity().recreate();
    }

    private void applyNotificationsSetting(boolean notificationsEnabled) {
        if (notificationsEnabled) {
            // Включаем уведомления
            enableNotifications();
            Toast.makeText(requireContext(), "Уведомления включены", Toast.LENGTH_SHORT).show();
        } else {
            // Отключаем уведомления
            disableNotifications();
            Toast.makeText(requireContext(), "Уведомления отключены", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableNotifications() {
        // Здесь можно добавить логику включения уведомлений
        broadcastNotificationsState(true);
    }

    private void disableNotifications() {
        // Отключаем все активные уведомления
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        // Сообщаем MessagesFragment об изменении настроек уведомлений
        broadcastNotificationsState(false);
    }

    private void broadcastNotificationsState(boolean enabled) {
        // Отправляем broadcast о изменении состояния уведомлений
        Intent intent = new Intent("NOTIFICATIONS_STATE_CHANGED");
        intent.putExtra("notifications_enabled", enabled);
        requireContext().sendBroadcast(intent);
    }

    // Статический метод для проверки состояния уведомлений
    public static boolean areNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
    }

    // Метод для загрузки профиля пользователя
    private void loadUserProfile() {
        String accessToken = TokenManager.getInstance(requireContext()).getToken();
        if (accessToken == null || userId == null) {
            userNameTextView.setText("Не авторизован");
            return;
        }

        String url = "https://api.vk.com/method/users.get" +
                "?user_ids=" + userId +
                "&access_token=" + accessToken +
                "&fields=photo_100" +
                "&v=5.131";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "KateMobileAndroid/56 lite-447 (Android 6.0; SDK 23; x86; Google Android SDK built for x86; en)")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        userNameTextView.setText("Ошибка соединения"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);

                    if (json.has("error")) {
                        JSONObject error = json.getJSONObject("error");
                        String errorMsg = error.getString("error_msg");
                        requireActivity().runOnUiThread(() ->
                                userNameTextView.setText("Ошибка: " + errorMsg));
                        return;
                    }

                    JSONArray users = json.getJSONArray("response");
                    if (users.length() == 0) {
                        requireActivity().runOnUiThread(() ->
                                userNameTextView.setText("Пользователь не найден"));
                        return;
                    }

                    JSONObject user = users.getJSONObject(0);
                    userFirstName = user.getString("first_name");
                    userLastName = user.getString("last_name");
                    String fullName = userFirstName + " " + userLastName;
                    String photoUrl = user.optString("photo_100", null);

                    requireActivity().runOnUiThread(() -> {
                        userNameTextView.setText(fullName);
                        // Здесь можно загрузить аватарку
                        // if (photoUrl != null) {
                        //     Glide.with(requireContext()).load(photoUrl).into(userAvatarImageView);
                        // }
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() ->
                            userNameTextView.setText("Ошибка загрузки"));
                }
            }
        });
    }

    // Метод для показа информации о профиле
    private void showProfileInfo() {
        if (userId == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Информация о профиле")
                .setMessage("ID пользователя: " + userId +
                        "\nИмя: " + userFirstName +
                        "\nФамилия: " + userLastName)
                .setPositiveButton("OK", null)
                .show();
    }

    // Метод для открытия системного эквалайзера
    private void openSystemEqualizer() {
        try {
            Intent equalizerIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            equalizerIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
            equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().getPackageName());

            PackageManager pm = requireContext().getPackageManager();
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(equalizerIntent, 0);

            if (resolveInfos.isEmpty()) {
                showEqualizerNotAvailableToast();
            } else {
                startActivity(equalizerIntent);
            }

        } catch (ActivityNotFoundException e) {
            Log.e("SettingsFragment", "Equalizer activity not found", e);
            showEqualizerNotAvailableToast();
        } catch (Exception e) {
            Log.e("SettingsFragment", "Error opening equalizer", e);
            showEqualizerNotAvailableToast();
        }
    }

    private void showEqualizerNotAvailableToast() {
        Toast.makeText(requireContext(),
                "Системный эквалайзер не доступен на этом устройстве",
                Toast.LENGTH_LONG).show();
    }

    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("VK", MODE_PRIVATE);
        prefs.edit()
                .remove("access_token")
                .remove("user_id")
                .remove("full_name")
                .apply();
    }

    private static void clearCookies(Context context) {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
            cookieManager.flush();
        } else {
            CookieSyncManager.createInstance(context);
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        }
    }

    private static void clearWebViewData(Context context) {
        try {
            context.deleteDatabase("webview.db");
            context.deleteDatabase("webviewCache.db");
            clearAppCache(context);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                WebStorage.getInstance().deleteAllData();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clearAppCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }

            File webViewCacheDir = new File(context.getCacheDir(), "webview");
            if (webViewCacheDir.exists() && webViewCacheDir.isDirectory()) {
                deleteDir(webViewCacheDir);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir != null && dir.delete();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // При возвращении на фрагмент обновляем данные пользователя
        if (userId != null) {
            loadUserProfile();
        }

        // Обновляем состояние переключателя уведомлений
        updateNotificationsSwitchState();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Сбрасываем ActionBar, когда фрагмент уничтожается
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                // Можно сбросить настройки или оставить как есть
            }
        }
    }

    private void updateNotificationsSwitchState() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
        notificationsSwitch.setChecked(notificationsEnabled);
    }
}
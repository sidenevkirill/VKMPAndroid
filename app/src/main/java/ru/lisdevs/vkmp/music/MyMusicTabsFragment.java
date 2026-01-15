package ru.lisdevs.vkmp.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import ru.lisdevs.vkmp.R;
import ru.lisdevs.vkmp.about.AboutFragment;
import ru.lisdevs.vkmp.search.GroupsSearchFragment;
import ru.lisdevs.vkmp.settings.SettingsFragment;

public class MyMusicTabsFragment extends Fragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private GroupsPagerAdapter pagerAdapter;
    private Toolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_tabs, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupToolbar();
        setupViewPager();
        setupTabs();

        return view;
    }

    private void setupViewPager() {
        pagerAdapter = new GroupsPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
    }

    private void setupTabs() {
        // Для обычного ViewPager используем TabLayout.setupWithViewPager()
        tabLayout.setupWithViewPager(viewPager);

        // Устанавливаем текст для табов
        if (tabLayout.getTabCount() >= 2) {
            TabLayout.Tab tab1 = tabLayout.getTabAt(0);
            if (tab1 != null) tab1.setText("Мои треки");

            TabLayout.Tab tab2 = tabLayout.getTabAt(1);
            if (tab2 != null) tab2.setText("Рекомендации");
        }
    }

    // Вложенный класс адаптера для ViewPager
    public static class GroupsPagerAdapter extends FragmentPagerAdapter {

        public GroupsPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MyMusicFragment.newInstance();
                case 1:
                    return RecommendationFragment.newInstance();
                default:
                    return MyMusicFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Мои треки";
                case 1:
                    return "Рекомендации";
                default:
                    return null;
            }
        }
    }

    private void setupToolbar() {
        if (toolbar != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            navigateToGroupsSearchFragment();
            return true;
        }
          if (item.getItemId() == R.id.action_about) {
              navigateAbout();
            return true;
        }
          if (item.getItemId() == R.id.action_settings) {
              navigateSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateSettings() {
        SettingsFragment settingsFragment = new SettingsFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, settingsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateAbout() {
        AboutFragment aboutFragment = new AboutFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, aboutFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToGroupsSearchFragment() {
        GroupsSearchFragment groupsSearchFragment = new GroupsSearchFragment();
        Bundle args = new Bundle();
        groupsSearchFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, groupsSearchFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
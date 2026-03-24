package com.example.guidego.ui.guide;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.guidego.R;
import com.example.guidego.databinding.ActivityGuideMainBinding;
import com.example.guidego.ui.chat.ChatListFragment;

public class GuideMainActivity extends AppCompatActivity {

    private ActivityGuideMainBinding binding;
    private GuideToursFragment toursFragment;
    private GuideTourRequestsFragment requestsFragment;
    private ChatListFragment chatListFragment;
    private GuideProfileFragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFragments();
        setupBottomNav();
    }

    private void initFragments() {
        toursFragment = new GuideToursFragment();
        requestsFragment = new GuideTourRequestsFragment();
        chatListFragment = new ChatListFragment();
        profileFragment = new GuideProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, toursFragment, "tours")
                .add(R.id.fragment_container, requestsFragment, "requests").hide(requestsFragment)
                .add(R.id.fragment_container, chatListFragment, "chat").hide(chatListFragment)
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .commit();
        activeFragment = toursFragment;
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_guide_tours) selected = toursFragment;
            else if (id == R.id.nav_guide_requests) selected = requestsFragment;
            else if (id == R.id.nav_guide_chat) selected = chatListFragment;
            else if (id == R.id.nav_guide_profile) selected = profileFragment;

            if (selected != null && selected != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment).show(selected).commit();
                activeFragment = selected;
            }
            return true;
        });
    }
}

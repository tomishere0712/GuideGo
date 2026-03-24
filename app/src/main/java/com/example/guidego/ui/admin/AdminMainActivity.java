package com.example.guidego.ui.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.guidego.R;
import com.example.guidego.databinding.ActivityAdminMainBinding;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private AdminUsersFragment usersFragment;
    private AdminGuidesFragment guidesFragment;
    private AdminToursFragment toursFragment;
    private AdminLocationsFragment locationsFragment;
    private AdminReviewsFragment reviewsFragment;
    private AdminProfileFragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFragments();
        setupBottomNav();
    }

    private void initFragments() {
        usersFragment = new AdminUsersFragment();
        guidesFragment = new AdminGuidesFragment();
        toursFragment = new AdminToursFragment();
        locationsFragment = new AdminLocationsFragment();
        reviewsFragment = new AdminReviewsFragment();
        profileFragment = new AdminProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, usersFragment, "users")
                .add(R.id.fragment_container, guidesFragment, "guides").hide(guidesFragment)
                .add(R.id.fragment_container, toursFragment, "tours").hide(toursFragment)
                .add(R.id.fragment_container, locationsFragment, "locations").hide(locationsFragment)
                .add(R.id.fragment_container, reviewsFragment, "reviews").hide(reviewsFragment)
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .commit();
        activeFragment = usersFragment;
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_admin_users) selected = usersFragment;
            else if (id == R.id.nav_admin_guides) selected = guidesFragment;
            else if (id == R.id.nav_admin_tours) selected = toursFragment;
            else if (id == R.id.nav_admin_locations) selected = locationsFragment;
            else if (id == R.id.nav_admin_reviews) selected = reviewsFragment;
            else if (id == R.id.nav_admin_profile) selected = profileFragment;

            if (selected != null && selected != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment).show(selected).commit();
                activeFragment = selected;
            }
            return true;
        });
    }
}

package com.example.guidego;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.guidego.databinding.ActivityMainBinding;
import com.example.guidego.ui.booking.BookingFragment;
import com.example.guidego.ui.cart.CartFragment;
import com.example.guidego.ui.home.HomeFragment;
import com.example.guidego.ui.profile.ProfileFragment;
import com.example.guidego.ui.search.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private CartFragment cartFragment;
    private BookingFragment bookingFragment;
    private ProfileFragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFragments();
        setupBottomNav();
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        cartFragment = new CartFragment();
        bookingFragment = new BookingFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "home")
                .add(R.id.fragment_container, searchFragment, "search").hide(searchFragment)
                .add(R.id.fragment_container, cartFragment, "cart").hide(cartFragment)
                .add(R.id.fragment_container, bookingFragment, "booking").hide(bookingFragment)
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .commit();
        activeFragment = homeFragment;
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) selected = homeFragment;
            else if (id == R.id.nav_search) selected = searchFragment;
            else if (id == R.id.nav_cart) selected = cartFragment;
            else if (id == R.id.nav_bookings) selected = bookingFragment;
            else if (id == R.id.nav_profile) selected = profileFragment;

            if (selected != null && selected != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment).show(selected).commit();
                activeFragment = selected;
            }
            return true;
        });
    }

    public void navigateToCart() { binding.bottomNav.setSelectedItemId(R.id.nav_cart); }
    public void navigateToBookings() { binding.bottomNav.setSelectedItemId(R.id.nav_bookings); }
    public void navigateToSearch() { binding.bottomNav.setSelectedItemId(R.id.nav_search); }
}
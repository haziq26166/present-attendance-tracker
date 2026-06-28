package com.example.present;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.present.databinding.ActivityUserMainBinding;

public class UserMainActivity extends AppCompatActivity {
    private ActivityUserMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Setup Toolbar and hide the Title text for a cleaner look
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_user);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // 2. Define User Top-Level Destinations
            // Adding them here prevents the "Back" arrow on main tabs and stop fragments from stacking
            // These IDs in UserMainActivity must match the IDs in bottom_nav_user.xml EXACTLY
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.userHomeFragment,
                    R.id.historyFragment,
                    R.id.scannerFragment,     // Matches the menu now
                    R.id.userProfileFragment  // Matches the menu now
            ).build();

            // 3. Link the Action Bar and Bottom Navigation to the Controller
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.bottomNavUser, navController);

            // 4. Control BottomNav Visibility (Hide it on sub-pages if any)
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                boolean isMainTab = id == R.id.userHomeFragment ||
                        id == R.id.historyFragment ||
                        id == R.id.scannerFragment ||
                        id == R.id.userProfileFragment;

                binding.bottomNavUser.setVisibility(isMainTab ? View.VISIBLE : View.GONE);
            });
        }
    }

    /**
     * Required to make the Up/Back button in the Toolbar work with the Navigation Component.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_user);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
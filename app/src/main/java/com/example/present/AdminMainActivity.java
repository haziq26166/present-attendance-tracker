package com.example.present;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.present.databinding.ActivityAdminMainBinding;

public class AdminMainActivity extends AppCompatActivity {
    private ActivityAdminMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the toolbar as the support action bar so NavigationUI can manage it
        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_admin);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // 1. Define Top-Level Destinations
            // This prevents the "Back" arrow from appearing on these screens and
            // tells the NavController to treat them as roots (no locking).
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.adminHomeFragment,
                    R.id.eventListFragment,
                    R.id.createEventFragment,
                    R.id.adminProfileFragment
            ).build();

            // 2. Link Action Bar and Bottom Navigation to NavController
            // setupActionBarWithNavController ensures the title updates and back button behavior is correct
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            // setupWithNavController handles the bottom icon clicks
            NavigationUI.setupWithNavController(binding.bottomNavAdmin, navController);

            // 3. Control BottomNav Visibility
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                boolean isMainTab = id == R.id.adminHomeFragment ||
                        id == R.id.eventListFragment ||
                        id == R.id.createEventFragment ||
                        id == R.id.adminProfileFragment;

                binding.bottomNavAdmin.setVisibility(isMainTab ? View.VISIBLE : View.GONE);
            });
        }
    }

    /**
     * This method is required to make the Up button in the Action Bar
     * work with the Navigation Component.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_admin);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
package com.example.present.ui.admin.create;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.present.R;
import com.example.present.data.repository.AuthRepository;
import com.example.present.data.repository.EventRepository;
import com.example.present.databinding.FragmentCreateEventBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

public class CreateEventFragment extends Fragment implements OnMapReadyCallback {
    private FragmentCreateEventBinding binding;
    private CreateEventViewModel viewModel;
    private GoogleMap googleMap;
    private Marker venueMarker;
    private Circle geofenceCircle;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean isLocationPermissionGranted = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CreateEventViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RESET LOGIC: If returning to this tab after a success, start fresh at Step 1
        if (viewModel.step.getValue() == CreateEventViewModel.CreateStep.DONE ||
                binding.layoutSuccess.getVisibility() == View.VISIBLE) {
            resetCreateForm();
        }

        viewModel.step.observe(getViewLifecycleOwner(), this::handleStepChange);

        binding.btnPickDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
            picker.addOnPositiveButtonClickListener(selection -> {
                binding.tvSelectedDate.setText(picker.getHeaderText());
            });
            picker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        binding.btnPickStartTime.setOnClickListener(v -> showTimePicker("START"));
        binding.btnPickEndTime.setOnClickListener(v -> showTimePicker("END"));

        binding.btnNextStep1.setOnClickListener(v -> {
            if (validateStep1()) {
                try {
                    int capacity = Integer.parseInt(binding.etMaxCapacity.getText().toString());
                    viewModel.proceedToMap(
                            binding.etEventName.getText().toString(),
                            binding.etDescription.getText().toString(),
                            binding.tvSelectedDate.getText().toString(),
                            binding.tvStartTime.getText().toString(),
                            binding.tvEndTime.getText().toString(),
                            binding.etVenueName.getText().toString(),
                            capacity
                    );
                } catch (NumberFormatException e) {
                    binding.etMaxCapacity.setError("Enter a valid number");
                }
            }
        });

        binding.btnFinalizeEvent.setOnClickListener(v -> {
            if (venueMarker == null) {
                Toast.makeText(getContext(), "Please tap on the map to set a location", Toast.LENGTH_SHORT).show();
                return;
            }

            int radius = binding.seekBarRadius.getProgress();
            if (radius < 20) radius = 20;

            AuthRepository auth = new AuthRepository();
            viewModel.finalizeEvent(venueMarker.getPosition().latitude,
                    venueMarker.getPosition().longitude,
                    radius,
                    auth.getCurrentUser().getUid());

            viewModel.step.setValue(CreateEventViewModel.CreateStep.DONE);
        });

        binding.btnSaveToFirestore.setOnClickListener(v -> {
            binding.btnSaveToFirestore.setEnabled(false);
            binding.btnSaveToFirestore.setText("Creating Event...");

            new EventRepository().createEvent(viewModel.event, new EventRepository.EventCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (isAdded()) {
                        binding.btnSaveToFirestore.setEnabled(true);
                        binding.btnSaveToFirestore.setText("Confirm and Create");
                        showSuccessState();
                    }
                }
                @Override
                public void onError(String message) {
                    if (isAdded()) {
                        binding.btnSaveToFirestore.setEnabled(true);
                        binding.btnSaveToFirestore.setText("Confirm and Create");
                        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (binding.layoutSuccess.getVisibility() == View.VISIBLE) {
                            resetCreateForm();
                            Navigation.findNavController(requireView()).popBackStack();
                            return;
                        }

                        CreateEventViewModel.CreateStep currentStep = viewModel.step.getValue();
                        if (currentStep == CreateEventViewModel.CreateStep.MAP_GEOFENCE) {
                            viewModel.step.setValue(CreateEventViewModel.CreateStep.DETAILS);
                        } else if (currentStep == CreateEventViewModel.CreateStep.DONE) {
                            viewModel.step.setValue(CreateEventViewModel.CreateStep.MAP_GEOFENCE);
                        } else {
                            setEnabled(false);
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }

    private void showSuccessState() {
        binding.layoutFinal.setVisibility(View.GONE);
        binding.layoutSuccess.setVisibility(View.VISIBLE);
        binding.stepIndicatorAdmin.setStep(4);

        Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
    }

    private void resetCreateForm() {
        // 1. Clear all inputs
        binding.etEventName.setText("");
        binding.etDescription.setText("");
        binding.etMaxCapacity.setText("");
        binding.etVenueName.setText("");
        binding.tvSelectedDate.setText("No date selected");
        binding.tvStartTime.setText("00:00 AM");
        binding.tvEndTime.setText("00:00 AM");

        // 2. Clear Map
        if (venueMarker != null) venueMarker.remove();
        if (geofenceCircle != null) geofenceCircle.remove();
        venueMarker = null;
        geofenceCircle = null;

        // 3. Reset Geofence
        binding.seekBarRadius.setProgress(50);
        binding.tvRadiusLabel.setText("Geofence Radius: 50m");

        // 4. IMPORTANT: Reset ViewModel data and Step back to 1
        viewModel.resetViewModel();
    }

    private void showTimePicker(String type) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Event Time")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d %s",
                    (picker.getHour() == 0 || picker.getHour() == 12) ? 12 : picker.getHour() % 12,
                    picker.getMinute(),
                    picker.getHour() < 12 ? "AM" : "PM");

            if (type.equals("START")) binding.tvStartTime.setText(time);
            else binding.tvEndTime.setText(time);
        });

        picker.show(getParentFragmentManager(), "TIME_PICKER");
    }

    private boolean validateStep1() {
        if (TextUtils.isEmpty(binding.etEventName.getText())) {
            binding.etEventName.setError("Required");
            return false;
        }
        if (TextUtils.isEmpty(binding.etMaxCapacity.getText())) {
            binding.etMaxCapacity.setError("Required");
            return false;
        }
        if (binding.tvSelectedDate.getText().toString().equals("No date selected")) {
            Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void handleStepChange(CreateEventViewModel.CreateStep step) {
        binding.layoutDetails.setVisibility(View.GONE);
        binding.layoutMap.setVisibility(View.GONE);
        binding.layoutFinal.setVisibility(View.GONE);
        binding.layoutSuccess.setVisibility(View.GONE);

        if (step == CreateEventViewModel.CreateStep.DETAILS) {
            binding.layoutDetails.setVisibility(View.VISIBLE);
            binding.stepIndicatorAdmin.setStep(1);
        } else if (step == CreateEventViewModel.CreateStep.MAP_GEOFENCE) {
            binding.layoutMap.setVisibility(View.VISIBLE);
            binding.stepIndicatorAdmin.setStep(2);
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_create);
            if (mapFragment != null) mapFragment.getMapAsync(this);
        } else if (step == CreateEventViewModel.CreateStep.DONE) {
            binding.layoutFinal.setVisibility(View.VISIBLE);
            binding.stepIndicatorAdmin.setStep(3);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        checkAndRequestLocationPermission();
        LatLng defaultPos = new LatLng(-3.1390, 101.6869);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 15f));
        googleMap.setOnMapClickListener(this::setEventLocation);
        googleMap.setOnMapLongClickListener(this::setEventLocation);

        binding.seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int displayRadius = Math.max(progress, 20);
                binding.tvRadiusLabel.setText("Geofence Radius: " + displayRadius + "m");
                if (venueMarker != null) updateCircle(venueMarker.getPosition(), displayRadius);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * IMPROVED: Checks and requests location permission properly
     */
    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionGranted = true;
            enableMyLocation();
        } else {
            // Request permission
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * IMPROVED: Enables my location only if permission is granted
     */
    private void enableMyLocation() {
        if (googleMap == null) return;

        if (isLocationPermissionGranted) {
            try {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } catch (SecurityException e) {
                // Permission was revoked during runtime
                isLocationPermissionGranted = false;
                Toast.makeText(getContext(), "Location permission needed for map features", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Location permission needed to find your location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * IMPROVED: Properly handles permission request results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = true;
                enableMyLocation();
            } else {
                isLocationPermissionGranted = false;
                // Permission denied - inform user but continue with map functionality
                Toast.makeText(getContext(),
                        "Location permission denied. You can manually tap on the map to set venue location.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setEventLocation(LatLng latLng) {
        if (venueMarker != null) venueMarker.remove();
        venueMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Event Venue"));
        int radius = Math.max(binding.seekBarRadius.getProgress(), 20);
        updateCircle(latLng, radius);
    }

    private void updateCircle(LatLng center, int radius) {
        if (geofenceCircle != null) geofenceCircle.remove();
        geofenceCircle = googleMap.addCircle(new CircleOptions()
                .center(center)
                .radius(radius)
                .strokeColor(0xFF7C5CFC)
                .fillColor(0x337C5CFC)
                .strokeWidth(3f));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear map references to prevent memory leaks
        if (googleMap != null) {
            googleMap.setOnMapClickListener(null);
            googleMap.setOnMapLongClickListener(null);
        }
        binding = null;
    }
}
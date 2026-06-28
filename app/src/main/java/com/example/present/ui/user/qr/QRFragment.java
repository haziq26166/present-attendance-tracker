package com.example.present.ui.user.qr;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.present.R;
import com.example.present.data.model.AttendanceRecord;
import com.example.present.data.model.Event;
import com.example.present.data.repository.AttendanceRepository;
import com.example.present.data.repository.AuthRepository;
import com.example.present.databinding.FragmentQrBinding;
import com.example.present.util.BitmapUtils;
import com.example.present.util.DateTimeUtils;
import com.example.present.util.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRFragment extends Fragment {
    private FragmentQrBinding binding;
    private QRViewModel viewModel;
    private ExecutorService cameraExecutor;
    private FusedLocationProviderClient locationClient;

    // Updated: Using LocationCallback instead of Handler/Runnable loop
    private LocationCallback locationCallback;
    private boolean isGpsUpdating = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQrBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(QRViewModel.class);
        cameraExecutor = Executors.newSingleThreadExecutor();
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getCurrentStep().observe(getViewLifecycleOwner(), this::handleStepChange);
        binding.btnCapturePhoto.setOnClickListener(v -> takeSelfie());
        binding.btnBackHome.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void handleStepChange(QRViewModel.QRStep step) {
        if (binding == null) return;

        if (binding.stepIndicator != null) {
            binding.stepIndicator.setStep(step.ordinal() + 1);
        }

        binding.layoutQrScan.setVisibility(step == QRViewModel.QRStep.QR_SCAN ? View.VISIBLE : View.GONE);
        binding.layoutGpsCheck.setVisibility(step == QRViewModel.QRStep.GPS_CHECK ? View.VISIBLE : View.GONE);
        binding.layoutPhotoCapture.setVisibility(step == QRViewModel.QRStep.PHOTO_CAPTURE ? View.VISIBLE : View.GONE);
        binding.layoutDone.setVisibility(step == QRViewModel.QRStep.DONE ? View.VISIBLE : View.GONE);

        if (step == QRViewModel.QRStep.QR_SCAN) {
            stopGPSTracking(); // Safety
            startQRScanner();
        } else if (step == QRViewModel.QRStep.GPS_CHECK) {
            startGPSTracking();
        } else if (step == QRViewModel.QRStep.PHOTO_CAPTURE) {
            stopGPSTracking(); // Stop GPS once we move to camera
            startSelfieCamera();
        }
    }

    // ... (startQRScanner and processQrPayload remain the same) ...
    private void startQRScanner() {
        if (!PermissionUtils.hasPermissions(requireContext())) {
            PermissionUtils.requestPermissions(requireActivity());
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                BarcodeScanner scanner = BarcodeScanning.getClient();
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    @SuppressLint("UnsafeOptInUsageError")
                    InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                    scanner.process(image)
                            .addOnSuccessListener(barcodes -> {
                                for (Barcode barcode : barcodes) {
                                    String rawValue = barcode.getRawValue();
                                    if (rawValue != null) {
                                        processQrPayload(rawValue);
                                        cameraProvider.unbindAll();
                                        break;
                                    }
                                }
                            })
                            .addOnCompleteListener(task -> imageProxy.close());
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void processQrPayload(String json) {
        try {
            Event event = new Gson().fromJson(json, Event.class);

            // CRITICAL CHECK: Ensure the QR actually contains an ID
            if (event != null && event.eventId != null) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> viewModel.setScannedEvent(event));
                }
            } else {
                handleFailure("Invalid QR: This code doesn't have an Event ID.");
            }
        } catch (Exception e) {
            handleFailure("Invalid QR Code Format");
        }
    }

    @SuppressLint("MissingPermission")
    private void startGPSTracking() {
        if (isGpsUpdating) return; // Prevent multiple instances

        isGpsUpdating = true;
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null && binding != null) {
                    updateGpsUI(location);
                }
            }
        };

        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopGPSTracking() {
        isGpsUpdating = false;
        if (locationClient != null && locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void updateGpsUI(Location current) {
        Event event = viewModel.getScannedEvent().getValue();
        if (event == null || binding == null) return;

        float[] results = new float[1];
        Location.distanceBetween(current.getLatitude(), current.getLongitude(), event.venueLat, event.venueLng, results);
        float distance = results[0];

        binding.tvGpsStatus.setText(String.format("Distance: %.1fm", distance));
        binding.gpsProgress.setProgress((int) Math.max(0, 100 - (distance / 2)));

        // CHECK: If within radius, STOP GPS and MOVE to next step
        if (distance <= event.radiusMeters) {
            stopGPSTracking();
            viewModel.setGpsPassed(distance);
        }
    }

    // ... (Remaining methods: startSelfieCamera, takeSelfie, uploadAndFinalize, saveAttendanceToFirestore) ...
    private void startSelfieCamera() {
        if (binding == null) return;
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinderSelfie.getSurfaceProvider());

                ImageCapture imageCapture = new ImageCapture.Builder().build();
                binding.btnCapturePhoto.setTag(imageCapture);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageCapture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takeSelfie() {
        ImageCapture imageCapture = (ImageCapture) binding.btnCapturePhoto.getTag();
        if (imageCapture == null) return;

        binding.photoLoading.setVisibility(View.VISIBLE);

        imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy image) {
                try {
                    Bitmap bitmap = BitmapUtils.getBitmap(image);
                    // Compress to ~150KB so Firestore doesn't reject it for being too large
                    byte[] data = BitmapUtils.compressToSize(bitmap, 150);

                    // Convert to Base64 (Bypasses the need for Firebase Storage)
                    String base64Photo = android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
                    String dataUrl = "data:image/jpeg;base64," + base64Photo;

                    saveAttendanceToFirestore(dataUrl);
                } catch (Exception e) {
                    handleFailure("Photo Error: " + e.getMessage());
                } finally {
                    image.close();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                handleFailure("Camera Error: " + exception.getMessage());
            }
        });
    }

    private void handleFailure(String message) {
        if (isAdded() && binding != null) {
            requireActivity().runOnUiThread(() -> {
                binding.photoLoading.setVisibility(View.GONE);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            });
        }
    }

    private void saveAttendanceToFirestore(String photoDataString) {
        AuthRepository authRepo = new AuthRepository();
        com.google.firebase.auth.FirebaseUser currentUser = authRepo.getCurrentUser();

        if (currentUser == null) {
            handleFailure("Error: No user logged in.");
            return;
        }

        String uid = currentUser.getUid();
        String email = (currentUser.getEmail() != null) ? currentUser.getEmail() : "no_email";

        // 1. Fetch the actual name from the "users" collection in Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Get name from Firestore, fallback to Auth DisplayName, then fallback to email prefix
                    String actualName = documentSnapshot.getString("displayName");
                    if (actualName == null || actualName.isEmpty()) {
                        actualName = currentUser.getDisplayName();
                    }
                    if (actualName == null || actualName.isEmpty()) {
                        actualName = email.split("@")[0];
                    }

                    // Make a FINAL copy of the name to fix the "effectively final" error for the inner lambda
                    final String finalName = actualName;

                    Event event = viewModel.getScannedEvent().getValue();
                    if (event == null || event.eventId == null) {
                        handleFailure("Database Error: Missing Event ID.");
                        return;
                    }

                    // 2. Prepare the Attendance Record
                    AttendanceRecord record = new AttendanceRecord();
                    record.setUserId(uid);
                    record.setDisplayName(finalName); // Uses the actual name fetched from Firestore
                    record.setEmail(email);
                    record.setEventId(event.eventId);
                    record.setEventName(event.eventName);
                    record.setVenueName(event.venueName);
                    record.setCheckedInAt(Timestamp.now());
                    record.setPhotoUrl(photoDataString);
                    record.setStatus("verified");

                    List<String> methods = new ArrayList<>();
                    methods.add("qr");
                    methods.add("gps");
                    methods.add("photo");
                    record.setVerificationMethods(methods);

                    // 3. Save to Attendance Collection
                    new AttendanceRepository().submitRecord(record, new AttendanceRepository.AttendanceCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            if (isAdded() && binding != null) {
                                requireActivity().runOnUiThread(() -> {
                                    viewModel.setPhotoUploaded(photoDataString);
                                    binding.photoLoading.setVisibility(View.GONE);
                                    binding.tvDoneTime.setText("Checked in at: " + DateTimeUtils.formatTimestamp(System.currentTimeMillis()));
                                    viewModel.setCurrentStep(QRViewModel.QRStep.DONE);

                                    // This log now uses the 'final' version of the name string
                                    Log.d("QR_FLOW", "Attendance saved for: " + finalName);
                                });
                            }
                        }

                        @Override
                        public void onError(String message) {
                            handleFailure("Database Save Failed: " + message);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    handleFailure("Failed to fetch user profile: " + e.getMessage());
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        stopGPSTracking();
        binding = null;
    }
}
package com.example.present;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.present.databinding.ActivityQrDisplayBinding;
import com.example.present.util.QRCodeGenerator;

public class QRDisplayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActivityQrDisplayBinding binding = ActivityQrDisplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String payload = getIntent().getStringExtra("qrPayload");
        String eventName = getIntent().getStringExtra("eventName");
        String eventDate = getIntent().getStringExtra("eventDate");

        binding.tvEventName.setText(eventName);
        binding.tvEventDate.setText(eventDate);

        Bitmap qrBitmap = QRCodeGenerator.generate(payload, 800);
        if (qrBitmap != null) {
            binding.ivQrLarge.setImageBitmap(qrBitmap);
        }

        binding.btnClose.setOnClickListener(v -> finish());
    }
}
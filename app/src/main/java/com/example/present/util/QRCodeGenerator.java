package com.example.present.util;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeGenerator {
    public static Bitmap generate(String text, int size) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(matrix);
        } catch (Exception e) {
            return null;
        }
    }
}
package com.example.present.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.camera.core.ImageProxy;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BitmapUtils {
    public static Bitmap getBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static byte[] compressToSize(Bitmap bitmap, int maxKb) {
        int quality = 100;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        while (stream.toByteArray().length / 1024 > maxKb && quality > 10) {
            quality -= 10;
            stream.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        }
        return stream.toByteArray();
    }
}
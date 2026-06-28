package com.example.present.ui.user.qr;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ScanOverlayView extends View {
    private final Paint maskPaint = new Paint();
    private final Paint bracketPaint = new Paint();
    private final Paint linePaint = new Paint();
    private float scanLineY = 0;
    private final RectF scanRect = new RectF();

    public ScanOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        maskPaint.setColor(Color.parseColor("#99000000"));
        bracketPaint.setColor(Color.parseColor("#7C5CFC"));
        bracketPaint.setStrokeWidth(8f);
        bracketPaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.parseColor("#7C5CFC"));
        linePaint.setStrokeWidth(4f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float size = w * 0.7f;
        float left = (w - size) / 2;
        float top = (h - size) / 2;
        scanRect.set(left, top, left + size, top + size);

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "scanLineY", scanRect.top, scanRect.bottom);
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();
    }

    public void setScanLineY(float y) {
        this.scanLineY = y;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw Vignette
        Path path = new Path();
        path.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        path.addRoundRect(scanRect, 40, 40, Path.Direction.CCW);
        canvas.drawPath(path, maskPaint);

        // Draw Corners
        float len = 60f;
        canvas.drawLine(scanRect.left, scanRect.top, scanRect.left + len, scanRect.top, bracketPaint);
        canvas.drawLine(scanRect.left, scanRect.top, scanRect.left, scanRect.top + len, bracketPaint);

        canvas.drawLine(scanRect.right, scanRect.top, scanRect.right - len, scanRect.top, bracketPaint);
        canvas.drawLine(scanRect.right, scanRect.top, scanRect.right, scanRect.top + len, bracketPaint);

        canvas.drawLine(scanRect.left, scanRect.bottom, scanRect.left + len, scanRect.bottom, bracketPaint);
        canvas.drawLine(scanRect.left, scanRect.bottom, scanRect.left, scanRect.bottom - len, bracketPaint);

        canvas.drawLine(scanRect.right, scanRect.bottom, scanRect.right - len, scanRect.bottom, bracketPaint);
        canvas.drawLine(scanRect.right, scanRect.bottom, scanRect.right, scanRect.bottom - len, bracketPaint);

        // Draw Scan Line
        canvas.drawLine(scanRect.left, scanLineY, scanRect.right, scanLineY, linePaint);
    }
}
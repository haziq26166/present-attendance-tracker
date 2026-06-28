package com.example.present.ui.user.qr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class StepIndicatorView extends View {
    private int currentStep = 1;
    private int totalSteps = 4;
    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public StepIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        circlePaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        linePaint.setStrokeWidth(4f);
    }

    public void setStep(int step) {
        this.currentStep = step;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        float spacing = width / (totalSteps + 1);
        float centerY = height / 2;

        for (int i = 1; i <= totalSteps; i++) {
            float cx = spacing * i;

            // Draw Line to next
            if (i < totalSteps) {
                linePaint.setColor(i < currentStep ? Color.parseColor("#7C5CFC") : Color.GRAY);
                canvas.drawLine(cx, centerY, spacing * (i + 1), centerY, linePaint);
            }

            // Draw Circle
            circlePaint.setColor(i <= currentStep ? Color.parseColor("#7C5CFC") : Color.DKGRAY);
            canvas.drawCircle(cx, centerY, 30f, circlePaint);
            canvas.drawText(String.valueOf(i), cx, centerY + 12f, textPaint);
        }
    }
}
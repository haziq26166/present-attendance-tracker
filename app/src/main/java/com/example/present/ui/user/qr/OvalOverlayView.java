package com.example.present.ui.user.qr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class OvalOverlayView extends View {
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ovalRect = new RectF();

    public OvalOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        maskPaint.setColor(Color.parseColor("#CC000000"));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), maskPaint);
        float w = getWidth() * 0.7f;
        float h = w * 1.3f;
        float left = (getWidth() - w) / 2;
        float top = (getHeight() - h) / 2;
        ovalRect.set(left, top, left + w, top + h);
        canvas.drawOval(ovalRect, transparentPaint);
    }
}
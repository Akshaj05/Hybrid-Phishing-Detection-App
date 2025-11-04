package com.akshajramakrishnan.hybrid_phishing_detection.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class GradiantBarView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float scoreFraction = 0.0f; // 0.0 to 1.0

    private int textColor;
    private int pointerColor;

    public GradiantBarView(Context context) {
        super(context);
    }

    public GradiantBarView(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init() {
        setMinimumHeight(40);
        paint.setStrokeWidth(6f);

        int nightMode = getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            textColor = ContextCompat.getColor(getContext(), android.R.color.white);
            pointerColor = ContextCompat.getColor(getContext(), android.R.color.white);
        } else {
            textColor = ContextCompat.getColor(getContext(), android.R.color.black);
            pointerColor = ContextCompat.getColor(getContext(), android.R.color.black);
        }

    }

    // Set the score in range 0–100
    public void setScore(int score) {
        this.scoreFraction = Math.max(0, Math.min(score, 100)) / 100f;
        invalidate(); // force redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        // Gradient
        LinearGradient lg = new LinearGradient(
                0, 0, w, 0,
                new int[]{0xFF2E7D32, 0xFFFFA000, 0xFFC62828},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        );

        paint.setShader(lg);
        canvas.drawRect(0, h * 0.25f, w, h * 0.6f, paint);
        paint.setShader(null);

        // Draw pointer
        float px = scoreFraction * w;
        paint.setColor(pointerColor);
        canvas.drawLine(px, h * 0.1f, px, h * 0.75f, paint);

        // percentage text below pointer
        paint.setTextSize(35f);
        paint.setColor(textColor);
        String text = Math.round(scoreFraction * 100) + "%";
        float textWidth = paint.measureText(text);
        float x = Math.max(0, Math.min(px - textWidth / 2, w - textWidth));
        canvas.drawText(text, x, h * .99f, paint);
    }
}

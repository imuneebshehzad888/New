package com.muneeb.aimenbirthday;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Lightweight background decoration: hearts, butterflies, stars and sparkles that
 * slowly float upwards and twinkle. Drawn with plain Canvas text (emoji), no images.
 * Call pauseAnimation() / resumeAnimation() to save battery when the app is hidden.
 */
public class FloatingDecorView extends View {

    private static final String[] SYMBOLS = {
            "💗", "🦋", "✨", "⭐", "🌸", "💖", "🦋", "✨", "💗", "🌟", "🦋", "💕"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int count = SYMBOLS.length;
    private final float[] xFraction = new float[count];
    private final float[] size = new float[count];
    private final float[] speed = new float[count];
    private final float[] phase = new float[count];
    private final float[] sway = new float[count];

    private boolean running = true;
    private long lastNanos = 0;
    private float elapsed = 0f;

    public FloatingDecorView(Context context) {
        super(context);
        init();
    }

    public FloatingDecorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Random random = new Random(7);
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < count; i++) {
            xFraction[i] = 0.05f + random.nextFloat() * 0.9f;
            size[i] = (18f + random.nextFloat() * 18f) * density;
            speed[i] = 0.03f + random.nextFloat() * 0.04f; // cycles per second
            phase[i] = random.nextFloat();
            sway[i] = (8f + random.nextFloat() * 18f) * density;
        }
    }

    public void pauseAnimation() {
        running = false;
        lastNanos = 0;
    }

    public void resumeAnimation() {
        if (!running) {
            running = true;
            lastNanos = 0;
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }

        long now = System.nanoTime();
        if (lastNanos != 0) {
            float delta = (now - lastNanos) / 1_000_000_000f;
            elapsed += Math.min(delta, 0.1f);
        }
        lastNanos = now;

        for (int i = 0; i < count; i++) {
            float progress = (elapsed * speed[i] + phase[i]) % 1f;
            float y = h * (1f - progress) + size[i];
            float x = xFraction[i] * w
                    + (float) Math.sin(elapsed * 0.8f + phase[i] * 6.28f) * sway[i];
            float edgeFade = Math.min(1f, Math.min(progress, 1f - progress) * 5f);
            float twinkle = 0.55f + 0.35f * (float) Math.sin(elapsed * 1.6f + phase[i] * 10f);
            paint.setAlpha((int) (255 * twinkle * edgeFade));
            paint.setTextSize(size[i]);
            canvas.drawText(SYMBOLS[i], x, y, paint);
        }

        if (running) {
            postInvalidateOnAnimation();
        }
    }
}

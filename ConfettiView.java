package com.muneeb.aimenbirthday;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * One-shot confetti burst: falling paper pieces, hearts, stars, butterflies and sparkles.
 * Call start() to play (safe to call repeatedly) and stop() to cancel.
 * It does nothing (and uses no battery) when it is not playing.
 */
public class ConfettiView extends View {

    private static final String[] EMOJI = {"💗", "⭐", "🦋", "✨", "🌸", "💖"};
    private static final int[] COLORS = {
            0xFFFF6FA5, 0xFFB388FF, 0xFFFFD36B, 0xFFFF9ECF, 0xFF8EC5FF, 0xFFFFFFFF
    };
    private static final int COUNT = 70;
    private static final float TOTAL_SECONDS = 6f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] xFraction = new float[COUNT];
    private final float[] delay = new float[COUNT];
    private final float[] fallTime = new float[COUNT];
    private final float[] size = new float[COUNT];
    private final float[] sway = new float[COUNT];
    private final float[] spin = new float[COUNT];
    private final int[] kind = new int[COUNT]; // 0 = rectangle, 1 = circle, 2 = emoji
    private final int[] color = new int[COUNT];
    private final int[] emojiIndex = new int[COUNT];

    private boolean active = false;
    private long lastNanos = 0;
    private float elapsed = 0f;

    public ConfettiView(Context context) {
        super(context);
    }

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void start() {
        Random random = new Random();
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < COUNT; i++) {
            xFraction[i] = random.nextFloat();
            delay[i] = random.nextFloat() * 1.6f;
            fallTime[i] = 2.6f + random.nextFloat() * 1.8f;
            sway[i] = (10f + random.nextFloat() * 30f) * density;
            spin[i] = (random.nextFloat() - 0.5f) * 720f;
            color[i] = COLORS[random.nextInt(COLORS.length)];
            emojiIndex[i] = random.nextInt(EMOJI.length);
            int roll = random.nextInt(10);
            kind[i] = roll < 4 ? 0 : (roll < 6 ? 1 : 2);
            size[i] = (kind[i] == 2 ? 22f + random.nextFloat() * 12f : 7f + random.nextFloat() * 7f) * density;
        }
        elapsed = 0f;
        lastNanos = 0;
        active = true;
        postInvalidateOnAnimation();
    }

    public void stop() {
        active = false;
        lastNanos = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!active) {
            return;
        }
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }

        long now = System.nanoTime();
        if (lastNanos != 0) {
            elapsed += Math.min((now - lastNanos) / 1_000_000_000f, 0.1f);
        }
        lastNanos = now;

        if (elapsed > TOTAL_SECONDS) {
            active = false;
            return;
        }

        for (int i = 0; i < COUNT; i++) {
            float p = (elapsed - delay[i]) / fallTime[i];
            if (p < 0f || p > 1f) {
                continue;
            }
            float y = -size[i] + p * (h + 2f * size[i]);
            float x = xFraction[i] * w + (float) Math.sin(p * 12f + i) * sway[i];
            float fade = p > 0.85f ? (1f - p) / 0.15f : 1f;
            int alpha = (int) (255 * fade);

            if (kind[i] == 2) {
                paint.setColor(0xFFFFFFFF);
                paint.setAlpha(alpha);
                paint.setTextSize(size[i]);
                canvas.drawText(EMOJI[emojiIndex[i]], x, y, paint);
            } else {
                paint.setColor(color[i]);
                paint.setAlpha(alpha);
                if (kind[i] == 1) {
                    canvas.drawCircle(x, y, size[i] / 2f, paint);
                } else {
                    canvas.save();
                    canvas.rotate(spin[i] * p, x, y);
                    canvas.drawRect(x - size[i] / 2f, y - size[i] / 4f,
                            x + size[i] / 2f, y + size[i] / 4f, paint);
                    canvas.restore();
                }
            }
        }

        postInvalidateOnAnimation();
    }
}

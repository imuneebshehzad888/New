package com.muneeb.aimenbirthday;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * The whole app lives in this one Activity with three "screens":
 *   1. loginScreen  - password entry
 *   2. giftScreen   - "Open Your Surprise"
 *   3. finalScreen  - the birthday message
 */
public class MainActivity extends Activity {

    // =====================================================================
    // CHANGE THE PASSWORD HERE (case-sensitive; leading/trailing spaces ignored).
    // NOTE: a password stored inside an APK is NOT strong security.
    // =====================================================================
    private static final String SURPRISE_PASSWORD = "Neebu";

    private static final int MAX_CARD_WIDTH_DP = 480;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable releaseBusy = new Runnable() {
        @Override
        public void run() {
            busy = false;
        }
    };

    private FloatingDecorView decorView;
    private ConfettiView confettiView;
    private View loginScreen;
    private View giftScreen;
    private View finalScreen;
    private View currentScreen;
    private View loginCard;
    private View giftCard;
    private View finalCard;
    private EditText passwordInput;
    private TextView errorText;
    private Button unlockButton;
    private Button openButton;
    private Button celebrateButton;
    private Button backButton;
    private ImageView giftImage;
    private View[] revealViews;

    /** True while a transition is running; blocks rapid repeated taps. */
    private boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decorView = (FloatingDecorView) findViewById(R.id.decorView);
        confettiView = (ConfettiView) findViewById(R.id.confettiView);
        loginScreen = findViewById(R.id.loginScreen);
        giftScreen = findViewById(R.id.giftScreen);
        finalScreen = findViewById(R.id.finalScreen);
        loginCard = findViewById(R.id.loginCard);
        giftCard = findViewById(R.id.giftCard);
        finalCard = findViewById(R.id.finalCard);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        errorText = (TextView) findViewById(R.id.errorText);
        unlockButton = (Button) findViewById(R.id.unlockButton);
        openButton = (Button) findViewById(R.id.openButton);
        celebrateButton = (Button) findViewById(R.id.celebrateButton);
        backButton = (Button) findViewById(R.id.backButton);
        giftImage = (ImageView) findViewById(R.id.giftImage);

        revealViews = new View[]{
                findViewById(R.id.finalDeco),
                findViewById(R.id.finalTitle),
                findViewById(R.id.finalSubtitle),
                findViewById(R.id.finalName),
                findViewById(R.id.messageCard),
                findViewById(R.id.buttonRow)
        };

        currentScreen = loginScreen;
        applyCardWidth();

        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleUnlock();
            }
        });

        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleUnlock();
                    return true;
                }
                return false;
            }
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                errorText.setVisibility(View.GONE);
            }
        });

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSurprise();
            }
        });

        celebrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!busy) {
                    prepareReveal();
                    playReveal();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStart();
            }
        });
    }

    // ------------------------------------------------------------------
    // Password handling
    // ------------------------------------------------------------------

    private void handleUnlock() {
        if (busy) {
            return;
        }
        String entered = passwordInput.getText().toString().trim();

        if (entered.isEmpty()) {
            showError(R.string.error_empty);
            return;
        }

        if (entered.equals(SURPRISE_PASSWORD)) {
            errorText.setVisibility(View.GONE);
            passwordInput.setText("");
            switchScreen(giftScreen, null);
        } else {
            showError(R.string.error_wrong);
            shakeLoginCard();
        }
    }

    private void showError(int messageRes) {
        errorText.setText(messageRes);
        errorText.setVisibility(View.VISIBLE);
    }

    private void shakeLoginCard() {
        float d = dp(16);
        ObjectAnimator shake = ObjectAnimator.ofFloat(loginCard, "translationX",
                0f, -d, d, -d * 0.6f, d * 0.6f, 0f);
        shake.setDuration(380);
        shake.start();
    }

    // ------------------------------------------------------------------
    // Gift screen -> final screen
    // ------------------------------------------------------------------

    private void openSurprise() {
        if (busy) {
            return;
        }
        busy = true;
        openButton.setEnabled(false);

        ObjectAnimator wobble = ObjectAnimator.ofFloat(giftImage, "rotation",
                0f, -10f, 10f, -8f, 8f, -4f, 4f, 0f);
        wobble.setDuration(700);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(giftImage, "scaleX", 1f, 1.4f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(giftImage, "scaleY", 1f, 1.4f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(giftImage, "alpha", 1f, 0f);
        AnimatorSet burst = new AnimatorSet();
        burst.playTogether(scaleX, scaleY, fadeOut);
        burst.setDuration(350);

        AnimatorSet all = new AnimatorSet();
        all.playSequentially(wobble, burst);
        all.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                giftImage.setRotation(0f);
                giftImage.setScaleX(1f);
                giftImage.setScaleY(1f);
                giftImage.setAlpha(1f);
                openButton.setEnabled(true);
                busy = false;
                switchScreen(finalScreen, new Runnable() {
                    @Override
                    public void run() {
                        playReveal();
                    }
                });
            }
        });
        all.start();
    }

    /** Hides the final-screen pieces so they can animate in. */
    private void prepareReveal() {
        float offset = dp(28);
        for (View v : revealViews) {
            v.animate().cancel();
            v.setAlpha(0f);
            v.setTranslationY(offset);
        }
    }

    /** Animates the final-screen pieces in one by one and fires the confetti. */
    private void playReveal() {
        busy = true;
        handler.removeCallbacks(releaseBusy);
        handler.postDelayed(releaseBusy, 1500);
        for (int i = 0; i < revealViews.length; i++) {
            revealViews[i].animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * 140L)
                    .setDuration(500)
                    .start();
        }
        confettiView.start();
    }

    private void goToStart() {
        if (busy || currentScreen == loginScreen) {
            return;
        }
        confettiView.stop();
        passwordInput.setText("");
        errorText.setVisibility(View.GONE);
        switchScreen(loginScreen, null);
    }

    // ------------------------------------------------------------------
    // Screen switching
    // ------------------------------------------------------------------

    private void switchScreen(final View to, final Runnable onShown) {
        if (to == currentScreen) {
            return;
        }
        busy = true;
        hideKeyboard();
        final View from = currentScreen;
        currentScreen = to;

        if (to == finalScreen) {
            prepareReveal();
        }

        from.animate().alpha(0f).setDuration(250).withEndAction(new Runnable() {
            @Override
            public void run() {
                from.setVisibility(View.GONE);
                from.setAlpha(1f);
                to.setAlpha(0f);
                to.setVisibility(View.VISIBLE);
                if (to instanceof ScrollView) {
                    ((ScrollView) to).scrollTo(0, 0);
                }
                to.animate().alpha(1f).setDuration(350).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        busy = false;
                        if (onShown != null) {
                            onShown.run();
                        }
                    }
                }).start();
            }
        }).start();
    }

    // ------------------------------------------------------------------
    // Helpers and lifecycle
    // ------------------------------------------------------------------

    /** Keeps cards a comfortable width on tablets and large screens. */
    private void applyCardWidth() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int max = (int) dp(MAX_CARD_WIDTH_DP);
        int width = screenWidth - (int) dp(32) > max ? max : ViewGroup.LayoutParams.MATCH_PARENT;
        View[] cards = {loginCard, giftCard, finalCard};
        for (View card : cards) {
            ViewGroup.LayoutParams lp = card.getLayoutParams();
            lp.width = width;
            card.setLayoutParams(lp);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = getCurrentFocus();
        if (imm != null && focus != null) {
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyCardWidth();
    }

    @Override
    public void onBackPressed() {
        if (busy) {
            return;
        }
        if (currentScreen != loginScreen) {
            goToStart();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        decorView.resumeAnimation();
    }

    @Override
    protected void onPause() {
        decorView.pauseAnimation();
        confettiView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        confettiView.stop();
        super.onDestroy();
    }
}

package com.example.taplab;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

/** A self-contained learning demo: the agent can act only on this toy game view. */
public final class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Runnable agentStep = new Runnable() {
        @Override public void run() {
            if (!agentRunning) return;

            // Observe the toy game's state, choose its target, then act inside this app.
            PointF observedTarget = gameView.observeTarget();
            gameView.agentTap(observedTarget.x, observedTarget.y);
            updateStatus();
            handler.postDelayed(this, 700L);
        }
    };

    private TargetGameView gameView;
    private TextView scoreView;
    private TextView statusView;
    private boolean agentRunning;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildScreen();
    }

    @Override protected void onDestroy() {
        stopAgent();
        super.onDestroy();
    }

    private void buildScreen() {
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(0xFFF5F7FB);

        TextView title = new TextView(this);
        title.setText("Tap Lab");
        title.setTextSize(28);
        title.setTextColor(0xFF172033);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title, matchWrap());

        TextView intro = new TextView(this);
        intro.setText("A small sandbox for learning the observe → decide → act loop.");
        intro.setTextSize(15);
        intro.setTextColor(0xFF596579);
        LinearLayout.LayoutParams introParams = matchWrap();
        introParams.topMargin = dp(6);
        root.addView(intro, introParams);

        scoreView = new TextView(this);
        scoreView.setTextSize(17);
        scoreView.setTextColor(0xFF172033);
        scoreView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams scoreParams = matchWrap();
        scoreParams.topMargin = dp(16);
        root.addView(scoreView, scoreParams);

        gameView = new TargetGameView();
        LinearLayout.LayoutParams gameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        gameParams.topMargin = dp(12);
        gameParams.bottomMargin = dp(12);
        root.addView(gameView, gameParams);

        statusView = new TextView(this);
        statusView.setTextSize(14);
        statusView.setTextColor(0xFF596579);
        root.addView(statusView, matchWrap());

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams actionsParams = matchWrap();
        actionsParams.topMargin = dp(10);
        root.addView(actions, actionsParams);

        Button start = new Button(this);
        start.setText("Start agent");
        start.setOnClickListener(v -> startAgent());
        actions.addView(start, new LinearLayout.LayoutParams(0, dp(52), 1f));

        Button stop = new Button(this);
        stop.setText("Stop");
        stop.setOnClickListener(v -> stopAgent());
        LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(0, dp(52), 1f);
        stopParams.leftMargin = dp(8);
        actions.addView(stop, stopParams);

        Button reset = new Button(this);
        reset.setText("Reset");
        reset.setOnClickListener(v -> {
            stopAgent();
            gameView.resetScore();
            updateStatus();
        });
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(0, dp(52), 1f);
        resetParams.leftMargin = dp(8);
        actions.addView(reset, resetParams);

        setContentView(root);
        updateStatus();
    }

    private void startAgent() {
        if (agentRunning) return;
        agentRunning = true;
        handler.removeCallbacks(agentStep);
        handler.post(agentStep);
        updateStatus();
    }

    private void stopAgent() {
        agentRunning = false;
        handler.removeCallbacks(agentStep);
        if (statusView != null) updateStatus();
    }

    private void updateStatus() {
        if (scoreView == null || gameView == null || statusView == null) return;
        scoreView.setText(String.format(Locale.getDefault(), "Score: %d", gameView.getScore()));
        statusView.setText(agentRunning
                ? "Agent running in this demo · next action in about 0.7 seconds"
                : "Agent stopped · tap the circle yourself or start the demo agent");
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class TargetGameView extends View {
        private final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float targetX;
        private float targetY;
        private float radius;
        private int score;

        TargetGameView() {
            super(MainActivity.this);
            setBackgroundColor(0xFFFFFFFF);
            setContentDescription("Toy game area. Tap the colored circle to score.");
            targetPaint.setColor(0xFF5967F2);
            radius = dp(32);
            post(this::moveTarget);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            background.setColor(0xFFE9EDFF);
            canvas.drawRoundRect(0, 0, getWidth(), getHeight(), dp(18), dp(18), background);
            if (targetX == 0f && targetY == 0f) moveTarget();
            canvas.drawCircle(targetX, targetY, radius, targetPaint);
        }

        @Override public boolean performClick() {
            super.performClick();
            return true;
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent event) {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                performClick();
                handleTap(event.getX(), event.getY());
                return true;
            }
            return true;
        }

        PointF observeTarget() {
            // The demo exposes state directly so students can inspect the policy clearly.
            return new PointF(targetX, targetY);
        }

        void agentTap(float x, float y) {
            handleTap(x, y);
        }

        private void handleTap(float x, float y) {
            float dx = x - targetX;
            float dy = y - targetY;
            if ((dx * dx) + (dy * dy) <= radius * radius) {
                score++;
                moveTarget();
                updateStatus();
            }
        }

        private void moveTarget() {
            if (getWidth() <= radius * 2 || getHeight() <= radius * 2) return;
            targetX = radius + random.nextFloat() * (getWidth() - (radius * 2));
            targetY = radius + random.nextFloat() * (getHeight() - (radius * 2));
            invalidate();
        }

        int getScore() { return score; }

        void resetScore() {
            score = 0;
            moveTarget();
            invalidate();
        }
    }
}


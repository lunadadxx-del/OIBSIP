package com.pavan.oibsip.stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * OIBSIP Android Task 5 — Stopwatch.
 *
 * Timing engine: android.os.Handler + Runnable posting every 50 ms.
 * Elapsed time is derived from SystemClock.elapsedRealtime() (a monotonic
 * clock), so the displayed time stays accurate even if UI ticks are delayed
 * or the activity is paused / destroyed (rotation) in between.
 */
public class MainActivity extends Activity {

    private static final long TICK_INTERVAL_MS = 50L;

    private static final String KEY_RUNNING = "running";
    private static final String KEY_START_BASE = "startBase";
    private static final String KEY_ACCUMULATED = "accumulated";
    private static final String KEY_LAP_COUNT = "lapCount";
    private static final String KEY_LAPS = "laps";

    private TextView tvTime;
    private Button btnStart;
    private Button btnPause;
    private Button btnReset;
    private Button btnLap;
    private ListView lvLaps;

    private ArrayList<String> laps;
    private ArrayAdapter<String> lapAdapter;
    private int lapCount = 1;

    /** True while the stopwatch is actively timing. */
    private boolean running = false;
    /**
     * SystemClock.elapsedRealtime() value captured when the current running
     * segment started. Only meaningful while {@code running} is true.
     */
    private long startBase = 0L;
    /** Elapsed milliseconds accumulated from all finished segments. */
    private long accumulated = 0L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable ticker = this::tick;

    /** Single tick: refresh the display and schedule the next one. */
    private void tick() {
        updateDisplay();
        handler.postDelayed(ticker, TICK_INTERVAL_MS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTime = findViewById(R.id.tvTime);
        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        btnReset = findViewById(R.id.btnReset);
        btnLap = findViewById(R.id.btnLap);
        lvLaps = findViewById(R.id.lvLaps);

        laps = new ArrayList<>();
        lapAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, laps);
        lvLaps.setAdapter(lapAdapter);

        if (savedInstanceState != null) {
            running = savedInstanceState.getBoolean(KEY_RUNNING, false);
            startBase = savedInstanceState.getLong(KEY_START_BASE, 0L);
            accumulated = savedInstanceState.getLong(KEY_ACCUMULATED, 0L);
            lapCount = savedInstanceState.getInt(KEY_LAP_COUNT, 1);
            ArrayList<String> savedLaps = savedInstanceState.getStringArrayList(KEY_LAPS);
            if (savedLaps != null) {
                laps.addAll(savedLaps);
                lapAdapter.notifyDataSetChanged();
            }
        }

        btnStart.setOnClickListener(v -> start());
        btnPause.setOnClickListener(v -> pause());
        btnReset.setOnClickListener(v -> reset());
        btnLap.setOnClickListener(v -> recordLap());

        updateDisplay();
        updateButtons();
    }

    /** Starts timing from 0, or resumes from the paused state. */
    private void start() {
        if (running) {
            return;
        }
        startBase = SystemClock.elapsedRealtime();
        running = true;
        handler.post(ticker);
        updateButtons();
    }

    /** Freezes the timer at the current elapsed time. */
    private void pause() {
        if (!running) {
            return;
        }
        accumulated += SystemClock.elapsedRealtime() - startBase;
        running = false;
        handler.removeCallbacks(ticker);
        updateDisplay();
        updateButtons();
    }

    /** Stops the timer, resets the display to 00:00:00.00 and clears laps. */
    private void reset() {
        handler.removeCallbacks(ticker);
        running = false;
        startBase = 0L;
        accumulated = 0L;
        laps.clear();
        lapCount = 1;
        lapAdapter.notifyDataSetChanged();
        updateDisplay();
        updateButtons();
    }

    /** Records the current formatted time as a lap (newest first). */
    private void recordLap() {
        if (!running) {
            return;
        }
        String entry = "Lap " + lapCount + "  —  " + formatTime(elapsedMillis());
        laps.add(0, entry);
        lapCount++;
        lapAdapter.notifyDataSetChanged();
    }

    /** True elapsed time in milliseconds, anchored on the monotonic clock. */
    private long elapsedMillis() {
        if (running) {
            return accumulated + (SystemClock.elapsedRealtime() - startBase);
        }
        return accumulated;
    }

    private void updateDisplay() {
        tvTime.setText(formatTime(elapsedMillis()));
    }

    private void updateButtons() {
        btnStart.setEnabled(!running);
        btnPause.setEnabled(running);
        btnLap.setEnabled(running);
    }

    /** Formats milliseconds as HH:MM:SS.cs (hours, minutes, seconds, centiseconds). */
    private String formatTime(long millis) {
        long centiseconds = (millis / 10) % 100;
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (60 * 1000)) % 60;
        long hours = millis / (60 * 60 * 1000);
        return String.format(Locale.US, "%02d:%02d:%02d.%02d",
                hours, minutes, seconds, centiseconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop UI ticks only. Timing state (running / startBase / accumulated)
        // is untouched, so elapsed time keeps accruing on the monotonic clock
        // while the user navigates away.
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the stopwatch was still running when we left, re-post the ticker
        // and refresh the display. Elapsed time is exactly right because it is
        // computed from SystemClock.elapsedRealtime(), not from tick counts.
        if (running) {
            updateDisplay();
            handler.post(ticker);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_RUNNING, running);
        outState.putLong(KEY_START_BASE, startBase);
        outState.putLong(KEY_ACCUMULATED, accumulated);
        outState.putInt(KEY_LAP_COUNT, lapCount);
        outState.putStringArrayList(KEY_LAPS, laps);
    }
}

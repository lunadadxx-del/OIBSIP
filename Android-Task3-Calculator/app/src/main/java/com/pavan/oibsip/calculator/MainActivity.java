package com.pavan.oibsip.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.app.Activity;

/**
 * Oasis Infobyte SIP - Android App Development, Task 3: Calculator.
 *
 * Immediate-execution (left-to-right) calculator: pressing an operator while a
 * pending operator and typed operand exist first computes the pending
 * operation, then stores the new operator. E.g. 5 + 3 x 2 = -> 16.
 *
 * Division by zero shows "Error"; any subsequent input clears it.
 * All click handling is wired here via setOnClickListener (no android:onClick in XML).
 */
public class MainActivity extends Activity {

    private static final int MAX_INPUT_LENGTH = 12;

    private TextView tvExpression;
    private TextView tvDisplay;

    private final StringBuilder currentInput = new StringBuilder();
    private double storedValue = 0.0;
    private boolean hasStoredValue = false;
    private String pendingOperator = null; // one of "+", "-", "x", "/"
    private double lastResult = 0.0;
    private boolean hasLastResult = false;
    private boolean errorState = false;
    private boolean justEvaluated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvExpression = findViewById(R.id.tv_expression);
        tvDisplay = findViewById(R.id.tv_display);

        final int[] numberIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };
        for (int i = 0; i < numberIds.length; i++) {
            final String digit = String.valueOf(i);
            findViewById(numberIds[i]).setOnClickListener(v -> onDigitPressed(digit));
        }

        findViewById(R.id.btn_dot).setOnClickListener(v -> onDecimalPressed());

        findViewById(R.id.btn_add).setOnClickListener(v -> onOperatorPressed("+"));

        findViewById(R.id.btn_sub).setOnClickListener(v -> onOperatorPressed("-"));

        findViewById(R.id.btn_mul).setOnClickListener(v -> onOperatorPressed("x"));

        findViewById(R.id.btn_div).setOnClickListener(v -> onOperatorPressed("/"));

        findViewById(R.id.btn_equals).setOnClickListener(v -> onEqualsPressed());

        findViewById(R.id.btn_clear).setOnClickListener(v -> onClearPressed());

        findViewById(R.id.btn_backspace).setOnClickListener(v -> onBackspacePressed());

        refreshDisplay();
    }

    private void onDigitPressed(String digit) {
        if (errorState) {
            resetAll();
        }
        if (justEvaluated) {
            currentInput.setLength(0);
            justEvaluated = false;
        }
        if (currentInput.length() >= MAX_INPUT_LENGTH) {
            return;
        }
        // Avoid ugly leading zeros like "007".
        if (currentInput.length() == 1 && currentInput.charAt(0) == '0'
                && currentInput.indexOf(".") == -1) {
            currentInput.setLength(0);
        }
        currentInput.append(digit);
        refreshDisplay();
    }

    private void onDecimalPressed() {
        if (errorState) {
            resetAll();
        }
        if (justEvaluated) {
            currentInput.setLength(0);
            justEvaluated = false;
        }
        if (currentInput.indexOf(".") != -1) {
            return; // only one decimal point per number
        }
        if (currentInput.length() == 0) {
            currentInput.append("0");
        }
        currentInput.append(".");
        refreshDisplay();
    }

    private void onOperatorPressed(String op) {
        if (errorState) {
            resetAll();
        }
        justEvaluated = false;

        if (currentInput.length() > 0) {
            double inputValue = Double.parseDouble(currentInput.toString());
            if (pendingOperator != null && hasStoredValue) {
                // Immediate execution: resolve the pending operation first,
                // then store the newly pressed operator (e.g. 5 + 3 x 2 -> 8 x).
                double result = compute(storedValue, inputValue, pendingOperator);
                if (Double.isNaN(result)) {
                    showError();
                    return;
                }
                storedValue = result;
            } else {
                storedValue = inputValue;
            }
            hasStoredValue = true;
            currentInput.setLength(0);
        } else if (pendingOperator != null) {
            // No new operand typed: user is just swapping the operator.
            pendingOperator = op;
            refreshDisplay();
            return;
        } else if (!hasStoredValue) {
            storedValue = hasLastResult ? lastResult : 0.0;
            hasStoredValue = true;
        }

        pendingOperator = op;
        refreshDisplay();
    }

    private void onEqualsPressed() {
        if (errorState) {
            return;
        }
        if (pendingOperator == null || !hasStoredValue || currentInput.length() == 0) {
            return; // nothing to evaluate - ignore the tap, no crash
        }
        double inputValue = Double.parseDouble(currentInput.toString());
        String expression = formatNumber(storedValue) + " " + pendingOperator
                + " " + formatNumber(inputValue) + " =";
        double result = compute(storedValue, inputValue, pendingOperator);
        if (Double.isNaN(result)) {
            showError();
            return;
        }

        lastResult = result;
        hasLastResult = true;
        storedValue = result;
        hasStoredValue = true;
        pendingOperator = null;
        currentInput.setLength(0);
        currentInput.append(formatNumber(result));
        justEvaluated = true;

        refreshDisplay();
        tvExpression.setText(expression);
    }

    private void onClearPressed() {
        resetAll();
    }

    private void onBackspacePressed() {
        if (errorState || justEvaluated) {
            resetAll();
            return;
        }
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            refreshDisplay();
        }
    }

    private void resetAll() {
        currentInput.setLength(0);
        storedValue = 0.0;
        hasStoredValue = false;
        pendingOperator = null;
        hasLastResult = false;
        errorState = false;
        justEvaluated = false;
        refreshDisplay();
    }

    private void showError() {
        errorState = true;
        refreshDisplay();
    }

    private double compute(double a, double b, String op) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "x":
                return a * b;
            case "/":
                if (b == 0.0) {
                    return Double.NaN; // division by zero
                }
                return a / b;
            default:
                return Double.NaN;
        }
    }

    /** Formats a double without ugly float artifacts (0.30000000000000004 -> "0.3"). */
    private String formatNumber(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return getString(R.string.error_text);
        }
        double rounded = Math.round(value * 1e10) / 1e10;
        if (rounded == Math.rint(rounded) && Math.abs(rounded) < 1e15) {
            return String.valueOf((long) rounded);
        }
        return String.valueOf(rounded);
    }

    private void refreshDisplay() {
        if (errorState) {
            tvDisplay.setText(R.string.error_text);
            tvExpression.setText("");
            return;
        }

        if (currentInput.length() > 0) {
            tvDisplay.setText(currentInput.toString());
        } else if (pendingOperator == null && hasLastResult) {
            tvDisplay.setText(formatNumber(lastResult));
        } else if (hasStoredValue) {
            tvDisplay.setText(formatNumber(storedValue));
        } else {
            tvDisplay.setText("0");
        }

        if (pendingOperator != null && hasStoredValue) {
            tvExpression.setText(formatNumber(storedValue) + " " + pendingOperator);
        } else {
            tvExpression.setText("");
        }
    }
}

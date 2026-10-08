package com.pavan.oibsip.unitconverter;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;

import java.util.Locale;

public class MainActivity extends Activity
        implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerCategory;
    private Spinner spinnerFromUnit;
    private Spinner spinnerToUnit;
    private EditText editInput;
    private Button buttonConvert;
    private TextView textResult;

    private static final String[] CATEGORIES = {"Length", "Weight", "Temperature", "Volume"};

    // Length units, factors expressed in meters
    private static final String[] LENGTH_UNITS = {
            "Millimeter", "Centimeter", "Meter", "Kilometer", "Inch", "Foot", "Yard", "Mile"};
    private static final double[] LENGTH_FACTORS = {
            0.001, 0.01, 1.0, 1000.0, 0.0254, 0.3048, 0.9144, 1609.344};
    private static final String[] LENGTH_ABBR = {"mm", "cm", "m", "km", "in", "ft", "yd", "mi"};

    // Weight units, factors expressed in grams
    private static final String[] WEIGHT_UNITS = {"Milligram", "Gram", "Kilogram", "Ounce", "Pound"};
    private static final double[] WEIGHT_FACTORS = {0.001, 1.0, 1000.0, 28.349523125, 453.59237};
    private static final String[] WEIGHT_ABBR = {"mg", "g", "kg", "oz", "lb"};

    // Temperature units (handled via a Celsius pivot, not factors)
    private static final String[] TEMP_UNITS = {"Celsius", "Fahrenheit", "Kelvin"};
    private static final String[] TEMP_ABBR = {"\u00B0C", "\u00B0F", "K"};
    private static final double ABSOLUTE_ZERO_C = -273.15;

    // Volume units, factors expressed in liters
    private static final String[] VOLUME_UNITS = {
            "Milliliter", "Liter", "Fluid Ounce (US)", "Cup (US)", "Pint (US)", "Quart (US)", "Gallon (US)"};
    private static final double[] VOLUME_FACTORS = {
            0.001, 1.0, 0.0295735, 0.236588, 0.473176, 0.946353, 3.78541};
    private static final String[] VOLUME_ABBR = {"mL", "L", "fl oz", "cup", "pt", "qt", "gal"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerFromUnit = findViewById(R.id.spinnerFromUnit);
        spinnerToUnit = findViewById(R.id.spinnerToUnit);
        editInput = findViewById(R.id.editInput);
        buttonConvert = findViewById(R.id.buttonConvert);
        textResult = findViewById(R.id.textResult);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerCategory.setOnItemSelectedListener(this);

        buttonConvert.setOnClickListener(v -> convert());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        populateUnitSpinners(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Keep the current unit spinners unchanged
    }

    /** Repopulates the from/to unit spinners for the chosen category and resets selections. */
    private void populateUnitSpinners(int categoryIndex) {
        String[] units;
        switch (categoryIndex) {
            case 1:
                units = WEIGHT_UNITS;
                break;
            case 2:
                units = TEMP_UNITS;
                break;
            case 3:
                units = VOLUME_UNITS;
                break;
            default:
                units = LENGTH_UNITS;
                break;
        }
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromUnit.setAdapter(unitAdapter);
        spinnerToUnit.setAdapter(unitAdapter);
        spinnerFromUnit.setSelection(0);
        spinnerToUnit.setSelection(0);
    }

    /** Validates input, performs the conversion, and displays the result. */
    private void convert() {
        String raw = editInput.getText().toString().trim();
        if (raw.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        double value;
        try {
            value = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.toast_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryIndex = spinnerCategory.getSelectedItemPosition();
        int fromPos = spinnerFromUnit.getSelectedItemPosition();
        int toPos = spinnerToUnit.getSelectedItemPosition();

        double result;
        String fromAbbr;
        String toAbbr;

        if (categoryIndex == 2) {
            // Temperature: convert via Celsius pivot
            double celsius = toCelsius(value, fromPos);
            if (celsius < ABSOLUTE_ZERO_C) {
                textResult.setTextColor(getColor(R.color.error));
                textResult.setText(getString(R.string.error_absolute_zero));
                return;
            }
            result = fromCelsius(celsius, toPos);
            fromAbbr = TEMP_ABBR[fromPos];
            toAbbr = TEMP_ABBR[toPos];
        } else {
            double[] factors;
            String[] abbrs;
            switch (categoryIndex) {
                case 1:
                    factors = WEIGHT_FACTORS;
                    abbrs = WEIGHT_ABBR;
                    break;
                case 3:
                    factors = VOLUME_FACTORS;
                    abbrs = VOLUME_ABBR;
                    break;
                default:
                    factors = LENGTH_FACTORS;
                    abbrs = LENGTH_ABBR;
                    break;
            }
            result = value * factors[fromPos] / factors[toPos];
            fromAbbr = abbrs[fromPos];
            toAbbr = abbrs[toPos];
        }

        textResult.setTextColor(getColor(R.color.text_primary));
        textResult.setText(format(value) + " " + fromAbbr + " = " + format(result) + " " + toAbbr);
    }

    /** Converts a temperature value to Celsius. fromPos: 0=Celsius, 1=Fahrenheit, 2=Kelvin. */
    private double toCelsius(double value, int fromPos) {
        if (fromPos == 1) {
            return (value - 32.0) * 5.0 / 9.0;
        }
        if (fromPos == 2) {
            return value - 273.15;
        }
        return value;
    }

    /** Converts a Celsius temperature to the target unit. toPos: 0=Celsius, 1=Fahrenheit, 2=Kelvin. */
    private double fromCelsius(double celsius, int toPos) {
        if (toPos == 1) {
            return celsius * 9.0 / 5.0 + 32.0;
        }
        if (toPos == 2) {
            return celsius + 273.15;
        }
        return celsius;
    }

    /** Formats a number with up to 4 decimal places, stripping trailing zeros. */
    private String format(double value) {
        if (value == 0) {
            value = 0; // normalize -0.0 to 0
        }
        String formatted = String.format(Locale.US, "%.4f", value);
        if (formatted.contains(".")) {
            formatted = formatted.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return formatted;
    }
}

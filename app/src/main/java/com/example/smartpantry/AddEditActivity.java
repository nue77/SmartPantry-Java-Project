package com.example.smartpantry;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.app.DatePickerDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartpantry.data.PantryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Add/Edit Ingredient screen.
 * Add mode: opened with no extras.
 * Edit mode: opened with the selected item's data passed through the Intent.
 */
public class AddEditActivity extends AppCompatActivity {

    // Keys used to pass data between screens through the Intent.
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_QUANTITY = "extra_quantity";
    public static final String EXTRA_UNIT = "extra_unit";
    public static final String EXTRA_EXPIRY = "extra_expiry";

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp", "cup"};

    private PantryViewModel viewModel;
    private TextInputLayout layoutName, layoutQuantity;
    private TextInputEditText etName, etQuantity;
    private Spinner spinnerUnit;
    private TextView tvExpiry;

    private long itemId = -1;       // -1 means "add mode"
    private Long expiryDate = null; // null = no expiry date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        tvExpiry = findViewById(R.id.tvExpiry);
        Button btnPickDate = findViewById(R.id.btnPickDate);
        Button btnClearDate = findViewById(R.id.btnClearDate);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        spinnerUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS));

        // If the Intent carries an item id, we are editing an existing item.
        if (getIntent().hasExtra(EXTRA_ID)) {
            itemId = getIntent().getLongExtra(EXTRA_ID, -1);
            setTitle("Edit Ingredient");

            etName.setText(getIntent().getStringExtra(EXTRA_NAME));
            etQuantity.setText(formatQuantity(getIntent().getDoubleExtra(EXTRA_QUANTITY, 0)));

            int unitIndex = Arrays.asList(UNITS).indexOf(getIntent().getStringExtra(EXTRA_UNIT));
            if (unitIndex >= 0) spinnerUnit.setSelection(unitIndex);

            if (getIntent().hasExtra(EXTRA_EXPIRY)) {
                long exp = getIntent().getLongExtra(EXTRA_EXPIRY, -1);
                if (exp != -1) {
                    expiryDate = exp;
                }
            }
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            setTitle("Add Ingredient");
            btnDelete.setVisibility(View.GONE);
        }
        updateExpiryText();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> {
            expiryDate = null;
            updateExpiryText();
        });
        btnSave.setOnClickListener(v -> saveItem());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    /** Validates the form, then inserts (add mode) or updates (edit mode). */
    private void saveItem() {
        layoutName.setError(null);
        layoutQuantity.setError(null);

        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String quantityText = etQuantity.getText() == null ? "" : etQuantity.getText().toString().trim();

        boolean valid = true;

        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name");
            valid = false;
        } else if (name.length() > 50) {
            layoutName.setError("Name must be 50 characters or fewer");
            valid = false;
        }

        double quantity = 0;
        if (quantityText.isEmpty()) {
            layoutQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    layoutQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                layoutQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) return;  // stop here and show the errors

        String unit = (String) spinnerUnit.getSelectedItem();
        PantryItem item = new PantryItem(name, quantity, unit, expiryDate);

        if (itemId == -1) {
            viewModel.insert(item);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            item.id = itemId;  // same id tells Room which row to update
            viewModel.update(item);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();  // go back to the list
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove this ingredient from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    PantryItem item = new PantryItem();
                    item.id = itemId;  // Room deletes by primary key
                    viewModel.delete(item);
                    Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (expiryDate != null) cal.setTimeInMillis(expiryDate);

        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day, 0, 0, 0);
            picked.set(Calendar.MILLISECOND, 0);
            expiryDate = picked.getTimeInMillis();
            updateExpiryText();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateExpiryText() {
        if (expiryDate == null) {
            tvExpiry.setText("Expiry date: none");
        } else {
            String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(new Date(expiryDate));
            tvExpiry.setText("Expiry date: " + date);
        }
    }

    // Show 3 instead of 3.0 for whole numbers.
    private String formatQuantity(double q) {
        return (q == (long) q) ? String.valueOf((long) q) : String.valueOf(q);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();  // toolbar back arrow
        return true;
    }
}
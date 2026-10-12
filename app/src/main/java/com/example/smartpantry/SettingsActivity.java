package com.example.smartpantry;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Settings screen: one toggle that controls expiring-soon highlighting on the pantry list. */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "smart_pantry_prefs";
    public static final String KEY_EXPIRING_ALERTS = "expiring_alerts";

    /** Read by the pantry list screen. Defaults to ON. */
    public static boolean isExpiringAlertsEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_EXPIRING_ALERTS, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle("Settings");

        SwitchCompat switchExpiring = findViewById(R.id.switchExpiring);
        switchExpiring.setChecked(isExpiringAlertsEnabled(this));

        // Save the choice immediately; SharedPreferences keeps it after the app closes.
        switchExpiring.setOnCheckedChangeListener((button, isChecked) ->
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putBoolean(KEY_EXPIRING_ALERTS, isChecked)
                        .apply());

        BottomNavHelper.setup(this, R.id.nav_settings);
    }
}

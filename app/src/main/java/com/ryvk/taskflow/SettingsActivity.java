package com.ryvk.taskflow;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private Switch darkModeSwitch;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "com.ryvk.taskflow";
    private static final String DARK_MODE_KEY = "DarkModeEnabled";

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
        // Initialize views
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set initial state
        boolean isDarkMode = Utils.isDarkModeEnabled(SettingsActivity.this);
        darkModeSwitch.setChecked(isDarkMode);

        // Setup switch listener
        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utils.setDarkMode(isChecked);
                saveDarkModePreference(isChecked);
                recreate();
            }
        });

        Button logoutButton = findViewById(R.id.button5);
        logoutButton.setOnClickListener(v -> {
            AlertUtils.showConfirmDialog(SettingsActivity.this, "Logout", "Are you sure you want to logout?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    User user = User.getSPUser(SettingsActivity.this);
                    user.removeSPUser(SettingsActivity.this);
                    finish();
                }
            });
        });
    }

    private void saveDarkModePreference(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DARK_MODE_KEY, enabled);
        editor.apply();
    }
}
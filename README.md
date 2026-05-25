# SecureAuth 2FA Spyware

This project is a Proof of Concept that demonstrates how an Android application with overly permissive accessibility privileges can passively monitor, capture, and exfiltrate time-sensitive data from a dynamic user interface without altering the target application's user experience.

---

## Project Structure

The project repository contains two separate Android Studio application packages running on the same device architecture:

1. **`VictimVault` (SecureAuth)**: A mock 2FA client app that automatically generates and rotates dynamic Time-based One-Time Passwords inside a `RecyclerView` container every 30 seconds.
2. **`BatteryOptimizer` (Phantom Finger)**: A malicious background service masquerading as a utility app. It hooks into the Android Accessibility framework to silently parse the active window view hierarchy and exfiltrate visible text elements.

---

## Threat Model and Execution Flow

Unlike traditional intrusive overlay attacks that disrupt the screen or rely on fragile user input manipulation (`ACTION_CLICK`), this optimized variant implements **passive background spyware execution**:

1. The background service uses `TYPE_WINDOW_STATE_CHANGED` events to identify when the target application (`com.smd.victimvault`) comes into active foreground view.
2. The service switches to an event-driven listener hook (`TYPE_WINDOW_CONTENT_CHANGED`). The moment the 2FA app's internal timer forces a dataset redraw, the service intercepts the structural layout tree.
3. The system programmatically scans the container mapping for targeted resource IDs (`tv_service` and `tv_code`) to extract plain-text pairs.
4. Scraped data changes are isolated (preventing redundant duplicates), converted to a JSON payload alongside device identification metadata, and uploaded over an asynchronous network thread to a remote Command and Control endpoint.

---

## Setting Up the Environment

### Prerequisites
* **Android Studio**
* **Android Emulator** running target API level 34 (Android 14)
* **Local Backend**: A network endpoint (e.g., a lightweight Flask or Node listener) listening on `http://localhost:5000/exfil` to process incoming JSON payloads.

### Step 1: Deploying the Target App
1. Import `VictimVault` into Android Studio.
2. Compile and install the APK on the test device.

### Step 2: Deploying the Exploitation Service
1. Import `BatteryOptimizer` into Android Studio.
2. Compile and install the APK on the same test device.
3. Open `Battery Optimizer` on the device interface and follow the checklist:
   * **Overlay Permission**: (Optional framework configuration requirement).
   * **Notification Permission**: Grant standard permissions (Required on Android 13+ to verify notification behaviors).
   * **Accessibility Service Authorization**: Click the toggle shortcut to enter the system settings. Locate `Battery Optimizer` within the *Downloaded Apps* or *Installed Services* list and slide the toggle to **ON**.
   * *Note for Android 13/14*: If the system blocks activation due to "Restricted Settings", navigate to the device's *Settings -> Apps -> Battery Optimizer*, tap the three dots in the top-right corner, select **Allow restricted settings**, then return to the accessibility menu to activate.

---

## Live Demonstration Walkthrough

1. Start your local C2 server engine on your machine.
2. On the emulator, tap open **SecureAuth** (`VictimVault`). 
3. **Observe the Integrity Defect**:
   * The device user interface appears completely normal with zero performance friction, overlay pops, or visual indicators of compromise.
   * Check your terminal window: The moment focus lands on the application view boundaries, the C2 server receives the initial set of active 2FA codes.
   * Wait for the 30-second token rotation countdown to hit zero: As the authenticator app updates the numbers on screen, the background service immediately forwards the brand-new, valid authentication codes seamlessly to the C2 server logs.

---

## Recommended Mitigations
To neutralize accessibility scraping and background data monitoring inside sensitive applications:
* Implement `setFilterTouchesWhenObscured(true)` on critical interaction boundaries to reject input traffic whenever custom layouts are detected.
* Explicitly label views housing highly cryptographic or secure transient secrets with `android:importantForAccessibility="no"` to purge structural strings from the global node tree.

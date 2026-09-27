# Implementation Plan: Monetization & Distribution (AdMob + Pro Upgrade)

This document outlines the strategy for refactoring AutoCare to support a "Freemium" model using Google AdMob for revenue and Google Play Billing for a "Pro" upgrade.

## 1. Overview
AutoCare will transition to a tiered model:
*   **Free Version:** Core maintenance tracking, fuel logs, and basic statistics. Supported by AdMob banner ads.
*   **Pro Version (IAP):** Ad-free experience, professional PDF/CSV exports, and unlimited vehicle management.

## 2. Technology Stack
*   **Google AdMob SDK:** For delivering banner and interstitial ads.
*   **Google Play Billing Library:** For handling one-time In-App Purchases (IAP).
*   **DataStore / SharedPreferences:** To cache the `isProUser` status locally for immediate UI responsiveness.

## 3. UI/UX Strategy: Ad Placement & Stability
To comply with Google AdMob policies and prevent poor UX (accidental clicks due to layout shifts), we will implement **Reserved Ad Spaces**.

### Strategy: Pre-allocated UI Containers
*   **Banner Slots:** Instead of injecting ads dynamically, Compose layouts will reserve a fixed height (e.g., 50dp or 90dp for Adaptive Banners) at the bottom of the screen.
*   **Components:** Create a `MonetizedScaffold` or `AdContainer` wrapper that manages the padding based on the `isProUser` state.
*   **Prevention of Layout Jumps:** If `isProUser` is false, the space is rendered with a placeholder or empty box *before* the ad loads. If true, the space is removed entirely.

### Target Locations:
1.  **Dashboard Screen:** Bottom banner above the Navigation Bar.
2.  **History/Logs Screen:** Bottom banner to maximize visibility during long scrolling sessions.
3.  **Refueling Form:** Top or bottom banner (carefully placed to avoid overlapping the "Save" button).

## 4. Monetization Logic & State Management

### State Flow:
1.  **App Launch:** Check local `UserPreferences` for `isProUser` flag.
2.  **Billing Sync:** Periodically (or on the Settings screen) query Google Play Billing to verify purchase tokens and update the local flag.
3.  **UI Injection:**
    ```kotlin
    val isProUser by userPreferences.isProUser.collectAsState()
    
    Scaffold(
        bottomBar = {
            Column {
                if (!isProUser) {
                    AdBannerPlaceholder(modifier = Modifier.height(50.dp))
                }
                BottomNavigationBar(...)
            }
        }
    )
    ```

## 5. "Pro" Feature Gatekeeping
The following features will be locked behind a `Pro` check:
*   **PDF Report Generation:** The "Export PDF" icon will trigger a "Go Pro" upsell dialog if the user is on the free tier.
*   **Multi-Vehicle Support:** Limit the free tier to 1 vehicle.
*   **Advanced Analytics:** Cost breakdown over 12+ months.

## 6. Implementation Roadmap

### Phase 1: Integration & Setup
*   Add dependencies: `com.google.android.gms:play-services-ads` and `com.android.billingclient:billing-ktx`.
*   Configure AdMob App ID in `AndroidManifest.xml`.
*   Setup `BillingManager` to handle connections to Google Play.

### Phase 2: UI Refactoring
*   Implement the `AdBanner` Composable using `AndroidView` to wrap the AdMob `AdView`.
*   Update `DashboardScreen` and `HistoryScreen` to include reserved ad slots.
*   Add "Remove Ads" button in `SettingsScreen`.

### Phase 3: IAP Workflow
*   Implement "Purchase Success" flow: verify purchase -> update local flag -> hide all Ad containers instantly.
*   Create a "Upgrade to Pro" landing page/dialog highlighting PDF exports and ad-free benefits.

### Phase 4: Compliance & Testing
*   Verify ad placement doesn't overlap interactive elements.
*   Test "Pro" status persistence across app restarts and data clears.
*   Submit to Google Play Console for "Closed Testing".

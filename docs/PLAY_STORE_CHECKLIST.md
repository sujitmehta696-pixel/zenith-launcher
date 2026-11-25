# Google Play Store Publishing Checklist — ANDROID LAUNCHER

## 1. App Identity & Build Configuration
- **App Name**: `Android Launcher` (16 chars, <= 30 char limit, no emojis/buzzwords)
- **Application ID**: Configured in `app/build.gradle.kts` (`com.aistudio.androidlauncher.k9v4xp` — update to your verified Play Console package ID if publishing under an existing listing)
- **Version Code / Name**: `1` / `1.0.0`
- **Release Output**: Run `gradle :app:bundleRelease` (produces `app/build/outputs/bundle/release/app-release.aab`)
- **Signing**: Configure `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` environment variables for Play App Signing upload key.

## 2. Google Play Store Listing Copy
- **Short Description (<= 80 chars)**:
  `Futuristic spinner home launcher with dynamic themes and studio AMOLED wallpapers.`
- **Full Description**:
  Transform your Android device with Android Launcher — a minimal, luxury home screen experience featuring an interactive Spinner orbital dial, customizable app grids, a live multi-font theme engine, and studio-crafted AMOLED wallpapers.
  - Three Home Modes: Signature Spinner Dial, Customizable Grid, and Distraction-Free Minimal Mode
  - Real Home Replacement: Multi-page workspace, Dock, App Drawer with sorting, App Shortcuts, Hidden Apps, and System Widget support
  - 50 Free Wallpapers & 50 Free Themes included out of the box
  - Pro Studio Upgrade: Unlocks exclusive Luxury & Futuristic collections, Cyber Arc & Luxury Halo spinner dials, and 6-column layouts

## 3. Visual Assets Specifications
- **App Icon**: `512 x 512 px` 32-bit PNG (generated from `ic_launcher_art` adaptive artwork)
- **Feature Graphic**: `1024 x 500 px` PNG/JPEG showcasing Spinner Launcher Dial + Dark AMOLED theme
- **Phone Screenshots (min 2, recommended 6–8, 9:16 1080x1920+)**:
  1. Live Home Screen in Spinner Mode
  2. Home Dashboard & Active Setup
  3. Wallpapers Gallery & Full-Screen Preview
  4. Themes Store & Live Theme Detail
  5. App Drawer & Long-Press Actions
  6. Pro Studio Upgrade Screen

## 4. Permissions & Data Safety Declaration
- **Permissions Used**:
  - `SET_WALLPAPER` & `SET_WALLPAPER_HINTS` (normal permissions, no runtime prompt)
  - `INTERNET` & `ACCESS_NETWORK_STATE` (Google Play Billing & Wi-Fi download preference check)
  - `com.android.vending.BILLING` (Google Play Billing)
  - `<queries>` for `MAIN/LAUNCHER` (Play-compliant alternative to `QUERY_ALL_PACKAGES`)
  - **Zero broad storage permissions** (`MediaStore` used on Android 10+ for wallpaper exports)
- **Data Safety Form**:
  - Personalization data (themes, favorites, hidden apps, search history) stays locally on-device.
  - Purchase history / Entitlements processed via Google Play Billing.

## 5. Google Play Billing Console Setup
Create the following products in **Google Play Console -> Monetize -> Products**:
- Subscription: `android_launcher_pro_yearly` (Annual base plan)
- Subscription: `android_launcher_pro_monthly` (Monthly base plan)
- In-App Product (One-time): `android_launcher_pro_lifetime`
(Note: The Debug Build Entitlement toggle in `PremiumScreen` is strictly disabled in `release` builds via `BuildConfig.DEBUG`.)

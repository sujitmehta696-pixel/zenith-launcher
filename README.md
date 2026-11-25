# ANDROID LAUNCHER

An ultra-premium futuristic Android Home Screen Launcher, Dynamic Theme Engine, and Studio Wallpaper application built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, and Google Play Billing v7.

## Core Architecture

- **Real Android Home Launcher (`LauncherActivity`)**: Registered with `ACTION_MAIN` + `CATEGORY_HOME` + `CATEGORY_DEFAULT` and `Theme.AndroidLauncher.Wallpaper` (`windowShowWallpaper = true`). Supports Android's `RoleManager.ROLE_HOME` default launcher selection flow, `HorizontalPager` multi-page workspaces, customizable grids, dock, app drawer (A-Z / Recent / Frequent sorting), long-press app actions (Pin, Favorite, Hide, App Info, Uninstall), `LauncherApps` dynamic shortcuts, and `AppWidgetHost` support.
- **Signature Spinner Launcher Mode**: Interactive rotary `SpinnerLauncherWheel` with 4 dial styles (`Orbital Dial`, `Chronos Ring`, `Cyber Arc` Pro, `Luxury Halo` Pro), plus `Grid Mode` and `Minimal Mode`.
- **Dynamic Theme Engine**: 50 distinct bundled Free Themes + 60 curated starter Pro Themes (across AMOLED, Dark, Light, Minimal, Gradient, Material, Luxury, Futuristic, Nature, Exclusive). Applying any theme updates the color scheme, card surface style (`Glassmorphic`, `Solid Elevated`, `Outlined Minimal`, `Gradient Border`), icon shape (`Squircle`, `Circle`, `Rounded`, `Hexagon`), corner radius, and bundled `.ttf` typography (`Plus Jakarta Sans`, `Space Grotesk`, `Outfit`, `Playfair Display`, `JetBrains Mono`).
- **Scalable Wallpaper Engine**: 50 distinct bundled Free Wallpapers (4 high-resolution raster artworks + 46 unique procedural studio compositions across 9 categories) + 60 curated starter Pro Wallpapers, backed by indexed Room pagination (`LIMIT/OFFSET`), LRU memory/disk thumbnail caching, real PNG export via `MediaStore`, and Home/Lock screen application via `WallpaperManager`.
- **Content Manifest Transparency**: `app/src/main/assets/catalog_manifest.json` documents the 50 free wallpapers, 50 free themes, 60 bundled starter Pro wallpapers/themes, and the paginated manifest expansion capacity for 2,000 Pro wallpapers & 2,000 Pro themes without duplicating entries.
- **Google Play Billing v7 (`BillingManager`)**: Real `BillingClient` integration for `android_launcher_pro_yearly`, `android_launcher_pro_monthly`, and `android_launcher_pro_lifetime` with purchase acknowledgement and restore purchases. Includes a `BuildConfig.DEBUG`-guarded Developer Entitlement switch disabled automatically in release builds.

## Build Commands

```bash
# Run unit & Robolectric UI tests
gradle :app:testDebugUnitTest

# Build Debug APK
gradle :app:assembleDebug

# Build Release Android App Bundle (AAB)
# Set KEYSTORE_PATH, STORE_PASSWORD, KEY_PASSWORD env vars for release signing
gradle :app:bundleRelease
```

See `docs/PLAY_STORE_CHECKLIST.md` for full Google Play Console setup and release guidelines.

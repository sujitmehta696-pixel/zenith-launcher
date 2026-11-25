const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_user_123";
const BOB_UID = "bob_user_456";
const ADMIN_UID = "admin_sujit_789";
const ADMIN_EMAIL = "sujitmehta696@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, createdAt, updatedAt) {
  return {
    userId: uid,
    displayName: "Aarav Studio",
    email: "aarav@example.com",
    handle: "aarav_os",
    premiumActive: false,
    purchaseSource: "NONE",
    activeThemeId: "theme_free_obsidian_core",
    activeWallpaperId: "wp_free_amoled_eclipse",
    launcherMode: "SPINNER",
    spinnerStyle: "PRO_3D_SPHERE",
    swipeDownAction: "OPEN_NOTIFICATIONS",
    swipeUpAction: "OPEN_APP_DRAWER",
    doubleTapAction: "LOCK_SCREEN",
    longPressAction: "OPEN_STUDIO",
    pinchAction: "OPEN_CONTROL_CENTER",
    createdAt,
    updatedAt,
  };
}

function validCategoryPayload(id, createdAt, updatedAt, active = true) {
  return {
    categoryId: id,
    name: "AMOLED",
    description: "Deep true-black studio wallpapers",
    sortOrder: 1,
    isActive: active,
    createdAt,
    updatedAt,
  };
}

function validWallpaperPayload(id, adminUid, createdAt, updatedAt, published = true) {
  return {
    wallpaperId: id,
    title: "Neon Vortex 4K",
    description: "Animated 3D nebula loop",
    categoryId: "cat_amoled",
    categoryName: "AMOLED",
    mediaType: "VIDEO",
    mediaUrl: "https://firebasestorage.googleapis.com/v0/b/demo/o/wallpapers%2Fvideo.mp4?alt=media",
    thumbnailUrl: "https://firebasestorage.googleapis.com/v0/b/demo/o/wallpapers%2Fthumb.jpg?alt=media",
    storagePath: "wallpapers/" + id + "/video.mp4",
    thumbnailStoragePath: "wallpapers/" + id + "/thumb.jpg",
    resolutionLabel: "1080p MP4",
    fileSizeBytes: 4200000,
    isPremium: true,
    isPublished: published,
    downloadCount: 0,
    uploadedByUid: adminUid,
    createdAt,
    updatedAt,
  };
}

function validConfigPayload(adminUid, updatedAt) {
  return {
    configId: "global",
    maintenanceMode: false,
    maintenanceMessage: "Scheduled update in progress.",
    announcementBanner: "New 4K Live Wallpapers added!",
    aiSearchEnabled: true,
    liveWallpapersEnabled: true,
    edgePanelEnabled: true,
    controlCenterEnabled: true,
    notificationBadgesEnabled: true,
    updatedByUid: adminUid,
    updatedAt,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 2000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now, now))
  );
  await assertFails(unauthDb.collection("remote_wallpapers").where("isPublished", "==", true).get());
  await assertFails(unauthDb.collection("wallpaper_categories").where("isActive", "==", true).get());
  await assertFails(unauthDb.collection("app_config").doc("global").get());
});

test("Owner can create profile, sync Pro 3D Sphere & pinch gesture, and record immutable purchase", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created, created))
  );

  // Regular user CANNOT self-grant premiumActive during create
  await assertFails(
    aliceDb
      .collection("users")
      .doc("bad_create")
      .set({
        ...validProfilePayload("bad_create", created, created),
        premiumActive: true,
      })
  );

  // Sync launcher theme, PRO_3D_SPHERE & gesture settings
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      activeThemeId: "theme_pro_aurelius_gold",
      spinnerStyle: "PRO_3D_SPHERE",
      swipeDownAction: "OPEN_CONTROL_CENTER",
      pinchAction: "OPEN_EDGE_PANEL",
      isActive: true,
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Regular user CANNOT self-block or list all users
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isBlocked: true,
      updatedAt: new Date(Date.now() - 1500),
    })
  );
  await assertFails(aliceDb.collection("users").get());

  // Sync verified Play Billing Pro entitlement
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      premiumActive: true,
      activeProductId: "android_launcher_pro_yearly",
      purchaseSource: "GOOGLE_PLAY",
      purchaseTokenHash: "sha256_abc123",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  const purchaseRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("purchases")
    .doc("order_001");
  await assertSucceeds(
    purchaseRef.set({
      userId: ALICE_UID,
      productId: "android_launcher_pro_yearly",
      purchaseSource: "GOOGLE_PLAY",
      purchaseTokenHash: "sha256_abc123",
      createdAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(purchaseRef.update({ productId: "tampered" }));
  await assertFails(purchaseRef.delete());
});

test("Email spoofing (unverified admin email) and regular user privilege escalation are rejected", async () => {
  const spoofDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: ADMIN_EMAIL,
      email_verified: false,
    })
    .firestore();
  const now = new Date(Date.now() - 3000);

  await assertFails(
    spoofDb
      .collection("wallpaper_categories")
      .doc("cat_amoled")
      .set(validCategoryPayload("cat_amoled", now, now))
  );
  await assertFails(
    spoofDb
      .collection("remote_wallpapers")
      .doc("wp_01")
      .set(validWallpaperPayload("wp_01", "spoof_uid", now, now))
  );
  await assertFails(
    spoofDb.collection("app_config").doc("global").set(validConfigPayload("spoof_uid", now))
  );
});

test("Verified Admin manages categories, wallpapers, live videos, users, and global config; User App sees only published/active content", async () => {
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: ADMIN_EMAIL,
      email_verified: true,
    })
    .firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 10000);

  // Admin creates category, published wallpaper, unpublished draft, and global config
  await assertSucceeds(
    adminDb
      .collection("wallpaper_categories")
      .doc("cat_amoled")
      .set(validCategoryPayload("cat_amoled", now, now, true))
  );
  await assertSucceeds(
    adminDb
      .collection("remote_wallpapers")
      .doc("wp_pub")
      .set(validWallpaperPayload("wp_pub", ADMIN_UID, now, now, true))
  );
  await assertSucceeds(
    adminDb
      .collection("remote_wallpapers")
      .doc("wp_draft")
      .set(validWallpaperPayload("wp_draft", ADMIN_UID, now, now, false))
  );
  await assertSucceeds(
    adminDb.collection("app_config").doc("global").set(validConfigPayload(ADMIN_UID, now))
  );

  // User App queries active categories and published wallpapers cleanly
  await assertSucceeds(
    aliceDb.collection("wallpaper_categories").where("isActive", "==", true).get()
  );
  await assertSucceeds(
    aliceDb.collection("remote_wallpapers").where("isPublished", "==", true).get()
  );
  await assertSucceeds(aliceDb.collection("app_config").doc("global").get());

  // User CANNOT read unpublished draft or list unfiltered wallpapers
  await assertFails(aliceDb.collection("remote_wallpapers").doc("wp_draft").get());
  await assertFails(aliceDb.collection("remote_wallpapers").get());

  // User can atomically increment downloadCount (+1) on published wallpaper, but cannot tamper title/isPremium
  await assertSucceeds(
    aliceDb.collection("remote_wallpapers").doc("wp_pub").update({
      downloadCount: 1,
      updatedAt: new Date(Date.now() - 2000),
    })
  );
  await assertFails(
    aliceDb.collection("remote_wallpapers").doc("wp_pub").update({
      downloadCount: 2,
      isPremium: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // User creates profile, Admin lists users & grants Pro / maintenance mode
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now, now))
  );
  await assertSucceeds(adminDb.collection("users").get());
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).update({
      premiumActive: true,
      purchaseSource: "ADMIN_GRANT",
      isBlocked: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Admin manages Payment Gateway config & admin-only secrets; User cannot access admin_secrets or use DEBUG_DEV", async () => {
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: ADMIN_EMAIL,
      email_verified: true,
    })
    .firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 10000);

  // Admin writes client-safe checkout endpoints to /app_config/global
  await assertSucceeds(
    adminDb.collection("app_config").doc("global").set({
      ...validConfigPayload(ADMIN_UID, now),
      paymentGatewayEnabled: true,
      paymentProviderName: "Razorpay",
      paymentBackendUrl: "https://api.example.com/payments/create-checkout",
      paymentVerifyUrl: "https://api.example.com/payments/verify",
      paymentPublicKey: "rzp_live_public_123",
      paymentCurrency: "INR",
      monthlyPriceLabel: "₹199 / mo",
      yearlyPriceLabel: "₹1,499 / yr",
      lifetimePriceLabel: "₹3,999 once",
      monthlyAmount: "199",
      yearlyAmount: "1499",
      lifetimeAmount: "3999",
    })
  );

  // Admin stores secret API key & webhook secret in /admin_secrets/payment_gateway
  await assertSucceeds(
    adminDb.collection("admin_secrets").doc("payment_gateway").set({
      secretId: "payment_gateway",
      providerName: "Razorpay",
      secretApiKey: "rzp_secret_very_private_key",
      webhookSecret: "whsec_private_hmac_secret",
      updatedByUid: ADMIN_UID,
      updatedAt: now,
    })
  );

  // Regular user can read global payment URL config, but is STRICTLY DENIED reading or writing admin_secrets
  await assertSucceeds(aliceDb.collection("app_config").doc("global").get());
  await assertFails(aliceDb.collection("admin_secrets").doc("payment_gateway").get());
  await assertFails(aliceDb.collection("admin_secrets").get());
  await assertFails(
    aliceDb.collection("admin_secrets").doc("payment_gateway").set({
      secretId: "payment_gateway",
      providerName: "Hack",
      secretApiKey: "stolen",
      webhookSecret: "stolen",
      updatedByUid: ALICE_UID,
      updatedAt: now,
    })
  );

  // Regular user cannot use legacy DEBUG_DEV bypass
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now, now))
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      premiumActive: true,
      activeProductId: "debug_dev_entitlement",
      purchaseSource: "DEBUG_DEV",
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Verified PAYMENT_GATEWAY entitlement & purchase receipt succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      premiumActive: true,
      activeProductId: "android_launcher_pro_yearly",
      purchaseSource: "PAYMENT_GATEWAY",
      purchaseTokenHash: "sha256_gateway_tx_991",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("purchases")
      .doc("gw_rcpt_991")
      .set({
        userId: ALICE_UID,
        productId: "android_launcher_pro_yearly",
        purchaseSource: "PAYMENT_GATEWAY",
        purchaseTokenHash: "sha256_gateway_tx_991",
        createdAt: new Date(Date.now() - 1000),
      })
  );
});

test("Admin creates & updates INR subscription_plans; User reads active plans and receives subscription expiry fields", async () => {
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: ADMIN_EMAIL,
      email_verified: true,
    })
    .firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    adminDb.collection("subscription_plans").doc("plan_pro_30").set({
      planId: "plan_pro_30",
      planName: "Pro 30 Days",
      subtitle: "Auto-UPI unlock for 30 days",
      priceInr: 149,
      validityDays: 30,
      badge: "Popular",
      sortOrder: 1,
      isActive: true,
      updatedByUid: ADMIN_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("subscription_plans").where("isActive", "==", true).get()
  );

  // Regular user cannot create or modify subscription plans or payment orders
  await assertFails(
    aliceDb.collection("subscription_plans").doc("plan_free_hack").set({
      planId: "plan_free_hack",
      planName: "Hack",
      subtitle: "Hack",
      priceInr: 1,
      validityDays: 3650,
      badge: "",
      sortOrder: 9,
      isActive: true,
      updatedByUid: ALICE_UID,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("payment_orders").doc("ord_spoof").set({
      orderId: "ord_spoof",
      userId: ALICE_UID,
      status: "PAID",
    })
  );

  // Admin can activate subscription fields on user profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now, now))
  );
  const startMs = Date.now() - 1000;
  const expiryMs = startMs + 30 * 86400000;
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).update({
      premiumActive: true,
      subscriptionActive: true,
      planId: "plan_pro_30",
      planName: "Pro 30 Days",
      planStartedAtMillis: startMs,
      planExpiresAtMillis: expiryMs,
      activeProductId: "plan_pro_30",
      purchaseSource: "PAYMENT_GATEWAY",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});



"use strict";

const { test } = require("node:test");
const assert = require("node:assert/strict");
const crypto = require("node:crypto");
const {
  computeExpiryMillis,
  verifyWebhookSignature,
  isGatewayPaidPayload,
  unwrapGatewayNode,
  hasAdminCredentials,
  loadServiceAccountCredentials,
} = require("../server");

test("computeExpiryMillis calculates exact validity days expiry", () => {
  const start = 1_700_000_000_000;
  assert.equal(computeExpiryMillis(start, 30), start + 30 * 86_400_000);
  assert.equal(computeExpiryMillis(start, 365), start + 365 * 86_400_000);
  assert.equal(computeExpiryMillis(start, 0), start + 30 * 86_400_000);
});

test("verifyWebhookSignature validates timing-safe HMAC-SHA256", () => {
  const payload = Buffer.from(JSON.stringify({ order_id: "fg_123", status: "success" }), "utf8");
  const secret = "whsec_test_secret_123";
  const sig = crypto.createHmac("sha256", secret).update(payload).digest("hex");

  assert.equal(verifyWebhookSignature(payload, sig, secret), true);
  assert.equal(verifyWebhookSignature(payload, `sha256=${sig}`, secret), true);
  assert.equal(verifyWebhookSignature(payload, "0000", secret), false);
  assert.equal(verifyWebhookSignature(payload, sig, ""), false);
});

test("isGatewayPaidPayload verifies status and minimum INR amount including nested result", () => {
  assert.equal(isGatewayPaidPayload({ status: "success", amount: 199 }, 199), true);
  assert.equal(isGatewayPaidPayload({ status: "paid", amount: 1499 }, 1499), true);
  assert.equal(isGatewayPaidPayload({ status: true, result: { txnStatus: "COMPLETED", amount: 499 } }, 499), true);
  assert.equal(isGatewayPaidPayload({ status: "pending", amount: 199 }, 199), false);
  assert.equal(isGatewayPaidPayload({ status: "success", amount: 1 }, 199), false);
  assert.equal(unwrapGatewayNode({ result: { payment_url: "https://famgateway.in/pay/1" } }).payment_url, "https://famgateway.in/pay/1");
  assert.equal(typeof hasAdminCredentials(), "boolean");
});

test("loadServiceAccountCredentials returns explicit error instead of applicationDefault fallback", () => {
  const prevJson = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
  const prevPath = process.env.GOOGLE_APPLICATION_CREDENTIALS;
  delete process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
  delete process.env.GOOGLE_APPLICATION_CREDENTIALS;
  try {
    const res = loadServiceAccountCredentials();
    assert.equal(res.serviceAccount, null);
    assert.match(res.error, /FIREBASE_SERVICE_ACCOUNT_JSON/);
    assert.match(res.error, /applicationDefault\(\) fallback is disabled/);
  } finally {
    if (prevJson !== undefined) process.env.FIREBASE_SERVICE_ACCOUNT_JSON = prevJson;
    if (prevPath !== undefined) process.env.GOOGLE_APPLICATION_CREDENTIALS = prevPath;
  }
});

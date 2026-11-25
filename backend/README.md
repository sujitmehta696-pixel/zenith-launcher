# FamGateway v2.0 Auto-UPI Subscription Backend

Node.js Express backend handling **FamGateway v2.0 (`https://famgateway.in`)** Auto-UPI orders, `/famgateway-webhook` callbacks, `/api/verify-order.php` status polling, and automatic Cloud Firestore subscription activation.

## Setup

1. Install dependencies:
   ```bash
   cd backend
   npm install
   ```
2. Copy `.env.example` to `.env` and fill in:
   - `PUBLIC_BACKEND_BASE_URL` (your deployed HTTPS URL, e.g. `https://launcher-pay.onrender.com`)
   - `GOOGLE_APPLICATION_CREDENTIALS` (Firebase Service Account JSON path on your host, or Application Default Credentials on Cloud Run / GCP)
   - `FAMGATEWAY_API_KEY` & `FAMGATEWAY_WEBHOOK_SECRET` (optional if you manage them directly from your **Launcher OS Admin** APK inside `/admin_secrets/payment_gateway`)
3. Start the server:
   ```bash
   npm start
   ```
4. In your **Launcher OS Admin** APK -> **Payment Gateway** tab:
   - Select **FamGateway v2.0**
   - Set **Backend URL** to `https://your-backend-host.com`
   - Enter your **FamGateway Secret API Key** & **Webhook Signing Secret**
   - Edit your INR subscription plans (`Plan Name`, `Price ₹`, `Validity Days`) and tap **Save**.
5. In your FamGateway dashboard, set your Webhook URL to:
   `https://your-backend-host.com/famgateway-webhook`

# 🍎 Apple Sign In Setup Guide

Complete guide to configure Apple OAuth for your iOS app.

**Last Updated:** 2025-11-04  
**Cost:** $99/year (Apple Developer Program membership required)

---

## ⚠️ Prerequisites

### 1. Apple Developer Account
- **Cost:** $99/year
- **Sign up:** https://developer.apple.com/programs/enroll/
- **Processing time:** 1-2 business days for approval

### 2. Access to Apple Developer Console
- **URL:** https://developer.apple.com/account
- **Requires:** Paid developer membership

---

## 📋 Step-by-Step Setup

### Step 1: Create an App ID (5 minutes)

**Purpose:** Identifies your iOS app

1. **Go to:** https://developer.apple.com/account/resources/identifiers/list
2. **Click:** "+" button (top left) to add new identifier
3. **Select:** "App IDs"
4. **Click:** "Continue"
5. **Select type:** "App"
6. **Configure:**
   - **Description:** `Oddiya Travel App`
   - **Bundle ID:** `com.oddiya.app` (must match your iOS app)
   - **Platform:** iOS
7. **Capabilities:**
   - ✅ Check "Sign In with Apple"
8. **Click:** "Continue" → "Register"

**Save this:**
- Bundle ID: `com.oddiya.app`

---

### Step 2: Create a Services ID (5 minutes)

**Purpose:** Identifies your web service for Apple Sign In

1. **Go to:** https://developer.apple.com/account/resources/identifiers/list/serviceId
2. **Click:** "+" button (top left)
3. **Select:** "Services IDs"
4. **Click:** "Continue"
5. **Configure:**
   - **Description:** `Oddiya Web Service`
   - **Identifier:** `com.oddiya.service` (your Service ID)
   - ✅ Check "Sign In with Apple"
6. **Click:** "Continue" → "Register"
7. **Configure Sign In with Apple:**
   - Click on the Services ID you just created
   - Click "Configure" next to "Sign In with Apple"
   - **Primary App ID:** Select `com.oddiya.app` (from Step 1)
   - **Domains and Subdomains:**
     - Add: `oddiya.click`
     - Add: `localhost` (for development)
   - **Return URLs:**
     - Add: `https://oddiya.click/api/v1/auth/oauth/apple/callback`
     - Add: `http://localhost:8080/api/v1/auth/oauth/apple/callback`
8. **Click:** "Save" → "Continue" → "Register"

**Save this:**
- Service ID (Client ID): `com.oddiya.service`

---

### Step 3: Create a Sign In with Apple Key (5 minutes)

**Purpose:** Private key for server-to-server authentication

1. **Go to:** https://developer.apple.com/account/resources/authkeys/list
2. **Click:** "+" button (top left)
3. **Key Name:** `Oddiya Sign In Key`
4. ✅ Check "Sign In with Apple"
5. **Click:** "Configure" next to "Sign In with Apple"
6. **Primary App ID:** Select `com.oddiya.app`
7. **Click:** "Save" → "Continue" → "Register"
8. **Download Key:**
   - **Important:** You can only download this ONCE!
   - Downloads: `AuthKey_XXXXXXXXXX.p8`
   - **Key ID:** Shown on the confirmation page (10 characters, e.g., `ABC123DEFG`)

**Save these:**
- Key ID: `ABC123DEFG` (shown on download page)
- Private Key file: `AuthKey_XXXXXXXXXX.p8`

**⚠️ CRITICAL:** Save the .p8 file securely! You cannot re-download it!

```bash
# Move to secure location
mv ~/Downloads/AuthKey_*.p8 ~/.ssh/apple_sign_in_key.p8
chmod 400 ~/.ssh/apple_sign_in_key.p8
```

---

### Step 4: Get Your Team ID (1 minute)

**Purpose:** Identifies your Apple Developer account

1. **Go to:** https://developer.apple.com/account
2. **Look for:** "Team ID" in the membership details
   - Usually on the right side of the page
   - Format: 10 characters (e.g., `XYZ1234ABC`)

**Save this:**
- Team ID: `XYZ1234ABC`

---

## 🔑 Your Apple OAuth Credentials

After completing the steps above, you should have:

| Credential | Example Value | Where to Find |
|------------|---------------|---------------|
| **Client ID** (Service ID) | `com.oddiya.service` | Step 2 - Services ID |
| **Team ID** | `XYZ1234ABC` | Step 4 - Account page |
| **Key ID** | `ABC123DEFG` | Step 3 - Key download page |
| **Private Key** | `~/.ssh/apple_sign_in_key.p8` | Step 3 - Downloaded file |

---

## 📝 Add to Configuration Files

### Option 1: Manual Edit

**Edit `.env` file:**
```bash
nano .env
```

**Find these lines and update:**
```bash
# Apple Sign In (for iOS App)
APPLE_CLIENT_ID=com.oddiya.service           # Your Service ID
APPLE_TEAM_ID=XYZ1234ABC                     # Your Team ID
APPLE_KEY_ID=ABC123DEFG                      # Your Key ID
APPLE_PRIVATE_KEY=~/.ssh/apple_sign_in_key.p8  # Path to .p8 file
```

**Edit `terraform.tfvars` file:**
```bash
nano infrastructure/terraform/phase1/terraform.tfvars
```

**Update these lines:**
```hcl
apple_client_id = "com.oddiya.service"
apple_team_id = "XYZ1234ABC"
apple_key_id = "ABC123DEFG"
apple_private_key = "~/.ssh/apple_sign_in_key.p8"
```

### Option 2: Automated Script

```bash
# Set your Apple credentials
CLIENT_ID="com.oddiya.service"
TEAM_ID="XYZ1234ABC"
KEY_ID="ABC123DEFG"
PRIVATE_KEY_PATH="~/.ssh/apple_sign_in_key.p8"

# Update .env file
sed -i '' "s|APPLE_CLIENT_ID=.*|APPLE_CLIENT_ID=$CLIENT_ID|g" .env
sed -i '' "s|APPLE_TEAM_ID=.*|APPLE_TEAM_ID=$TEAM_ID|g" .env
sed -i '' "s|APPLE_KEY_ID=.*|APPLE_KEY_ID=$KEY_ID|g" .env
sed -i '' "s|APPLE_PRIVATE_KEY=.*|APPLE_PRIVATE_KEY=$PRIVATE_KEY_PATH|g" .env

# Update terraform.tfvars
sed -i '' "s|apple_client_id = \".*\"|apple_client_id = \"$CLIENT_ID\"|g" infrastructure/terraform/phase1/terraform.tfvars
sed -i '' "s|apple_team_id = \".*\"|apple_team_id = \"$TEAM_ID\"|g" infrastructure/terraform/phase1/terraform.tfvars
sed -i '' "s|apple_key_id = \".*\"|apple_key_id = \"$KEY_ID\"|g" infrastructure/terraform/phase1/terraform.tfvars
sed -i '' "s|apple_private_key = \".*\"|apple_private_key = \"$PRIVATE_KEY_PATH\"|g" infrastructure/terraform/phase1/terraform.tfvars

echo "✅ Apple OAuth credentials updated!"
```

---

## 🔒 Private Key Handling

### Option 1: File Path (Recommended for Local)
```bash
APPLE_PRIVATE_KEY=~/.ssh/apple_sign_in_key.p8
```

### Option 2: Base64 Encoded (Recommended for Production)
```bash
# Encode the private key
base64 ~/.ssh/apple_sign_in_key.p8 | tr -d '\n' > apple_key_base64.txt

# Use in .env
APPLE_PRIVATE_KEY=LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0t...
```

### Option 3: Environment Variable (AWS Secrets Manager)
```bash
# Store in AWS Secrets Manager
aws secretsmanager create-secret \
  --name oddiya/prod/apple-private-key \
  --secret-string file://~/.ssh/apple_sign_in_key.p8 \
  --region ap-northeast-2
```

---

## ✅ Verify Configuration

Run this command to check if Apple OAuth is properly configured:

```bash
# Check .env
cat .env | grep APPLE_

# Should show:
# APPLE_CLIENT_ID=com.oddiya.service
# APPLE_TEAM_ID=XYZ1234ABC
# APPLE_KEY_ID=ABC123DEFG
# APPLE_PRIVATE_KEY=~/.ssh/apple_sign_in_key.p8

# Verify private key file exists
ls -la ~/.ssh/apple_sign_in_key.p8
# Should show: -r-------- (read-only)
```

---

## 🧪 Test Apple Sign In

### Local Testing

```bash
# Start Auth Service
cd services/auth-service
./gradlew bootRun

# Test Apple OAuth endpoint
curl http://localhost:8081/api/v1/auth/oauth/apple/login
# Should redirect to Apple sign-in page
```

### iOS App Configuration

**Update your iOS app's configuration:**

```swift
// In your iOS app (SwiftUI)
import AuthenticationServices

Button("Sign in with Apple") {
    let request = ASAuthorizationAppleIDProvider().createRequest()
    request.requestedScopes = [.fullName, .email]
    
    let controller = ASAuthorizationController(authorizationRequests: [request])
    controller.performRequests()
}
```

---

## 🔄 OAuth Flow

```
┌─────────────────────────────────────────────────────────┐
│                  Apple Sign In Flow                     │
└─────────────────────────────────────────────────────────┘

1. User taps "Sign in with Apple" in iOS app
   ↓
2. iOS app initiates Apple authentication
   ↓
3. User authenticates with Face ID/Touch ID/Password
   ↓
4. Apple returns authorization code to app
   ↓
5. App sends code to your backend:
   POST https://oddiya.click/api/v1/auth/oauth/apple/callback
   ↓
6. Your backend:
   • Validates authorization code with Apple
   • Exchanges code for Apple ID token
   • Verifies token signature
   • Creates/updates user in database
   • Returns JWT access token
   ↓
7. App stores JWT and user is signed in ✅
```

---

## 🔍 Troubleshooting

### Issue: "Invalid client_id"
**Solution:** 
- Verify Service ID matches exactly: `com.oddiya.service`
- Check that Service ID is registered in Apple Developer Console
- Ensure "Sign In with Apple" is enabled for the Service ID

### Issue: "Invalid redirect_uri"
**Solution:**
- Add your domain to "Domains and Subdomains" in Service ID config
- Add callback URL to "Return URLs" exactly as configured
- For local testing, add `http://localhost:8080/api/v1/auth/oauth/apple/callback`

### Issue: "Invalid Key ID"
**Solution:**
- Key ID is 10 characters (e.g., `ABC123DEFG`)
- Check the Key ID in Apple Developer Console → Keys
- Ensure it matches the key you downloaded

### Issue: "Invalid private key"
**Solution:**
- Verify .p8 file is readable: `cat ~/.ssh/apple_sign_in_key.p8`
- Check file permissions: `ls -la ~/.ssh/apple_sign_in_key.p8`
- Should show: `-r--------` (400 permissions)
- Re-download key if corrupted (only possible if not already downloaded)

### Issue: "Team ID not found"
**Solution:**
- Go to https://developer.apple.com/account
- Team ID is shown in membership details (10 characters)
- Format: `ABC123DEFG`

---

## 💰 Cost Breakdown

| Item | Cost | Frequency |
|------|------|-----------|
| **Apple Developer Program** | $99 | Annual |
| **API Usage** | Free | Unlimited |
| **Total** | **$99/year** | - |

**Note:** Required for iOS app distribution anyway, so no additional cost for Sign in with Apple!

---

## 🎯 Current Status

**Google OAuth:** ✅ Configured
- Client ID: `201806680568-34bjg6mnu76939outdakjbf8gmme1r5m.apps.googleusercontent.com`
- Client Secret: Configured
- Status: Ready for Android/iOS

**Apple OAuth:** ⚠️ Pending
- Configuration files: ✅ Ready
- Credentials: ⏳ Need to add (follow this guide)
- Status: Optional - Add when ready for iOS App Store

---

## 📚 Additional Resources

### Official Documentation
- **Sign in with Apple:** https://developer.apple.com/sign-in-with-apple/
- **REST API:** https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api
- **Configure Services:** https://help.apple.com/developer-account/#/dev1c0e25352

### Code Examples
- **Backend (Spring Boot):** See `services/auth-service/src/main/java/com/oddiya/auth/service/AppleOAuthService.java`
- **iOS (Swift):** See `mobile/ios/Oddiya/Services/AppleAuthService.swift`

### Support
- **Apple Developer Forums:** https://developer.apple.com/forums/
- **Technical Support:** https://developer.apple.com/support/technical/

---

## 🎊 Next Steps

### If You Have Apple Developer Account:
1. ✅ Follow steps above to get credentials
2. ✅ Add to `.env` and `terraform.tfvars`
3. ✅ Test with iOS app
4. ✅ Deploy to production

### If You Don't Have Apple Developer Account Yet:
1. ⏳ Continue with Google OAuth only (already working!)
2. ⏳ Sign up for Apple Developer Program when ready
3. ⏳ Follow this guide to add Apple Sign In later
4. ⏳ No impact on current deployment

**Google OAuth is sufficient for MVP launch!** 🚀

---

**Status:** Configuration files ready, waiting for Apple credentials  
**Required:** Only for iOS App Store submission  
**Optional:** Can deploy without Apple OAuth initially

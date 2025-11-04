# 🔑 How to Get Your Gemini API Key

## You Need TWO Different Keys (Don't Confuse Them!)

### ✅ Already Have: Google OAuth (for login)
- `client_id`: 201806680568-34bjg6mnu76939outdakjbf8gmme1r5m...
- `client_secret`: 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
- **Purpose:** User sign-in with Google
- **Status:** ✅ DONE - Already in .env file

### ⚠️ Still Need: Gemini API Key (for AI)
- **Format:** `AIzaSyD...` (39 characters, starts with AIzaSy)
- **Purpose:** AI travel plan generation
- **Status:** ⚠️ MISSING - Need to get this!

---

## Step-by-Step: Get Gemini API Key

### Step 1: Open the Correct Website
```bash
open https://ai.google.dev/
```

**Important:** This is a DIFFERENT website from where you got OAuth credentials!
- OAuth credentials: https://console.cloud.google.com/ ✅ (already done)
- Gemini API key: https://ai.google.dev/ ⚠️ (need to do this)

### Step 2: Sign In
- Sign in with your Google account
- Same account you used for OAuth is fine

### Step 3: Get API Key
1. Look for "**Get API Key**" or "**API Key**" button
2. Click it
3. You might see "**Create API key in new project**" or "**Create API key**"
4. Click to generate a new key

### Step 4: Copy Your Key
- Your key will look like: `AIzaSyDlMvCLaGNMbPJXvnNkpjf_d4gOQOr5Hbk`
- **Must start with:** `AIzaSy`
- **Length:** 39 characters
- Copy the entire key

### Step 5: Add to 3 Files

**Option 1: Automated (Recommended)**
```bash
# Replace the value below with your ACTUAL Gemini API key
KEY="AIzaSyDlMvCLaGNMbPJXvnNkpjf_d4gOQOr5Hbk"

# Update all 3 files at once
sed -i '' "s/PASTE_YOUR_GEMINI_API_KEY_HERE/$KEY/g" .env
sed -i '' "s/PASTE_YOUR_GEMINI_API_KEY_HERE/$KEY/g" services/llm-agent/.env
sed -i '' "s/PASTE_YOUR_GEMINI_API_KEY_HERE/$KEY/g" infrastructure/terraform/phase1/terraform.tfvars

# Verify it worked
./scripts/verify-api-key.sh
```

**Option 2: Manual**
```bash
# Edit each file
nano .env
nano services/llm-agent/.env
nano infrastructure/terraform/phase1/terraform.tfvars

# Find line: GOOGLE_API_KEY=PASTE_YOUR_GEMINI_API_KEY_HERE
# Replace with: GOOGLE_API_KEY=AIzaSyD...your-actual-key
# Save each file
```

### Step 6: Verify
```bash
./scripts/verify-api-key.sh
```

**Expected output:**
```
✅ CONFIGURED: .env
✅ CONFIGURED: services/llm-agent/.env
✅ CONFIGURED: infrastructure/terraform/phase1/terraform.tfvars
```

---

## Visual Guide

```
┌─────────────────────────────────────────────────────┐
│  What You Already Have (OAuth - for login)         │
├─────────────────────────────────────────────────────┤
│  From: https://console.cloud.google.com/            │
│  Purpose: User authentication                       │
│  Format: client_id (long URL-like string)          │
│  Example: 201806680568-34bjg...googleusercontent.com│
│  Status: ✅ DONE                                    │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  What You Still Need (Gemini - for AI)             │
├─────────────────────────────────────────────────────┤
│  From: https://ai.google.dev/                       │
│  Purpose: AI travel planning                        │
│  Format: AIzaSy... (39 characters)                  │
│  Example: AIzaSyDlMvCLaGNMbPJXvnNkpjf_d4gOQOr5Hbk   │
│  Status: ⚠️ NEED TO GET                            │
└─────────────────────────────────────────────────────┘
```

---

## Screenshots/Visual Steps

### 1. Go to ai.google.dev
![URL Bar]
```
https://ai.google.dev/
```

### 2. Look for Button
- Button text: "Get API Key" or "Get Started"
- Usually in top right or center of page

### 3. Create API Key
- Click "Create API key in new project"
- Or select existing project
- Key will be generated instantly

### 4. Copy Key
```
Your API key: AIzaSyDlMvCLaGNMbPJXvnNkpjf_d4gOQOr5Hbk
              ↑ Copy this entire string
```

---

## Common Mistakes

❌ **Wrong:** Using OAuth client_id as Gemini key
```bash
GOOGLE_API_KEY=201806680568-34bjg...  # This is OAuth, not Gemini!
```

✅ **Correct:** Using actual Gemini API key
```bash
GOOGLE_API_KEY=AIzaSyDlMvCLaGNMbPJXv...  # Starts with AIzaSy
```

---

## Troubleshooting

### "I can't find the Get API Key button"
1. Make sure you're on https://ai.google.dev/ (NOT console.cloud.google.com)
2. Try clicking "Get Started" or "Try Gemini API"
3. Sign in if you're not already signed in
4. Look for "API" or "Keys" in the navigation

### "My key doesn't start with AIzaSy"
- You might have copied something else (client ID, secret, etc.)
- Go back to ai.google.dev and look specifically for "API Key"
- The Gemini key ALWAYS starts with `AIzaSy`

### "I have a key but verify script still fails"
```bash
# Check what's actually in the file
cat .env | grep GOOGLE_API_KEY

# Should show:
# GOOGLE_API_KEY=AIzaSy...

# Should NOT show:
# GOOGLE_API_KEY=PASTE_YOUR_GEMINI_API_KEY_HERE
```

---

## After You Get the Key

1. ✅ Add to all 3 files (use commands above)
2. ✅ Verify with `./scripts/verify-api-key.sh`
3. ✅ Test LLM Agent: `cd services/llm-agent && python main.py`
4. ✅ Ready for AWS deployment!

---

## Free Tier Limits

**Good news:** Gemini API has a generous free tier!
- ✅ 60 requests per minute
- ✅ 1,500 requests per day
- ✅ Perfect for testing and MVP
- ✅ No credit card required initially

---

## Need More Help?

**Still stuck? Try these:**

1. **Direct link to API keys page:**
   ```bash
   open https://aistudio.google.com/app/apikey
   ```

2. **Alternative method (Google AI Studio):**
   ```bash
   open https://makersuite.google.com/app/apikey
   ```

3. **Video tutorial:** Search YouTube for "How to get Gemini API key 2024"

---

**Once you have your Gemini API key, you're 100% ready to deploy! 🚀**

# 🚀 Oddiya Expo Deployment - Quick Start

## Current Progress: ✅ Expo Installed, ✅ app.json Created

---

## Step 3: Login to Expo (2 minutes)

Open your terminal and run:

```bash
cd /Users/wjs/cursor/oddiya/mobile

# Login with your Expo account
eas login
```

**Enter:**
- Email: (your Expo account email)
- Password: (your password)

---

## Step 4: Initialize EAS Build (2 minutes)

```bash
# Initialize EAS Build (this will create eas.json)
eas build:configure
```

**When prompted:**
- "Generate a new Android Keystore?" → **Yes**
- "Generate credentials for iOS?" → **Skip for now** (or Yes if you have Apple Developer account)

This creates `eas.json` file automatically.

---

## Step 5: First Build - Android Only (15 minutes)

```bash
# Build Android APK
eas build --platform android --profile production
```

**What happens:**
1. EAS uploads your code to cloud
2. Builds Android APK (takes 10-15 minutes)
3. Sends download link to your email

**While waiting:**
- Check build status: https://expo.dev/accounts/YOUR_USERNAME/projects/oddiya/builds
- You'll get email notification when complete

---

## Step 6: Download and Test

After build completes:

```bash
# Download APK
eas build:download --platform android

# Install on Android device/emulator
adb install app-release.apk
```

---

## Step 7: Build iOS (Optional - requires Apple Developer account)

If you have Apple Developer account ($99/year):

```bash
# Build iOS IPA
eas build --platform ios --profile production
```

**When prompted:**
- Enter Apple ID
- EAS will handle certificates automatically

---

## Step 8: Build Both Simultaneously (5 minutes)

Once Android works, build both:

```bash
# 🎉 Build Android + iOS together
eas build --platform all --profile production
```

**Result:**
- Android APK: Ready in ~15 minutes
- iOS IPA: Ready in ~15 minutes
- Both download links sent to email

---

## Quick Commands Reference

```bash
# Login
eas login

# Setup
eas build:configure

# Build Android only
eas build --platform android

# Build iOS only  
eas build --platform ios

# Build both
eas build --platform all

# Check build status
eas build:list

# Download builds
eas build:download --platform android
eas build:download --platform ios
```

---

## Troubleshooting

### "Not logged in"
```bash
eas logout
eas login
```

### "Project not configured"
```bash
eas build:configure
```

### Build failed
```bash
# Check logs
eas build:list
# Click on failed build to see error details
```

---

## Cost Reminder

- **Free tier:** 30 builds/month
- **Your usage:** ~1-2 builds/day = ~30-60/month
- **If needed:** $29/month for unlimited builds

---

## Next Steps After First Successful Build

1. **Test APK** on Android device
2. **Update API URL** in `src/constants/config.ts` to your EC2 IP
3. **Add real app icons** to `assets/` folder
4. **Build final version** with `eas build --platform all`
5. **Submit to stores** with `eas submit`

---

**Ready?** Run Step 3 now:

```bash
cd /Users/wjs/cursor/oddiya/mobile
eas login
```

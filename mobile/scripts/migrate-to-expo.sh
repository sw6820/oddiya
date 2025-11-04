#!/bin/bash
# Expo 자동 마이그레이션 스크립트

set -e

echo "🚀 Oddiya - Expo 마이그레이션"
echo "================================"
echo ""

cd "$(dirname "$0")/.."

# Step 1: Expo 패키지 설치
echo "📦 Step 1/5: Expo 패키지 설치 중..."
npm install expo expo-dev-client

# Step 2: EAS CLI 설치
echo "📦 Step 2/5: EAS CLI 설치 중..."
npm install -g eas-cli

# Step 3: app.json 생성
echo "📝 Step 3/5: app.json 생성 중..."
cat > app.json << 'APPJSON'
{
  "expo": {
    "name": "Oddiya",
    "slug": "oddiya",
    "version": "1.0.0",
    "orientation": "portrait",
    "icon": "./assets/icon.png",
    "userInterfaceStyle": "light",
    "splash": {
      "image": "./assets/splash.png",
      "resizeMode": "contain",
      "backgroundColor": "#667eea"
    },
    "assetBundlePatterns": ["**/*"],
    "ios": {
      "supportsTablet": true,
      "bundleIdentifier": "com.oddiya.app",
      "buildNumber": "1.0.0"
    },
    "android": {
      "adaptiveIcon": {
        "foregroundImage": "./assets/adaptive-icon.png",
        "backgroundColor": "#667eea"
      },
      "package": "com.oddiya.app",
      "versionCode": 1
    },
    "web": {
      "favicon": "./assets/favicon.png"
    },
    "extra": {
      "eas": {
        "projectId": "your-project-id"
      }
    }
  }
}
APPJSON

# Step 4: 에셋 폴더 생성
echo "🎨 Step 4/5: 에셋 폴더 생성 중..."
mkdir -p assets

# 기본 아이콘 생성 (임시 - 나중에 실제 로고로 교체)
cat > assets/icon.png << 'ICONEOF'
# Placeholder - Replace with actual icon
ICONEOF

cat > assets/splash.png << 'SPLASHEOF'
# Placeholder - Replace with actual splash
SPLASHEOF

echo ""
echo "⚠️  임시 아이콘이 생성되었습니다."
echo "   실제 아이콘으로 교체하세요:"
echo "   - assets/icon.png (1024x1024)"
echo "   - assets/splash.png (1242x2688)"
echo "   - assets/adaptive-icon.png (1024x1024)"
echo ""

# Step 5: package.json 업데이트
echo "📝 Step 5/5: package.json 업데이트 중..."
npx json -I -f package.json -e '
this.scripts["build:all"] = "eas build --platform all --profile production";
this.scripts["build:android"] = "eas build --platform android --profile production";
this.scripts["build:ios"] = "eas build --platform ios --profile production";
this.scripts["submit:android"] = "eas submit --platform android";
this.scripts["submit:ios"] = "eas submit --platform ios";
' 2>/dev/null || echo "⚠️  수동으로 package.json 스크립트를 추가하세요"

echo ""
echo "✅ Expo 마이그레이션 완료!"
echo "================================"
echo ""
echo "다음 단계:"
echo "1. Expo 로그인:"
echo "   eas login"
echo ""
echo "2. EAS Build 설정:"
echo "   eas build:configure"
echo ""
echo "3. Android + iOS 동시 빌드:"
echo "   npm run build:all"
echo ""
echo "4. 빌드 모니터링:"
echo "   https://expo.dev"
echo ""

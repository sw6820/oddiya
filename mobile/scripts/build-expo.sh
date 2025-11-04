#!/bin/bash
# Expo 빌드 자동화 스크립트 - Android & iOS 동시 빌드

set -e

echo "🚀 Oddiya - Android & iOS 빌드"
echo "=============================="
echo ""

cd "$(dirname "$0")/.."

# 빌드 옵션 선택
echo "빌드 옵션을 선택하세요:"
echo "1) Android만"
echo "2) iOS만"
echo "3) 둘 다 (Android + iOS) ⭐ 추천"
echo ""
read -p "선택 [1-3]: " choice

case $choice in
  1)
    PLATFORM="android"
    echo "📱 Android APK 빌드 시작..."
    ;;
  2)
    PLATFORM="ios"
    echo "🍎 iOS IPA 빌드 시작..."
    ;;
  3)
    PLATFORM="all"
    echo "📱🍎 Android + iOS 동시 빌드 시작..."
    ;;
  *)
    echo "❌ 잘못된 선택"
    exit 1
    ;;
esac

echo ""
echo "⏳ 빌드 제출 중..."
echo "   (예상 소요 시간: 10-15분)"
echo ""

# EAS Build 실행
eas build --platform $PLATFORM --profile production --non-interactive

echo ""
echo "✅ 빌드가 제출되었습니다!"
echo "=============================="
echo ""
echo "📊 빌드 상태 확인:"
echo "   https://expo.dev/accounts/YOUR_USERNAME/projects/oddiya/builds"
echo ""
echo "📧 빌드 완료 시 이메일로 알림을 받게 됩니다."
echo ""
echo "💾 빌드 다운로드:"
if [ "$PLATFORM" = "android" ] || [ "$PLATFORM" = "all" ]; then
  echo "   Android: eas build:download --platform android"
fi
if [ "$PLATFORM" = "ios" ] || [ "$PLATFORM" = "all" ]; then
  echo "   iOS: eas build:download --platform ios"
fi
echo ""

import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Image,
  TouchableOpacity,
  Dimensions,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '@/navigation/types';
import { useAppDispatch } from '@/store/hooks';
import { loginWithGoogle, loginWithApple } from '@/store/slices/authSlice';
import { Platform } from 'react-native';
import { appleSignInService } from '@/services/appleSignInService';

type Props = NativeStackScreenProps<RootStackParamList, 'Welcome'>;

const { width } = Dimensions.get('window');

export default function WelcomeScreen({ navigation }: Props) {
  const dispatch = useAppDispatch();
  const [isGoogleLoading, setIsGoogleLoading] = useState(false);
  const [isAppleLoading, setIsAppleLoading] = useState(false);
  const [isAppleAvailable, setIsAppleAvailable] = useState(false);

  // Check if Apple Sign-In is available (iOS 13+)
  React.useEffect(() => {
    const checkAppleAvailability = async () => {
      const available = await appleSignInService.isAvailable();
      setIsAppleAvailable(available);
      console.log('[WelcomeScreen] Apple Sign-In available:', available);
    };
    checkAppleAvailability();
  }, []);

  const handleGoogleSignIn = async () => {
    setIsGoogleLoading(true);
    try {
      console.log('[WelcomeScreen] Starting Google Sign-In...');
      await dispatch(loginWithGoogle()).unwrap();
      console.log('[WelcomeScreen] ✅ Google Sign-In successful');
      // Navigation is handled by App.tsx based on auth state
    } catch (error: any) {
      console.error('[WelcomeScreen] ❌ Google Sign-In failed:', error);
      Alert.alert(
        '로그인 실패',
        error.message || 'Google 로그인에 실패했습니다. 다시 시도해주세요.',
        [{ text: '확인' }]
      );
    } finally {
      setIsGoogleLoading(false);
    }
  };

  const handleAppleSignIn = async () => {
    setIsAppleLoading(true);
    try {
      console.log('[WelcomeScreen] Starting Apple Sign-In...');
      await dispatch(loginWithApple()).unwrap();
      console.log('[WelcomeScreen] ✅ Apple Sign-In successful');
      // Navigation is handled by App.tsx based on auth state
    } catch (error: any) {
      console.error('[WelcomeScreen] ❌ Apple Sign-In failed:', error);
      Alert.alert(
        '로그인 실패',
        error.message || 'Apple 로그인에 실패했습니다. 다시 시도해주세요.',
        [{ text: '확인' }]
      );
    } finally {
      setIsAppleLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      {/* Logo */}
      <View style={styles.logoContainer}>
        <Text style={styles.logo}>🗺️</Text>
        <Text style={styles.appName}>오디야</Text>
        <Text style={styles.tagline}>AI 기반 여행 플래너</Text>
      </View>

      {/* Features */}
      <View style={styles.featuresContainer}>
        <FeatureItem
          emoji="🤖"
          title="AI 여행 계획"
          description="AI가 제공하는 맞춤형 여행 일정"
        />
        <FeatureItem
          emoji="🎥"
          title="비디오 추억"
          description="자동으로 아름다운 여행 영상 제작"
        />
        <FeatureItem
          emoji="🌍"
          title="한국 탐험"
          description="숨은 명소와 로컬 경험 탐색"
        />
      </View>

      {/* CTA Buttons */}
      <View style={styles.buttonContainer}>
        {/* Google Sign-In Button */}
        <TouchableOpacity
          style={styles.googleButton}
          onPress={handleGoogleSignIn}
          activeOpacity={0.8}
          disabled={isGoogleLoading || isAppleLoading}>
          {isGoogleLoading ? (
            <ActivityIndicator color="#FFFFFF" />
          ) : (
            <>
              <Text style={styles.googleIcon}>🔵</Text>
              <Text style={styles.googleButtonText}>Google로 계속하기</Text>
            </>
          )}
        </TouchableOpacity>

        {/* Apple Sign-In Button */}
        {isAppleAvailable && (
          <TouchableOpacity
            style={styles.appleButton}
            onPress={handleAppleSignIn}
            activeOpacity={0.8}
            disabled={isGoogleLoading || isAppleLoading}>
            {isAppleLoading ? (
              <ActivityIndicator color="#FFFFFF" />
            ) : (
              <>
                <Text style={styles.appleIcon}></Text>
                <Text style={styles.appleButtonText}>Apple로 계속하기</Text>
              </>
            )}
          </TouchableOpacity>
        )}

        {/* Terms */}
        <Text style={styles.termsText}>
          계속하면{' '}
          <Text style={styles.termsLink}>서비스 약관</Text>
          {' '}및{' '}
          <Text style={styles.termsLink}>개인정보 보호정책</Text>
          에 동의하게 됩니다
        </Text>
      </View>
    </View>
  );
}

function FeatureItem({
  emoji,
  title,
  description,
}: {
  emoji: string;
  title: string;
  description: string;
}) {
  return (
    <View style={styles.featureItem}>
      <Text style={styles.featureEmoji}>{emoji}</Text>
      <View style={styles.featureTextContainer}>
        <Text style={styles.featureTitle}>{title}</Text>
        <Text style={styles.featureDescription}>{description}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 24,
    paddingTop: 60,
    paddingBottom: 40,
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 48,
  },
  logo: {
    fontSize: 80,
    marginBottom: 16,
  },
  appName: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#1A1A1A',
    marginBottom: 8,
  },
  tagline: {
    fontSize: 16,
    color: '#666666',
  },
  featuresContainer: {
    flex: 1,
    justifyContent: 'center',
  },
  featureItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 32,
  },
  featureEmoji: {
    fontSize: 40,
    marginRight: 16,
  },
  featureTextContainer: {
    flex: 1,
  },
  featureTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1A1A1A',
    marginBottom: 4,
  },
  featureDescription: {
    fontSize: 14,
    color: '#666666',
    lineHeight: 20,
  },
  buttonContainer: {
    gap: 16,
  },
  googleButton: {
    backgroundColor: '#4285F4', // Google Blue
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  googleIcon: {
    fontSize: 24,
    marginRight: 12,
  },
  googleButtonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
  },
  appleButton: {
    backgroundColor: '#000000', // Apple Black
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  appleIcon: {
    fontSize: 24,
    marginRight: 12,
  },
  appleButtonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
  },
  testButton: {
    backgroundColor: '#F5F5F5',
    paddingVertical: 12,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 8,
    borderWidth: 1,
    borderColor: '#E0E0E0',
  },
  testButtonText: {
    color: '#666666',
    fontSize: 14,
    fontWeight: '500',
  },
  disabledButton: {
    opacity: 0.5,
  },
  termsText: {
    fontSize: 12,
    color: '#999999',
    textAlign: 'center',
    marginTop: 8,
    lineHeight: 18,
  },
  termsLink: {
    color: '#007AFF',
    textDecorationLine: 'underline',
  },
});

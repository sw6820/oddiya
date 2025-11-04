/**
 * Google Sign-In Service for React Native
 *
 * This service wraps @react-native-google-signin/google-signin
 * and provides a clean API for Google OAuth authentication.
 */

import {
  GoogleSignin,
  statusCodes,
  User,
} from '@react-native-google-signin/google-signin';

export interface GoogleUser {
  id: string;
  email: string;
  name: string;
  photo?: string;
  idToken: string;
}

class GoogleSignInService {
  private configured = false;

  /**
   * Configure Google Sign-In
   * Must be called before any other methods
   *
   * @param webClientId - OAuth 2.0 Web Client ID from Google Cloud Console
   */
  configure(webClientId: string): void {
    if (this.configured) {
      return;
    }

    GoogleSignin.configure({
      webClientId, // From Google Cloud Console (Web application type)
      offlineAccess: false, // We don't need offline access
      hostedDomain: '', // Optional: restrict to specific domain
      forceCodeForRefreshToken: false,
    });

    this.configured = true;
  }

  /**
   * Sign in with Google
   * Opens Google Sign-In dialog
   *
   * @returns GoogleUser object with user info and ID token
   * @throws Error if sign-in fails or is cancelled
   */
  async signIn(): Promise<GoogleUser> {
    try {
      // Check if configured
      if (!this.configured) {
        throw new Error(
          'GoogleSignInService not configured. Call configure() first.'
        );
      }

      // Check if Google Play Services are available (Android)
      await GoogleSignin.hasPlayServices({
        showPlayServicesUpdateDialog: true,
      });

      // Sign in
      const userInfo: User = await GoogleSignin.signIn();

      // Get ID token
      const tokens = await GoogleSignin.getTokens();

      return {
        id: userInfo.user.id,
        email: userInfo.user.email,
        name: userInfo.user.name || '',
        photo: userInfo.user.photo || undefined,
        idToken: tokens.idToken,
      };
    } catch (error: any) {
      // Handle specific error cases
      if (error.code === statusCodes.SIGN_IN_CANCELLED) {
        throw new Error('Google Sign-In cancelled by user');
      } else if (error.code === statusCodes.IN_PROGRESS) {
        throw new Error('Google Sign-In already in progress');
      } else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
        throw new Error('Google Play Services not available or outdated');
      } else {
        throw new Error(`Google Sign-In failed: ${error.message}`);
      }
    }
  }

  /**
   * Sign out from Google
   * Clears Google Sign-In session
   */
  async signOut(): Promise<void> {
    try {
      await GoogleSignin.signOut();
    } catch (error) {
      console.error('Google sign-out error:', error);
      // Don't throw - sign out should always succeed locally
    }
  }

  /**
   * Revoke access to Google account
   * Disconnects the app from user's Google account
   */
  async revokeAccess(): Promise<void> {
    try {
      await GoogleSignin.revokeAccess();
    } catch (error) {
      console.error('Google revoke access error:', error);
    }
  }

  /**
   * Check if user is currently signed in to Google
   *
   * @returns true if signed in, false otherwise
   */
  async isSignedIn(): Promise<boolean> {
    return await GoogleSignin.isSignedIn();
  }

  /**
   * Get currently signed in user (if any)
   *
   * @returns GoogleUser or null if not signed in
   */
  async getCurrentUser(): Promise<GoogleUser | null> {
    try {
      const userInfo = await GoogleSignin.signInSilently();
      const tokens = await GoogleSignin.getTokens();

      return {
        id: userInfo.user.id,
        email: userInfo.user.email,
        name: userInfo.user.name || '',
        photo: userInfo.user.photo || undefined,
        idToken: tokens.idToken,
      };
    } catch (error) {
      return null;
    }
  }
}

// Export singleton instance
export const googleSignInService = new GoogleSignInService();

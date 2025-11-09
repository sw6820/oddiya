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
   * @param iosClientId - (Optional) OAuth 2.0 iOS Client ID from Google Cloud Console
   */
  configure(webClientId: string, iosClientId?: string): void {
    if (this.configured) {
      return;
    }

    try {
      GoogleSignin.configure({
        webClientId,
        iosClientId, // iOS requires this to be set explicitly
        offlineAccess: true,
        forceCodeForRefreshToken: true,
      });
      this.configured = true;
      console.log('✅ Google Sign-In configured successfully');
    } catch (error) {
      console.error('❌ Failed to configure Google Sign-In:', error);
      throw error;
    }
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
      await GoogleSignin.hasPlayServices();
      const userInfo: User = await GoogleSignin.signIn();

      if (!userInfo.data?.user) {
        throw new Error('Failed to get user information from Google');
      }

      const {user, idToken} = userInfo.data;

      return {
        id: user.id,
        email: user.email,
        name: user.name || '',
        photo: user.photo || undefined,
        idToken: idToken || '',
      };
    } catch (error: any) {
      if (error.code === statusCodes.SIGN_IN_CANCELLED) {
        throw new Error('Sign in was cancelled');
      } else if (error.code === statusCodes.IN_PROGRESS) {
        throw new Error('Sign in is already in progress');
      } else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
        throw new Error('Google Play Services not available. Please update Google Play Services and try again.');
      } else {
        console.error('Google Sign-In error:', error);
        throw new Error('Failed to sign in with Google. Please try again later.');
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
      console.log('✅ Google sign-out successful');
    } catch (error) {
      console.error('❌ Google sign-out error:', error);
      throw error;
    }
  }

  /**
   * Revoke access to Google account
   * Disconnects the app from user's Google account
   */
  async revokeAccess(): Promise<void> {
    try {
      await GoogleSignin.revokeAccess();
      console.log('✅ Google access revoked');
    } catch (error) {
      console.error('❌ Google revoke access error:', error);
      throw error;
    }
  }

  /**
   * Check if user is currently signed in to Google
   *
   * @returns true if signed in, false otherwise
   */
  async isSignedIn(): Promise<boolean> {
    try {
      return await GoogleSignin.isSignedIn();
    } catch (error) {
      console.error('❌ Failed to check Google sign-in status:', error);
      return false;
    }
  }

  /**
   * Get currently signed in user (if any)
   *
   * @returns GoogleUser or null if not signed in
   */
  async getCurrentUser(): Promise<GoogleUser | null> {
    try {
      const userInfo = await GoogleSignin.getCurrentUser();
      if (!userInfo?.data?.user) {
        return null;
      }

      const {user, idToken} = userInfo.data;

      return {
        id: user.id,
        email: user.email,
        name: user.name || '',
        photo: user.photo || undefined,
        idToken: idToken || '',
      };
    } catch (error) {
      console.error('❌ Failed to get current Google user:', error);
      return null;
    }
  }
}

// Export singleton instance
export const googleSignInService = new GoogleSignInService();

import React, { useEffect } from 'react';
import { StatusBar, ActivityIndicator, View, StyleSheet, Text, ScrollView, Platform } from 'react-native';
import { Provider, useDispatch } from 'react-redux';
import { store, AppDispatch } from './src/store';
import { loadStoredAuth } from './src/store/slices/authSlice';
import AppNavigator from './src/navigation/AppNavigator';
import { googleSignInService } from './src/services/googleSignInService';
import { GOOGLE_WEB_CLIENT_ID, GOOGLE_IOS_CLIENT_ID } from '@env';

// Error Boundary Component
class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean; error: Error | null }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error) {
    console.error('========== ERROR BOUNDARY CAUGHT ERROR ==========');
    console.error(error);
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Error details:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <View style={styles.errorContainer}>
          <Text style={styles.errorTitle}>App Error</Text>
          <ScrollView style={styles.errorScroll}>
            <Text style={styles.errorText}>
              {this.state.error?.toString()}
            </Text>
            <Text style={styles.errorStack}>
              {this.state.error?.stack}
            </Text>
          </ScrollView>
        </View>
      );
    }

    return this.props.children;
  }
}

function AppContent(): JSX.Element {
  const dispatch = useDispatch<AppDispatch>();
  const [isInitializing, setIsInitializing] = React.useState(true);

  useEffect(() => {
    console.log('========== APP INITIALIZING ==========');

    const initializeApp = async () => {
      // Initialize Google Sign-In
      try {
        console.log('Initializing Google Sign-In...');
        console.log('Platform:', Platform.OS);

        // iOS needs iosClientId, Android uses webClientId
        googleSignInService.configure(
          GOOGLE_WEB_CLIENT_ID,
          Platform.OS === 'ios' ? GOOGLE_IOS_CLIENT_ID : undefined
        );

        console.log('✅ Google Sign-In configured successfully');
      } catch (error) {
        console.error('❌ Failed to configure Google Sign-In:', error);
      }

      // Try to load stored authentication
      try {
        console.log('Loading stored auth...');
        await dispatch(loadStoredAuth()).unwrap();
        console.log('✅ Auth loaded successfully');
      } catch (error) {
        // No stored auth or invalid - user will see welcome screen
        console.log('No stored authentication found:', error);
      } finally {
        setIsInitializing(false);
      }
    };

    initializeApp();
  }, [dispatch]);

  // Show splash screen while checking auth
  if (isInitializing) {
    return (
      <View style={styles.splashContainer}>
        <ActivityIndicator size="large" color="#007AFF" />
        <Text style={styles.splashText}>Loading...</Text>
      </View>
    );
  }

  return (
    <>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      <AppNavigator />
    </>
  );
}

function App(): JSX.Element {
  console.log('========== APP ROOT RENDERING ==========');
  return (
    <ErrorBoundary>
      <Provider store={store}>
        <AppContent />
      </Provider>
    </ErrorBoundary>
  );
}

const styles = StyleSheet.create({
  splashContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
  },
  splashText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666666',
  },
  errorContainer: {
    flex: 1,
    padding: 20,
    backgroundColor: '#FFE5E5',
  },
  errorTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#CC0000',
    marginBottom: 16,
  },
  errorScroll: {
    flex: 1,
  },
  errorText: {
    fontSize: 14,
    color: '#CC0000',
    fontWeight: 'bold',
    marginBottom: 16,
  },
  errorStack: {
    fontSize: 12,
    color: '#666666',
    fontFamily: 'Courier',
  },
});

export default App;

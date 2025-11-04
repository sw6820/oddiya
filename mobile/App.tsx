import React, { useEffect } from 'react';
import { StatusBar, ActivityIndicator, View, StyleSheet } from 'react-native';
import { Provider, useDispatch } from 'react-redux';
import { store, AppDispatch } from './src/store';
import { loadStoredAuth } from './src/store/slices/authSlice';
import AppNavigator from './src/navigation/AppNavigator';
import { googleSignInService } from './src/services/googleSignInService';

// TODO: Replace with your Web Client ID from Google Cloud Console
const GOOGLE_WEB_CLIENT_ID = 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com';

function AppContent(): JSX.Element {
  const dispatch = useDispatch<AppDispatch>();
  const [isInitializing, setIsInitializing] = React.useState(true);

  useEffect(() => {
    // Initialize Google Sign-In
    googleSignInService.configure(GOOGLE_WEB_CLIENT_ID);

    // Try to load stored authentication on app startup
    const initializeAuth = async () => {
      try {
        await dispatch(loadStoredAuth()).unwrap();
      } catch (error) {
        // No stored auth or invalid - user will see welcome screen
        console.log('No stored authentication found');
      } finally {
        setIsInitializing(false);
      }
    };

    initializeAuth();
  }, [dispatch]);

  // Show splash screen while checking auth
  if (isInitializing) {
    return (
      <View style={styles.splashContainer}>
        <ActivityIndicator size="large" color="#007AFF" />
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
  return (
    <Provider store={store}>
      <AppContent />
    </Provider>
  );
}

const styles = StyleSheet.create({
  splashContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
  },
});

export default App;


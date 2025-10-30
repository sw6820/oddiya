/**
 * Navigation type definitions
 */

export type RootStackParamList = {
  // Auth Stack
  Welcome: undefined;
  Login: undefined;
  Signup: undefined;

  // Main App Stack
  MainTabs: undefined;
  Plans: undefined;
  Videos: undefined;
  Profile: undefined;
};

export type AuthStackParamList = {
  Welcome: undefined;
  Login: undefined;
  Signup: undefined;
};

export type MainTabsParamList = {
  Plans: undefined;
  Videos: undefined;
  Profile: undefined;
};

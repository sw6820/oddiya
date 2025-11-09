module.exports = {
  presets: ['module:@react-native/babel-preset'],
  plugins: [
    [
      'module-resolver',
      {
        root: ['./src'],
        alias: {
          '@': './src',
        },
      },
    ],
    [
      'module:react-native-dotenv',
      {
        moduleName: '@env',
        path: '.env',
        safe: false,
        allowUndefined: true,
        // Load order: .env.local (if exists) → .env
        // This allows developers to override .env with .env.local
        blacklist: null,
        whitelist: null,
        envName: 'APP_ENV',
      },
    ],
  ],
};

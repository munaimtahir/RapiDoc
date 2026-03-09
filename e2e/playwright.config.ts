import { defineConfig } from '@playwright/test';
import path from 'path';

/**
 * Playwright configuration for RapiDoc Android e2e tests.
 *
 * Tests use the Playwright Android API (via ADB) to drive the native
 * Android app. An Android emulator or physical device must be connected
 * and the app APK already installed before running the suite.
 *
 * Run with:
 *   cd e2e && npx playwright test
 *
 * CI: see .github/workflows/e2e-playwright.yaml (manual trigger only).
 */
export default defineConfig({
  testDir: path.join(__dirname, 'tests'),
  timeout: 120_000,
  retries: 1,
  workers: 1, // Android tests must run serially (single device)
  reporter: [
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['list'],
  ],
  use: {
    // Screenshots on failure for debugging
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    // Shared test timeout
    actionTimeout: 30_000,
  },
  // Projects allow future expansion to multiple device profiles
  projects: [
    {
      name: 'android-emulator',
      testMatch: '**/*.spec.ts',
    },
  ],
  // Output directory for test artefacts (traces, videos, screenshots)
  outputDir: path.join(__dirname, 'test-results'),
});

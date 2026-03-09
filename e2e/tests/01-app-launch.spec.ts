/**
 * 01-app-launch.spec.ts
 *
 * Verifies that the app installs cleanly, launches without crashing,
 * and reaches the home screen within the expected time-frame.
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, APP_PACKAGE, adbShell } from '../helpers/android-device';

test.describe('App Launch', () => {
  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('app launches without crash', async () => {
    await launchApp();
    const ui = dumpUi();
    expect(ui).toContain('RapiDoc');
  });

  test('home screen is reachable within 10 seconds', async () => {
    const start = Date.now();
    await launchApp();
    const elapsed = Date.now() - start;
    const ui = dumpUi();
    expect(ui).toContain('RapiDoc');
    expect(elapsed).toBeLessThan(10_000);
  });

  test('app is not in a crash/ANR state', async () => {
    await launchApp();
    const ui = dumpUi();
    expect(ui).not.toContain('has stopped');
    expect(ui).not.toContain("isn't responding");
    expect(ui).not.toContain('Application Error');
  });
});


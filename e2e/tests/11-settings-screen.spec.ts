/**
 * 11-settings-screen.spec.ts
 *
 * Tests the Settings screen:
 * - Branding section (header text field, save header button)
 * - Logo upload and clear logo buttons
 * - Appearance / Theme selector
 * - Factory Reset button and confirmation dialog
 * - Back navigation returns to home screen
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, waitForText, APP_PACKAGE, adbShell } from '../helpers/android-device';
import { openSettings, tapButton, typeInField } from '../helpers/app-actions';

test.describe('Settings Screen', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openSettings();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Settings screen shows Branding section', () => {
    expect(dumpUi()).toContain('Branding');
  });

  test('Header text field is present', () => {
    expect(dumpUi()).toContain('Header Text');
  });

  test('Save Header button is present', () => {
    expect(dumpUi()).toContain('Save Header');
  });

  test('Upload Logo button is present', () => {
    expect(dumpUi()).toContain('Upload Logo');
  });

  test('Clear Logo button is present', () => {
    expect(dumpUi()).toContain('Clear Logo');
  });

  test('Appearance section is present', () => {
    expect(dumpUi()).toContain('Appearance');
  });

  test('Theme selector is present', () => {
    expect(dumpUi()).toContain('Theme');
  });

  test('Danger Zone section is present', () => {
    expect(dumpUi()).toContain('Danger Zone');
  });

  test('Factory Reset button is present', () => {
    expect(dumpUi()).toContain('Factory Reset');
  });

  test('Factory Reset button shows confirmation dialog', async () => {
    await tapButton('Factory Reset All Data');
    await waitForText('Factory Reset');
    expect(dumpUi()).toContain('permanently clear');
  });

  test('Cancelling factory reset dialog keeps settings intact', async () => {
    await tapButton('Factory Reset All Data');
    await waitForText('Cancel');
    await tapButton('Cancel');
    expect(dumpUi()).toContain('Danger Zone');
  });

  test('Header text can be updated', async () => {
    await typeInField('Header Text', 'Test Clinic');
    await tapButton('Save Header');
    expect(dumpUi()).toContain('Test Clinic');
  });

  test('Back to Home button is present', () => {
    expect(dumpUi()).toContain('Back to Home');
  });

  test('Tapping Back to Home returns to the home screen', async () => {
    await tapButton('Back to Home');
    await waitForText('USG Abdomen');
    expect(dumpUi()).toContain('USG Abdomen');
  });
});


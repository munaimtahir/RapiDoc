/**
 * 02-home-screen.spec.ts
 *
 * Verifies all interactive elements on the home screen:
 * - Document type buttons
 * - Quick-entry field and Parse button
 * - Settings and Parser Dictionary links
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, waitForText, APP_PACKAGE, adbShell } from '../helpers/android-device';
import { tapButton } from '../helpers/app-actions';

test.describe('Home Screen', () => {
  test.beforeEach(async () => {
    await launchApp();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('home screen shows Quick Entry section', () => {
    expect(dumpUi()).toContain('Quick Entry');
  });

  test('home screen shows Command text field', () => {
    expect(dumpUi()).toContain('Command');
  });

  test('home screen shows Parse button', () => {
    expect(dumpUi()).toContain('Parse');
  });

  test('home screen shows USG Abdomen button', () => {
    expect(dumpUi()).toContain('USG Abdomen');
  });

  test('home screen shows Medical Certificate buttons', () => {
    expect(dumpUi()).toContain('Medical');
  });

  test('home screen shows Settings link', () => {
    expect(dumpUi()).toContain('Settings');
  });

  test('home screen shows Parser Dictionary link', () => {
    expect(dumpUi()).toContain('Parser Dictionary');
  });

  test('tapping USG Abdomen navigates to the USG form', async () => {
    await tapButton('USG Abdomen');
    await waitForText('Patient Name');
    expect(dumpUi()).toContain('Patient Name');
    adbShell('input keyevent KEYCODE_BACK');
    await waitForText('USG Abdomen');
  });

  test('tapping Settings navigates to settings screen', async () => {
    await tapButton('Settings');
    await waitForText('Branding');
    expect(dumpUi()).toContain('Branding');
    adbShell('input keyevent KEYCODE_BACK');
    await waitForText('USG Abdomen');
  });

  test('tapping Parser Dictionary navigates to dictionary screen', async () => {
    await tapButton('Parser Dictionary');
    await waitForText('Parser Dictionary');
    expect(dumpUi()).toContain('Parser Dictionary');
    adbShell('input keyevent KEYCODE_BACK');
    await waitForText('USG Abdomen');
  });
});


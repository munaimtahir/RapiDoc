/**
 * 12-quick-entry.spec.ts
 *
 * Tests the Quick Entry / Chat Parser feature on the home screen:
 * - Command text field accepts input
 * - Parse button triggers parsing
 * - Parsed Summary card appears after parsing
 * - Recognised document type is shown
 * - Unknown command triggers the document picker dialog
 * - Clear button removes the entered command
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, waitForText, APP_PACKAGE, adbShell } from '../helpers/android-device';
import { tapButton, typeInField } from '../helpers/app-actions';

test.describe('Quick Entry / Chat Parser', () => {
  test.beforeEach(async () => {
    await launchApp();
    await waitForText('Quick Entry');
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Command text field is present on home screen', () => {
    expect(dumpUi()).toContain('Command');
  });

  test('Parse button is present on home screen', () => {
    expect(dumpUi()).toContain('Parse');
  });

  test('Entering text into Command field and tapping Parse shows Parsed Summary', async () => {
    await typeInField('Command', 'usg john 35 male');
    await tapButton('Parse');
    await waitForText('Parsed Summary');
    expect(dumpUi()).toContain('Parsed Summary');
  });

  test('Parsed Summary shows detected document type', async () => {
    await typeInField('Command', 'usg john 35 male');
    await tapButton('Parse');
    await waitForText('Detected');
    expect(dumpUi()).toContain('Detected');
  });

  test('Unrecognised command shows document picker dialog', async () => {
    await typeInField('Command', 'xyzxyz unknown gibberish');
    await tapButton('Parse');
    await waitForText('Choose Document');
    expect(dumpUi()).toContain('Choose Document');
  });

  test('Document picker can be cancelled', async () => {
    await typeInField('Command', 'xyzxyz unknown gibberish');
    await tapButton('Parse');
    await waitForText('Cancel');
    await tapButton('Cancel');
    expect(dumpUi()).toContain('Quick Entry');
  });

  test('Clear button appears after entering a command', async () => {
    await typeInField('Command', 'usg test');
    expect(dumpUi()).toContain('Clear');
  });

  test('Clear button removes the entered command text', async () => {
    await typeInField('Command', 'usg test');
    await tapButton('Clear');
    expect(dumpUi()).not.toContain('usg test');
  });
});


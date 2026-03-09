/**
 * 05-gallbladder-section.spec.ts
 *
 * Tests the Gallbladder section on the USG Abdomen form:
 * - Section visible with default NORMAL print mode
 * - ABNORMAL mode exposes the Gallstones toggle
 * - When Gallstones = true, Stone size (mm) text field appears
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, waitForText, APP_PACKAGE, adbShell } from '../helpers/android-device';
import {
  openUsgAbdomenForm,
  setOrganMode,
  scrollToText,
  toggleSwitch,
  typeInField,
} from '../helpers/app-actions';

test.describe('Gallbladder Section', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openUsgAbdomenForm();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Gallbladder section is visible', async () => {
    await scrollToText('Gallbladder');
    expect(dumpUi()).toContain('Gallbladder');
  });

  test('Gallbladder default print mode is NORMAL', async () => {
    await scrollToText('Gallbladder');
    expect(dumpUi()).toContain('NORMAL');
  });

  test('Gallbladder print mode can be set to SKIP', async () => {
    await setOrganMode('Gallbladder', 'SKIP');
    await scrollToText('Gallbladder');
    expect(dumpUi()).toContain('SKIP');
  });

  test('Gallbladder print mode can be set to ABNORMAL', async () => {
    await setOrganMode('Gallbladder', 'ABNORMAL');
    expect(dumpUi()).toContain('ABNORMAL');
  });

  test('Gallstones toggle appears when mode is ABNORMAL', async () => {
    await setOrganMode('Gallbladder', 'ABNORMAL');
    await waitForText('Gallstones');
    expect(dumpUi()).toContain('Gallstones');
  });

  test('Stone size field appears when Gallstones toggle is enabled', async () => {
    await setOrganMode('Gallbladder', 'ABNORMAL');
    await toggleSwitch('Gallstones', true);
    await waitForText('Stone size');
    expect(dumpUi()).toContain('Stone size');
  });

  test('Stone size accepts numeric input', async () => {
    await setOrganMode('Gallbladder', 'ABNORMAL');
    await toggleSwitch('Gallstones', true);
    await waitForText('Stone size');
    await typeInField('Stone size', '8');
    expect(dumpUi()).toContain('8');
  });

  test('Stone size field hides when Gallstones toggle is disabled', async () => {
    await setOrganMode('Gallbladder', 'ABNORMAL');
    await toggleSwitch('Gallstones', true);
    await waitForText('Stone size');
    await toggleSwitch('Gallstones', false);
    expect(dumpUi()).not.toContain('Stone size');
  });
});


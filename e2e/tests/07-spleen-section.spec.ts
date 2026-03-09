/**
 * 07-spleen-section.spec.ts
 *
 * Tests the Spleen organ section on the USG Abdomen form:
 * - Section visible with default NORMAL mode
 * - ABNORMAL mode exposes the Splenomegaly toggle
 * - When Splenomegaly = true, Spleen size (cm) input field appears
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

test.describe('Spleen Section', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openUsgAbdomenForm();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Spleen section is visible on the form', async () => {
    await scrollToText('Spleen');
    expect(dumpUi()).toContain('Spleen');
  });

  test('Spleen default print mode is NORMAL', async () => {
    await scrollToText('Spleen');
    expect(dumpUi()).toContain('NORMAL');
  });

  test('Spleen print mode can be set to SKIP', async () => {
    await setOrganMode('Spleen', 'SKIP');
    await scrollToText('Spleen');
    expect(dumpUi()).toContain('SKIP');
  });

  test('Spleen print mode can be set to ABNORMAL', async () => {
    await setOrganMode('Spleen', 'ABNORMAL');
    expect(dumpUi()).toContain('ABNORMAL');
  });

  test('Splenomegaly toggle appears when mode is ABNORMAL', async () => {
    await setOrganMode('Spleen', 'ABNORMAL');
    await waitForText('Splenomegaly');
    expect(dumpUi()).toContain('Splenomegaly');
  });

  test('Spleen size field appears when Splenomegaly is enabled', async () => {
    await setOrganMode('Spleen', 'ABNORMAL');
    await toggleSwitch('Splenomegaly', true);
    await waitForText('Spleen size');
    expect(dumpUi()).toContain('Spleen size');
  });

  test('Spleen size accepts numeric input', async () => {
    await setOrganMode('Spleen', 'ABNORMAL');
    await toggleSwitch('Splenomegaly', true);
    await waitForText('Spleen size');
    await typeInField('Spleen size', '14');
    expect(dumpUi()).toContain('14');
  });

  test('Spleen size field hides when Splenomegaly is disabled', async () => {
    await setOrganMode('Spleen', 'ABNORMAL');
    await toggleSwitch('Splenomegaly', true);
    await waitForText('Spleen size');
    await toggleSwitch('Splenomegaly', false);
    expect(dumpUi()).not.toContain('Spleen size');
  });
});


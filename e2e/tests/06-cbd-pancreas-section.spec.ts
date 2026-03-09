/**
 * 06-cbd-pancreas-section.spec.ts
 *
 * Tests the CBD and Pancreas sections on the USG Abdomen form.
 * Per SPEC.md both default to SKIP (not printed by default).
 * Tests verify the default state and that modes can be changed.
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, APP_PACKAGE, adbShell } from '../helpers/android-device';
import { openUsgAbdomenForm, setOrganMode, scrollToText } from '../helpers/app-actions';

test.describe('CBD and Pancreas Sections', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openUsgAbdomenForm();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  // -- CBD ----------------------------------------------------------------

  test('CBD section is visible on the form', async () => {
    await scrollToText('CBD');
    expect(dumpUi()).toContain('CBD');
  });

  test('CBD default print mode is SKIP (per SPEC)', async () => {
    await scrollToText('CBD');
    expect(dumpUi()).toContain('SKIP');
  });

  test('CBD print mode can be changed to NORMAL', async () => {
    await setOrganMode('CBD', 'NORMAL');
    await scrollToText('CBD');
    expect(dumpUi()).toContain('NORMAL');
  });

  test('CBD print mode can be changed to ABNORMAL', async () => {
    await setOrganMode('CBD', 'ABNORMAL');
    await scrollToText('CBD');
    expect(dumpUi()).toContain('ABNORMAL');
  });

  // -- Pancreas -----------------------------------------------------------

  test('Pancreas section is visible on the form', async () => {
    await scrollToText('Pancreas');
    expect(dumpUi()).toContain('Pancreas');
  });

  test('Pancreas default print mode is SKIP (per SPEC)', async () => {
    await scrollToText('Pancreas');
    expect(dumpUi()).toContain('SKIP');
  });

  test('Pancreas print mode can be changed to NORMAL', async () => {
    await setOrganMode('Pancreas', 'NORMAL');
    await scrollToText('Pancreas');
    expect(dumpUi()).toContain('NORMAL');
  });

  test('Pancreas print mode can be changed to ABNORMAL', async () => {
    await setOrganMode('Pancreas', 'ABNORMAL');
    await scrollToText('Pancreas');
    expect(dumpUi()).toContain('ABNORMAL');
  });
});


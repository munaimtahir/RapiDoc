/**
 * 04-liver-section.spec.ts
 *
 * Tests the Liver organ section on the USG Abdomen form:
 * - Section card is visible
 * - Print Mode (SKIP / NORMAL / ABNORMAL) can be changed
 * - When ABNORMAL: Fatty Liver Grade, Hepatomegaly, CLD toggle, and
 *   Ascites selector are exposed
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, waitForText, APP_PACKAGE, adbShell } from '../helpers/android-device';
import { openUsgAbdomenForm, setOrganMode, scrollToText, toggleSwitch, selectDropdown } from '../helpers/app-actions';

test.describe('Liver Section', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openUsgAbdomenForm();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Liver section card is visible on the form', async () => {
    await scrollToText('Liver');
    expect(dumpUi()).toContain('Liver');
  });

  test('Liver default print mode is NORMAL', async () => {
    await scrollToText('Liver');
    expect(dumpUi()).toContain('NORMAL');
  });

  test('Liver print mode can be set to SKIP', async () => {
    await setOrganMode('Liver', 'SKIP');
    await scrollToText('Liver');
    expect(dumpUi()).toContain('SKIP');
  });

  test('Liver print mode can be set to ABNORMAL', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await scrollToText('Liver');
    expect(dumpUi()).toContain('ABNORMAL');
  });

  test('Fatty Liver Grade selector appears when mode is ABNORMAL', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await waitForText('Fatty Liver Grade');
    expect(dumpUi()).toContain('Fatty Liver Grade');
  });

  test('Hepatomegaly selector appears when mode is ABNORMAL', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await scrollToText('Hepatomegaly');
    expect(dumpUi()).toContain('Hepatomegaly');
  });

  test('CLD toggle appears when mode is ABNORMAL', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await scrollToText('CLD');
    expect(dumpUi()).toContain('CLD');
  });

  test('Ascites selector appears when mode is ABNORMAL', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await scrollToText('Ascites');
    expect(dumpUi()).toContain('Ascites');
  });

  test('Fatty Grade can be set to Grade 1', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await selectDropdown('Fatty Liver Grade', 'Grade 1');
    expect(dumpUi()).toContain('Grade 1');
  });

  test('Fatty Grade can be set to Grade 2', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await selectDropdown('Fatty Liver Grade', 'Grade 2');
    expect(dumpUi()).toContain('Grade 2');
  });

  test('Fatty Grade can be set to Grade 3', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await selectDropdown('Fatty Liver Grade', 'Grade 3');
    expect(dumpUi()).toContain('Grade 3');
  });

  test('Hepatomegaly can be set to MILD', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await selectDropdown('Hepatomegaly', 'MILD');
    expect(dumpUi()).toContain('MILD');
  });

  test('CLD toggle can be switched on', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await toggleSwitch('CLD', true);
    expect(dumpUi()).toContain('CLD');
  });

  test('Ascites can be set to MILD', async () => {
    await setOrganMode('Liver', 'ABNORMAL');
    await selectDropdown('Ascites', 'MILD');
    expect(dumpUi()).toContain('MILD');
  });
});


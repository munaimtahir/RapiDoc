/**
 * 09-urinary-bladder.spec.ts
 *
 * Tests the Urinary Bladder organ section on the USG Abdomen form:
 * - Section is visible with default NORMAL print mode
 * - Print mode can be changed to SKIP and ABNORMAL
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, APP_PACKAGE, adbShell } from '../helpers/android-device';
import { openUsgAbdomenForm, setOrganMode, scrollToText } from '../helpers/app-actions';

test.describe('Urinary Bladder Section', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openUsgAbdomenForm();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Urinary Bladder section is visible on the form', async () => {
    await scrollToText('Urinary Bladder');
    expect(dumpUi()).toContain('Urinary Bladder');
  });

  test('Urinary Bladder default print mode is NORMAL', async () => {
    await scrollToText('Urinary Bladder');
    expect(dumpUi()).toContain('NORMAL');
  });

  test('Urinary Bladder print mode can be set to SKIP', async () => {
    await setOrganMode('Urinary Bladder', 'SKIP');
    await scrollToText('Urinary Bladder');
    expect(dumpUi()).toContain('SKIP');
  });

  test('Urinary Bladder print mode can be set to ABNORMAL', async () => {
    await setOrganMode('Urinary Bladder', 'ABNORMAL');
    await scrollToText('Urinary Bladder');
    expect(dumpUi()).toContain('ABNORMAL');
  });

  test('Obstruction selector is visible on the form', async () => {
    await scrollToText('Obstruction');
    expect(dumpUi()).toContain('Obstruction');
  });
});


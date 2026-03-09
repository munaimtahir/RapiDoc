/**
 * 08-kidney-sections.spec.ts
 *
 * Tests both Right and Left Kidney organ sections on the USG Abdomen form:
 * - Sections visible with default NORMAL print mode
 * - CMD selector (PRESERVED / REDUCED)
 * - Hydronephrosis severity selector
 * - Renal Stone toggle + stone size + stone location fields
 * - Renal Cyst toggle + cyst size field
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, waitForText, APP_PACKAGE, adbShell } from '../helpers/android-device';
import {
  openUsgAbdomenForm,
  setOrganMode,
  scrollToText,
  toggleSwitch,
  typeInField,
  selectDropdown,
} from '../helpers/app-actions';

test.describe('Right Kidney Section', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openUsgAbdomenForm();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Right Kidney section is visible', async () => {
    await scrollToText('Right Kidney');
    expect(dumpUi()).toContain('Right Kidney');
  });

  test('Right Kidney default print mode is NORMAL', async () => {
    await scrollToText('Right Kidney');
    expect(dumpUi()).toContain('NORMAL');
  });

  test('Right Kidney print mode can be set to ABNORMAL', async () => {
    await setOrganMode('Right Kidney', 'ABNORMAL');
    expect(dumpUi()).toContain('ABNORMAL');
  });

  test('Right CMD selector is visible in ABNORMAL mode', async () => {
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await waitForText('Right CMD');
    expect(dumpUi()).toContain('Right CMD');
  });

  test('Right CMD can be set to REDUCED', async () => {
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await selectDropdown('Right CMD', 'REDUCED');
    expect(dumpUi()).toContain('REDUCED');
  });

  test('Hydronephrosis selector is visible in ABNORMAL mode', async () => {
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await waitForText('Hydronephrosis');
    expect(dumpUi()).toContain('Hydronephrosis');
  });

  test('Hydronephrosis can be set to MILD', async () => {
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await selectDropdown('Hydronephrosis', 'MILD');
    expect(dumpUi()).toContain('MILD');
  });

  test('Right Renal Stone toggle is visible in ABNORMAL mode', async () => {
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await waitForText('Right Renal Stone');
    expect(dumpUi()).toContain('Right Renal Stone');
  });

  test('Stone Size and Location fields appear when Right Renal Stone enabled', async () => {
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await toggleSwitch('Right Renal Stone', true);
    await waitForText('Stone Size');
    const ui = dumpUi();
    expect(ui).toContain('Stone Size');
    expect(ui).toContain('Right stone location');
  });

  test('Renal Cyst Right toggle is visible in ABNORMAL mode', async () => {
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await waitForText('Renal Cyst Right');
    expect(dumpUi()).toContain('Renal Cyst Right');
  });

  test('Cyst size field appears when Renal Cyst Right is enabled', async () => {
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await toggleSwitch('Renal Cyst Right', true);
    await waitForText('Cyst Size');
    expect(dumpUi()).toContain('Cyst Size');
  });
});

test.describe('Left Kidney Section', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openUsgAbdomenForm();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Left Kidney section is visible', async () => {
    await scrollToText('Left Kidney');
    expect(dumpUi()).toContain('Left Kidney');
  });

  test('Left Kidney default print mode is NORMAL', async () => {
    await scrollToText('Left Kidney');
    expect(dumpUi()).toContain('NORMAL');
  });

  test('Left Kidney print mode can be set to ABNORMAL', async () => {
    await setOrganMode('Left Kidney', 'ABNORMAL');
    expect(dumpUi()).toContain('ABNORMAL');
  });

  test('Left CMD selector is visible in ABNORMAL mode', async () => {
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await waitForText('Left CMD');
    expect(dumpUi()).toContain('Left CMD');
  });

  test('Left CMD can be set to REDUCED', async () => {
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await selectDropdown('Left CMD', 'REDUCED');
    expect(dumpUi()).toContain('REDUCED');
  });

  test('Left Renal Stone toggle is visible in ABNORMAL mode', async () => {
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await waitForText('Left Renal Stone');
    expect(dumpUi()).toContain('Left Renal Stone');
  });

  test('Stone Size and Location fields appear when Left Renal Stone enabled', async () => {
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await toggleSwitch('Left Renal Stone', true);
    await waitForText('Stone Size');
    const ui = dumpUi();
    expect(ui).toContain('Stone Size');
    expect(ui).toContain('Left stone location');
  });

  test('Stone location can be set to Renal pelvis', async () => {
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await toggleSwitch('Left Renal Stone', true);
    await typeInField('Stone Size', '6');
    await selectDropdown('Left stone location', 'Renal pelvis');
    expect(dumpUi()).toContain('Renal pelvis');
  });

  test('Renal Cyst Left toggle is visible in ABNORMAL mode', async () => {
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await waitForText('Renal Cyst Left');
    expect(dumpUi()).toContain('Renal Cyst Left');
  });

  test('Cyst size field appears when Renal Cyst Left is enabled', async () => {
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await toggleSwitch('Renal Cyst Left', true);
    await waitForText('Cyst Size');
    expect(dumpUi()).toContain('Cyst Size');
  });
});


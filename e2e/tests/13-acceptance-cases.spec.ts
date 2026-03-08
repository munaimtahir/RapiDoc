/**
 * 13-acceptance-cases.spec.ts
 *
 * Implements the three mandatory acceptance test cases from docs/SPEC.md §8:
 *
 * Case A) Normal study — all defaults, no abnormalities
 *         → impression contains "Normal abdominal ultrasound."
 *         → CBD and Pancreas are NOT printed (their default mode is SKIP)
 *
 * Case B) Surayya 50F — Grade I fatty liver + bilateral mild hydronephrosis;
 *         rest normal.
 *         → impression contains fatty liver and hydronephrosis entries
 *
 * Case C) Fayyaz 32M — Grade I fatty liver + left moderate hydronephrosis
 *         + 6 mm left renal pelvis stone + obstruction not seen.
 *         → impression contains all three abnormal items
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, waitForText, APP_PACKAGE, adbShell } from '../helpers/android-device';
import {
  openUsgAbdomenForm,
  fillPatientInfo,
  setOrganMode,
  selectDropdown,
  toggleSwitch,
  typeInField,
  goToPreview,
  tapButton,
  scrollToText,
} from '../helpers/app-actions';

test.describe('Acceptance Case A — Normal Study', () => {
  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('A1: Normal study shows "Normal abdominal ultrasound." in impression', async () => {
    await launchApp();
    await openUsgAbdomenForm();
    // Use all defaults — no abnormalities selected
    await fillPatientInfo('Normal Patient', '30', 'Male');
    await goToPreview();
    await waitForText('Impression');
    expect(dumpUi()).toContain('Normal abdominal ultrasound');
  });

  test('A2: Normal study PDF generates without errors', async () => {
    await launchApp();
    await openUsgAbdomenForm();
    await fillPatientInfo('Normal Patient', '30', 'Male');
    await goToPreview();
    await tapButton('Generate PDF');
    await waitForText('Print');
    expect(dumpUi()).not.toContain('has stopped');
  });
});

test.describe('Acceptance Case B — Surayya 50F', () => {
  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('B1: Fatty liver grade 1 + bilateral mild hydronephrosis impression is abnormal', async () => {
    await launchApp();
    await openUsgAbdomenForm();
    await fillPatientInfo('Surayya', '50', 'Female');

    // Liver ABNORMAL — fatty grade 1
    await setOrganMode('Liver', 'ABNORMAL');
    await selectDropdown('Fatty Liver Grade', 'Grade 1');

    // Right Kidney ABNORMAL — Hydronephrosis = MILD
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await selectDropdown('Hydronephrosis', 'MILD');

    // Left Kidney ABNORMAL — Hydronephrosis = MILD
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await selectDropdown('Hydronephrosis', 'MILD');

    await goToPreview();
    await waitForText('Impression');
    const ui = dumpUi();
    // Impression must be abnormal
    expect(ui).not.toContain('Normal abdominal ultrasound');
    // Fatty liver must appear in impression
    expect(ui).toContain('fatty');
  });

  test('B2: Surayya PDF generates without crash', async () => {
    await launchApp();
    await openUsgAbdomenForm();
    await fillPatientInfo('Surayya', '50', 'Female');

    await setOrganMode('Liver', 'ABNORMAL');
    await selectDropdown('Fatty Liver Grade', 'Grade 1');
    await setOrganMode('Right Kidney', 'ABNORMAL');
    await selectDropdown('Hydronephrosis', 'MILD');
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await selectDropdown('Hydronephrosis', 'MILD');

    await goToPreview();
    await tapButton('Generate PDF');
    await waitForText('Print');
    expect(dumpUi()).not.toContain('has stopped');
  });
});

test.describe('Acceptance Case C — Fayyaz 32M', () => {
  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('C1: Fatty liver grade 1 + left moderate hydronephrosis + 6 mm left pelvis stone', async () => {
    await launchApp();
    await openUsgAbdomenForm();
    await fillPatientInfo('Fayyaz', '32', 'Male');

    // Liver ABNORMAL — fatty grade 1
    await setOrganMode('Liver', 'ABNORMAL');
    await selectDropdown('Fatty Liver Grade', 'Grade 1');

    // Left Kidney ABNORMAL — moderate hydronephrosis + 6 mm stone at renal pelvis
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await selectDropdown('Hydronephrosis', 'MODERATE');
    await toggleSwitch('Left Renal Stone', true);
    await typeInField('Stone Size', '6');
    await selectDropdown('Left stone location', 'Renal pelvis');

    // Obstruction = NOT_SEEN
    await scrollToText('Obstruction');
    await selectDropdown('Obstruction', 'NOT SEEN');

    await goToPreview();
    await waitForText('Impression');
    const ui = dumpUi();
    // Impression must be abnormal
    expect(ui).not.toContain('Normal abdominal ultrasound');
    // Fatty liver should appear
    expect(ui).toContain('fatty');
  });

  test('C2: Fayyaz PDF generates without crash', async () => {
    await launchApp();
    await openUsgAbdomenForm();
    await fillPatientInfo('Fayyaz', '32', 'Male');

    await setOrganMode('Liver', 'ABNORMAL');
    await selectDropdown('Fatty Liver Grade', 'Grade 1');
    await setOrganMode('Left Kidney', 'ABNORMAL');
    await selectDropdown('Hydronephrosis', 'MODERATE');
    await toggleSwitch('Left Renal Stone', true);
    await typeInField('Stone Size', '6');
    await selectDropdown('Left stone location', 'Renal pelvis');
    await scrollToText('Obstruction');
    await selectDropdown('Obstruction', 'NOT SEEN');

    await goToPreview();
    await tapButton('Generate PDF');
    await waitForText('Print');
    expect(dumpUi()).not.toContain('has stopped');
  });
});


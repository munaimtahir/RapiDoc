/**
 * 10-pdf-generation.spec.ts
 *
 * Tests the full PDF-generation flow:
 * - Navigating to the Preview screen
 * - Impression text is displayed (Normal / Abnormal)
 * - "Generate PDF" button exists and can be tapped
 * - PDF Preview Box is shown after generation
 * - Print and Share buttons are present
 * - New Report button returns to home screen
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, waitForText, APP_PACKAGE, adbShell } from '../helpers/android-device';
import {
  openUsgAbdomenForm,
  fillPatientInfo,
  goToPreview,
  generatePdf,
  tapButton,
} from '../helpers/app-actions';

test.describe('PDF Generation Flow', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openUsgAbdomenForm();
    await fillPatientInfo('Test Patient', '40', 'Male');
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Generate PDF Report button is present on the form', () => {
    expect(dumpUi()).toContain('Generate PDF Report');
  });

  test('Preview screen is reached after tapping Generate PDF Report', async () => {
    await goToPreview();
    expect(dumpUi()).toContain('Preview');
  });

  test('Patient name is shown on the Preview screen', async () => {
    await goToPreview();
    expect(dumpUi()).toContain('Test Patient');
  });

  test('Impression section is displayed on Preview screen', async () => {
    await goToPreview();
    await waitForText('Impression');
    expect(dumpUi()).toContain('Impression');
  });

  test('Generate PDF button exists on Preview screen', async () => {
    await goToPreview();
    await waitForText('Generate PDF');
    expect(dumpUi()).toContain('Generate PDF');
  });

  test('Generating PDF does not crash the app', async () => {
    await goToPreview();
    await generatePdf();
    const ui = dumpUi();
    expect(ui).not.toContain('has stopped');
    expect(ui).not.toContain('Application Error');
  });

  test('Print button is present on Preview screen', async () => {
    await goToPreview();
    await waitForText('Print');
    expect(dumpUi()).toContain('Print');
  });

  test('Share button is present on Preview screen', async () => {
    await goToPreview();
    await waitForText('Share');
    expect(dumpUi()).toContain('Share');
  });

  test('New Report button is present on Preview screen', async () => {
    await goToPreview();
    await waitForText('New Report');
    expect(dumpUi()).toContain('New Report');
  });

  test('Tapping New Report returns to the home screen', async () => {
    await goToPreview();
    await tapButton('New Report');
    await waitForText('USG Abdomen');
    expect(dumpUi()).toContain('USG Abdomen');
  });
});


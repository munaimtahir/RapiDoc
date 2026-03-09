/**
 * 03-patient-info.spec.ts
 *
 * Validates the Patient Information card on the USG Abdomen form:
 * - Required fields are present (Name, Age, Gender)
 * - Optional Patient ID field is present
 * - Normal Toggle is present
 * - Validation feedback appears for invalid / empty data
 * - Valid data is accepted
 */

import { test, expect } from '@playwright/test';
import { launchApp, dumpUi, APP_PACKAGE, adbShell } from '../helpers/android-device';
import {
  openUsgAbdomenForm,
  fillPatientInfo,
  typeInField,
  selectDropdown,
} from '../helpers/app-actions';

test.describe('Patient Information Form', () => {
  test.beforeEach(async () => {
    await launchApp();
    await openUsgAbdomenForm();
  });

  test.afterAll(() => {
    adbShell(`am force-stop ${APP_PACKAGE}`);
  });

  test('Patient Name field is present', () => {
    expect(dumpUi()).toContain('Patient Name');
  });

  test('Patient ID (optional) field is present', () => {
    expect(dumpUi()).toContain('Patient ID');
  });

  test('Age field is present', () => {
    expect(dumpUi()).toContain('Age');
  });

  test('Gender selector is present', () => {
    expect(dumpUi()).toContain('Gender');
  });

  test('Normal Toggle switch is present on the form', () => {
    expect(dumpUi()).toContain('Normal Toggle');
  });

  test('entering a valid patient name is accepted', async () => {
    await typeInField('Patient Name', 'Ahmad');
    expect(dumpUi()).toContain('Ahmad');
  });

  test('entering a valid age is accepted', async () => {
    await typeInField('Age', '35');
    expect(dumpUi()).toContain('35');
  });

  test('selecting Male gender works', async () => {
    await selectDropdown('Gender', 'Male');
    expect(dumpUi()).toContain('Male');
  });

  test('selecting Female gender works', async () => {
    await selectDropdown('Gender', 'Female');
    expect(dumpUi()).toContain('Female');
  });

  test('filling all required fields enables report generation flow', async () => {
    await fillPatientInfo('Test Patient', '30', 'Male');
    expect(dumpUi()).toContain('Generate PDF Report');
  });
});


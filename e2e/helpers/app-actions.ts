/**
 * app-actions.ts
 *
 * High-level reusable actions for RapiDoc's UI navigation and form
 * interactions.  All actions drive the device exclusively through
 * ADB shell commands (via the helpers in android-device.ts).
 *
 * No Playwright browser / page object is used here; `@playwright/test`
 * acts purely as the test runner and assertion library.
 */

import { adbShell, dumpUi, sleep, waitForText } from './android-device';

// ---------------------------------------------------------------------------
// Navigation helpers
// ---------------------------------------------------------------------------

/**
 * Tap the on-screen button whose text matches the given label.
 * Parses the UI dump to find the element bounds and taps the centre.
 */
export async function tapButton(label: string): Promise<void> {
  const bounds = findElementBounds(dumpUi(), label);
  const [cx, cy] = boundsCenter(bounds);
  adbShell(`input tap ${cx} ${cy}`);
  await sleep(400);
}

/**
 * Open the USG Abdomen form from the home screen.
 */
export async function openUsgAbdomenForm(): Promise<void> {
  await waitForText('USG Abdomen');
  await tapButton('USG Abdomen');
  await waitForText('Patient Name');
}

/**
 * Open the Settings screen from the home screen.
 */
export async function openSettings(): Promise<void> {
  await waitForText('Settings');
  await tapButton('Settings');
  await waitForText('Branding');
}

/**
 * Open the Parser Dictionary screen from the home screen.
 */
export async function openParserDictionary(): Promise<void> {
  await waitForText('Parser Dictionary');
  await tapButton('Parser Dictionary');
  await waitForText('Parser Dictionary');
}

// ---------------------------------------------------------------------------
// Patient info helpers
// ---------------------------------------------------------------------------

/**
 * Fill in the Patient Information fields on the USG form screen.
 */
export async function fillPatientInfo(
  name: string,
  age: string,
  sex: 'Male' | 'Female'
): Promise<void> {
  await typeInField('Patient Name', name);
  await typeInField('Age', age);
  await selectDropdown('Gender', sex);
}

// ---------------------------------------------------------------------------
// Organ-section helpers
// ---------------------------------------------------------------------------

/**
 * Change the Print Mode of an organ section card.
 */
export async function setOrganMode(
  organTitle: string,
  mode: 'SKIP' | 'NORMAL' | 'ABNORMAL'
): Promise<void> {
  await scrollToText(organTitle);
  await sleep(300);
  await selectDropdown('Print Mode', mode, organTitle);
}

/**
 * Toggle a named Switch widget on/off.
 */
export async function toggleSwitch(switchLabel: string, targetState: boolean): Promise<void> {
  const ui = dumpUi();
  const currentlyOn = isSwitchChecked(ui, switchLabel);
  if (currentlyOn !== targetState) {
    const bounds = findElementBounds(ui, switchLabel);
    const [cx, cy] = boundsCenter(bounds);
    adbShell(`input tap ${cx} ${cy}`);
    await sleep(400);
  }
}

/**
 * Type text into a labelled OutlinedTextField.
 * Taps to focus, selects all existing content, then types the new text.
 */
export async function typeInField(fieldLabel: string, text: string): Promise<void> {
  await scrollToText(fieldLabel);
  const bounds = findElementBounds(dumpUi(), fieldLabel);
  const [cx, cy] = boundsCenter(bounds);
  adbShell(`input tap ${cx} ${cy}`);
  await sleep(300);
  // Select-all + type
  adbShell('input keyevent --longpress KEYCODE_A');
  adbShell(`input text "${text.replace(/ /g, '%s')}"`);
  await sleep(300);
}

/**
 * Choose an option from a dropdown (EnumSelector).
 *
 * @param dropdownLabel  Text/label on the dropdown button
 * @param optionText     Text of the option to select
 * @param contextText    Optional scroll-to anchor (defaults to dropdownLabel)
 */
export async function selectDropdown(
  dropdownLabel: string,
  optionText: string,
  contextText?: string
): Promise<void> {
  await scrollToText(contextText ?? dropdownLabel);
  const ui = dumpUi();
  const bounds = findElementBounds(ui, dropdownLabel);
  const [cx, cy] = boundsCenter(bounds);
  adbShell(`input tap ${cx} ${cy}`);
  await sleep(600);
  // Option text now visible in the dropdown menu
  const menuUi = dumpUi();
  const optBounds = findElementBounds(menuUi, optionText);
  const [ox, oy] = boundsCenter(optBounds);
  adbShell(`input tap ${ox} ${oy}`);
  await sleep(400);
}

// ---------------------------------------------------------------------------
// Preview & PDF helpers
// ---------------------------------------------------------------------------

/**
 * Press the "Generate PDF Report" button to navigate to the Preview screen.
 */
export async function goToPreview(): Promise<void> {
  await scrollToText('Generate PDF Report');
  await tapButton('Generate PDF Report');
  await waitForText('Preview');
}

/**
 * Press "Generate PDF" on the Preview screen and wait for generation.
 */
export async function generatePdf(): Promise<void> {
  await tapButton('Generate PDF');
  await sleep(3_500);
}

// ---------------------------------------------------------------------------
// Scroll helper
// ---------------------------------------------------------------------------

/**
 * Scroll down until the given text appears in the UI dump.
 */
export async function scrollToText(text: string): Promise<void> {
  const MAX_SCROLLS = 14;
  for (let i = 0; i < MAX_SCROLLS; i++) {
    if (dumpUi().includes(text)) return;
    // Swipe upward (scroll down) for a typical portrait phone
    adbShell('input swipe 540 1400 540 600 300');
    await sleep(400);
  }
  if (!dumpUi().includes(text)) {
    throw new Error(`Could not scroll to text "${text}" after ${MAX_SCROLLS} attempts`);
  }
}

// ---------------------------------------------------------------------------
// Low-level UI-dump utilities
// ---------------------------------------------------------------------------

/**
 * Find the bounds attribute for the first node in the UI dump whose
 * `text` or `content-desc` attribute contains the given string.
 */
export function findElementBounds(uiDump: string, text: string): string {
  const patterns = [
    new RegExp(`text="${escapeRegex(text)}"[^/]*bounds="([^"]+)"`, 'i'),
    new RegExp(`content-desc="${escapeRegex(text)}"[^/]*bounds="([^"]+)"`, 'i'),
    new RegExp(`text="[^"]*${escapeRegex(text)}[^"]*"[^/]*bounds="([^"]+)"`, 'i'),
    new RegExp(`content-desc="[^"]*${escapeRegex(text)}[^"]*"[^/]*bounds="([^"]+)"`, 'i'),
  ];
  for (const re of patterns) {
    const m = uiDump.match(re);
    if (m) return m[1];
  }
  throw new Error(
    `Element with text/content-desc containing "${text}" not found in UI dump.\n` +
    `Dump excerpt:\n${uiDump.slice(0, 2000)}`
  );
}

/** Convert "[left,top][right,bottom]" bounds to centre [cx, cy]. */
function boundsCenter(bounds: string): [number, number] {
  const m = bounds.match(/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/);
  if (!m) throw new Error(`Cannot parse bounds: "${bounds}"`);
  const cx = Math.round((parseInt(m[1], 10) + parseInt(m[3], 10)) / 2);
  const cy = Math.round((parseInt(m[2], 10) + parseInt(m[4], 10)) / 2);
  return [cx, cy];
}

/** Detect whether the Switch immediately following the given label is checked. */
function isSwitchChecked(uiDump: string, label: string): boolean {
  const idx = uiDump.indexOf(`text="${label}"`);
  if (idx === -1) return false;
  const after = uiDump.slice(idx, idx + 800);
  const m = after.match(/checked="(true|false)"/);
  return m ? m[1] === 'true' : false;
}

function escapeRegex(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}


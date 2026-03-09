/**
 * android-device.ts
 *
 * Shared helper to drive the RapiDoc Android app via ADB.
 *
 * Tests use `@playwright/test` as the test **runner** (test, expect, reporter).
 * App interaction is performed through standard ADB shell commands via
 * Node's `child_process.execSync`, which works with any connected
 * emulator or physical device.
 *
 * Pre-requisites:
 *   - `adb` must be on PATH.
 *   - Exactly one device/emulator must be connected (`adb devices`).
 *   - The app APK must already be installed on the device.
 */

import { execSync } from 'child_process';

export const APP_PACKAGE = 'com.alshifa.rapidocusg';
export const MAIN_ACTIVITY = `${APP_PACKAGE}/.MainActivity`;

// ---------------------------------------------------------------------------
// Core ADB helpers
// ---------------------------------------------------------------------------

/**
 * Run an ADB shell command and return its stdout as a string.
 */
export function adbShell(cmd: string): string {
  try {
    return execSync(`adb shell ${cmd}`, { encoding: 'utf8', timeout: 30_000 });
  } catch (err: unknown) {
    const e = err as { stdout?: string; message?: string };
    return e.stdout ?? e.message ?? '';
  }
}

/**
 * Run a top-level ADB command (not `adb shell`).
 */
export function adb(cmd: string): string {
  try {
    return execSync(`adb ${cmd}`, { encoding: 'utf8', timeout: 30_000 });
  } catch (err: unknown) {
    const e = err as { stdout?: string; message?: string };
    return e.stdout ?? e.message ?? '';
  }
}

/** Promise-based sleep. */
export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

// ---------------------------------------------------------------------------
// App lifecycle
// ---------------------------------------------------------------------------

/**
 * Force-stop the app and relaunch it fresh, then wait for it to settle.
 */
export async function launchApp(): Promise<void> {
  adbShell(`am force-stop ${APP_PACKAGE}`);
  await sleep(500);
  adbShell(`am start -n ${MAIN_ACTIVITY}`);
  await sleep(2_000);
}

/**
 * Dump the current on-screen UI accessibility hierarchy as XML text.
 * Uses UIAutomator to generate the dump on the device, then pulls it.
 */
export function dumpUi(): string {
  adbShell('uiautomator dump /sdcard/window_dump.xml 2>/dev/null || true');
  return adbShell('cat /sdcard/window_dump.xml 2>/dev/null || echo ""');
}

/**
 * Poll until the given text appears in the UI dump or the timeout expires.
 */
export async function waitForText(text: string, timeoutMs = 15_000): Promise<void> {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (dumpUi().includes(text)) return;
    await sleep(500);
  }
  throw new Error(
    `waitForText: "${text}" did not appear within ${timeoutMs} ms.\n` +
    `UI dump:\n${dumpUi().slice(0, 1500)}`
  );
}

/**
 * Assert that the given text is currently visible in the UI hierarchy.
 * Throws with a descriptive message if not found.
 */
export function assertTextVisible(text: string): void {
  const ui = dumpUi();
  if (!ui.includes(text)) {
    throw new Error(
      `assertTextVisible: "${text}" not found in current UI.\n` +
      `UI dump (first 1500 chars):\n${ui.slice(0, 1500)}`
    );
  }
}


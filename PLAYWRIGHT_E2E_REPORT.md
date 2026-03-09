# Playwright E2E Test Suite — RapiDoc Android App

**Date:** 2026-03-08  
**Repository:** `munaimtahir/RapiDoc`  
**App Package:** `com.alshifa.rapidocusg`

---

## 1. Playwright Server Status

| Item | Status |
|------|--------|
| Playwright server previously configured | ❌ Not set up |
| Playwright infrastructure added by this PR | ✅ Set up |
| Playwright version | `@playwright/test ^1.50.1` |
| Test runner | `@playwright/test` (Node.js) |
| Device automation | Playwright Android API via ADB |
| Location | `e2e/` directory |

---

## 2. How Playwright Is Used For This Android App

RapiDoc is a **native Android app** built with Kotlin + Jetpack Compose.  
Playwright's **Android device API** (`@playwright/test`) communicates with the
running emulator/device over ADB.  Tests use:

- `android.devices()` — obtain the first connected device.
- `device.shell(cmd)` — run arbitrary ADB shell commands (UI dump, am start, input events).
- `uiautomator dump` — capture a serialised XML snapshot of the on-screen accessibility
  tree for assertion.
- `input tap / swipe / text / keyevent` — simulate user gestures.

This approach lets `@playwright/test` act as the test **runner and reporter**
while ADB drives the native Compose UI.

---

## 3. Files Added

```
e2e/
├── package.json                 — Node.js project (devDep: @playwright/test, typescript)
├── tsconfig.json                — TypeScript compiler config
├── playwright.config.ts         — Playwright config (timeout, reporter, projects)
├── helpers/
│   ├── android-device.ts        — Device connect/launch, waitForText, dumpUi helpers
│   └── app-actions.ts           — High-level UI actions (tapButton, fillPatientInfo, etc.)
└── tests/
    ├── 01-app-launch.spec.ts    — App launches without crash / ANR
    ├── 02-home-screen.spec.ts   — Home screen elements and navigation buttons
    ├── 03-patient-info.spec.ts  — Patient information form fields and validation
    ├── 04-liver-section.spec.ts — Liver organ section (modes, fatty grade, CLD, ascites)
    ├── 05-gallbladder-section.spec.ts — Gallbladder (stones toggle, stone size)
    ├── 06-cbd-pancreas-section.spec.ts — CBD & Pancreas (default SKIP per SPEC)
    ├── 07-spleen-section.spec.ts — Spleen (splenomegaly, size field)
    ├── 08-kidney-sections.spec.ts — Right & Left Kidney (CMD, hydronephrosis, stone, cyst)
    ├── 09-urinary-bladder.spec.ts — Urinary Bladder section + Obstruction selector
    ├── 10-pdf-generation.spec.ts — Preview screen, Generate PDF, Print, Share, New Report
    ├── 11-settings-screen.spec.ts — Settings: branding, appearance, factory reset
    ├── 12-quick-entry.spec.ts   — Quick Entry parser (parse, clear, doc picker)
    └── 13-acceptance-cases.spec.ts — SPEC §8 acceptance tests A, B, C

.github/workflows/
└── e2e-playwright.yaml          — Manual-trigger-only GitHub Actions workflow
```

---

## 4. Test Coverage Summary

| Spec File | Area Covered | Test Count |
|-----------|-------------|-----------|
| 01-app-launch | Launch, no crash, no ANR | 3 |
| 02-home-screen | Home screen elements, all navigation buttons | 9 |
| 03-patient-info | Name / Age / Gender fields, validation flow | 10 |
| 04-liver-section | Print modes, fatty grade, hepatomegaly, CLD, ascites | 13 |
| 05-gallbladder-section | Print modes, stones toggle, stone size | 8 |
| 06-cbd-pancreas-section | Default SKIP mode, NORMAL/ABNORMAL changes | 8 |
| 07-spleen-section | Print modes, splenomegaly, size field | 8 |
| 08-kidney-sections | Right + Left kidney: CMD, hydronephrosis, stone, cyst | 18 |
| 09-urinary-bladder | Print modes, obstruction selector | 5 |
| 10-pdf-generation | Preview screen, generate, print, share, new report | 9 |
| 11-settings-screen | Branding, appearance, factory reset dialog | 11 |
| 12-quick-entry | Parse command, doc picker, clear button | 8 |
| 13-acceptance-cases | SPEC §8 cases A, B, C | 6 |
| **Total** | | **~116** |

---

## 5. Acceptance Tests (SPEC §8)

All three mandatory acceptance cases from `docs/SPEC.md §8` are covered in
`e2e/tests/13-acceptance-cases.spec.ts`:

### Case A — Normal Study
- Input: all defaults, no abnormalities.
- Expected impression: `"Normal abdominal ultrasound."` (green).
- Test A1 asserts impression contains this text.
- Test A2 asserts PDF generation completes without crash.

### Case B — Surayya 50F
- Grade I fatty liver + bilateral mild hydronephrosis; rest normal.
- Tests B1–B2 assert impression is abnormal and PDF generates cleanly.

### Case C — Fayyaz 32M
- Grade I fatty liver + left moderate hydronephrosis + 6 mm left renal pelvis stone + obstruction not seen.
- Tests C1–C2 assert impression is abnormal and PDF generates cleanly.

---

## 6. GitHub Actions Workflow

File: `.github/workflows/e2e-playwright.yaml`

### Trigger
```yaml
on:
  workflow_dispatch:   # MANUAL ONLY — no push / pull_request / schedule triggers
```

### Workflow Steps
1. Checkout code
2. Set up JDK 21 (Temurin)
3. Gradle cache
4. Android SDK + emulator system image
5. Build Debug APK (`./gradlew :app:assembleDebug`)
6. Set up Node.js 20 + install Playwright
7. Enable KVM for hardware acceleration
8. Create AVD (default: Pixel 6 / API 33 / google_apis / x86_64)
9. Start emulator (headless, swift-shader GPU)
10. Wait for emulator boot + dismiss lock screen
11. Install APK via `adb install`
12. Run `npx playwright test`
13. Upload artefacts: HTML report, test-results, emulator log (on failure)

### Configurable Inputs
| Input | Default | Description |
|-------|---------|-------------|
| `avd_api_level` | `33` | Android API level |
| `emulator_target` | `google_apis` | Emulator image variant |

---

## 7. Running Tests Locally

### Pre-requisites
- Node.js ≥ 20
- Android SDK with ADB on PATH
- Android emulator **running** (or physical device connected):
  ```
  adb devices   # should list at least one device
  ```
- App APK installed on the device:
  ```
  adb install app/build/outputs/apk/debug/app-debug.apk
  ```

### Install and run
```bash
cd e2e
npm install
npx playwright install --with-deps chromium
npx playwright test
```

### View report
```bash
npx playwright show-report playwright-report
```

---

## 8. Architecture Notes

- Tests run **serially** (`workers: 1`) because there is a single Android device.
- Each test file calls `launchApp()` in `beforeEach` to start from a clean state,
  preventing state leakage between tests.
- `retries: 1` handles transient emulator flakiness common in CI.
- Screenshots and video are captured **only on failure** to keep artefact sizes small.
- Timeouts: 120 s per test, 30 s per action.

---

*End of report.*

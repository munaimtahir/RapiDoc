# Phase 1 Smoke Test

## Build Status
- `assembleDebug` expected to pass without regression.
- Unit Tests expected to pass.

## Manual Sanity Checks
1. [ ] Launch Application
2. [ ] Leave Patient Name empty on USG Abdomen -> Verify "Generate PDF" is disabled.
3. [ ] Input valid data on USG Abdomen -> Generate PDF -> Verify Layout is clean with no missing demographics fields and no empty Patient ID space.
4. [ ] Click "Reset" button -> Verify fields are cleared but "Normal Mode" does not override unintended state elements.
5. [ ] Input `sick 5 days for fever` in Quick Entry -> Verify it parses correctly and maps to Leave Certificate without duplicating words into `durationDays`.
6. [ ] Generate Medical Fitness Certificate -> Verify layout renders neatly with "Restrictions" and "Remarks" spaced properly.

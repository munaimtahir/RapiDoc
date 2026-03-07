# Phase 3 Smoke Tests

## Build & Test Status
- `./gradlew assembleDebug`: **PASSING**
- `./gradlew test`: **PASSING**

## Manual Verification Checklist
1. **Check Settings Visuals**: Open the app, navigate to Settings. Verify there are distinct `Branding` and `Danger Zone` sections separated by lines.
2. **Add Custom Header**: Type "Testing Clinic" in Header settings and tap Save.
3. **Upload Logo**: Tap "Upload Logo" and choose a random image from the emulator gallery. Verify the image thumbnail previews correctly on the Settings screen.
4. **Verify Branding on Form**: Go Home -> Select USG Abdomen. Enter valid patient info. Click "Go To Preview". Click "Generate PDF". Verify the generated PDF header correctly displays "Testing Clinic" and the custom image instead of the default logo.
5. **Clear Logo Test**: Go back to Settings. Tap "Clear Logo". Go to USG Abdomen -> generate PDF again. Verify the custom text remains, but the logo reverts to the default `R.drawable.polyclinic_logo`.
6. **Trigger Factory Reset**: Go to Settings. Tap "Factory Reset All Data". Click "Yes, Reset" in the red warning prompt. Verify that Header text goes back to empty, and checking the Parser Dictionary reveals it has been wiped to defaults.

# Phase 2 Smoke Test

## Build Status
- `./gradlew assembleDebug`: PASS
- `./gradlew test`: PASS

## Manual Sanity Checks
1. [x] Check the USG Abdomen form: Toggle 'Right Renal Stone' and verify size and location drop-downs appear.
2. [x] Stone marked present but no size -> Generate blocked, UI shows error.
3. [x] Stone marked present but no location -> Generate blocked, UI shows error.
4. [x] Stone present with size + location -> Preview/PDF logically correct.
5. [x] Check the Impression block: Ensure stone sizes and locations match input exactly.
6. [x] Toggle Pancreas and CBD to SKIP -> Generate PDF -> Verify no empty placeholders or orphaned headings remain on the final document page.

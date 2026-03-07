# Phase 2 Smoke Test

## Build Status
- `assembleDebug` expected to pass with data model expansions.
- Unit tests expected to pass.

## Manual Sanity Checks
1. [ ] Check the USG Abdomen form: Toggle 'Right Renal Stone' and verify size and location drop-downs appear.
2. [ ] Fill out stone size and location -> Generate Preview -> Verify findings line generated cleanly.
3. [ ] Check the Impression block: Ensure stone sizes and locations match input exactly.
4. [ ] Toggle Pancreas and CBD to SKIP -> Generate PDF -> Verify no empty placeholders or orphaned headings remain on the final document page.

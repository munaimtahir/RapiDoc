# Smoke Plan (Prepared; pending SDK availability)

## Build gate
1. `./gradlew clean`
2. `./gradlew assembleDebug`

## Runtime smoke (after install)
1. Launch app, verify Home route loads.
2. Open USG Abdomen, enter required patient fields, generate PDF, preview, print/share.
3. Verify impression color for normal vs abnormal cases.
4. Open Medical Certificate, fill minimal fields, generate and print/share.
5. Open Prescription, add at least 2 medicine rows manually, generate and print/share.
6. Open Lab Request Slip, select checklist + other text, generate and print/share.
7. Open Radiology Request Slip, fill modality/study/notes, generate and print/share.
8. Open Settings:
   - change header text, verify in generated document
   - upload logo and verify
   - factory reset and verify defaults restored
9. Toggle FREE/PRO test switch and verify locked docs + upsell behavior.
10. Force airplane mode and re-run one generation/print path to confirm offline behavior.

# Smoke Plan (Stage 2)

## Build checks
1. `./gradlew clean --no-daemon --console=plain`
2. `./gradlew assembleDebug --no-daemon --console=plain`

## Navigation & gating
1. Launch app -> verify Home route appears.
2. In FREE tier:
   - USG Abdomen + Medical Certificate open.
   - Prescription/Lab/Radiology show PRO label and redirect to upsell.
3. Open Settings -> toggle PRO -> return Home -> all docs become accessible.

## USG flow
1. Open USG form route.
2. Enter Name/Age/Gender and select findings.
3. Open preview, generate PDF, print/share.
4. Verify impression coloring (normal green, abnormal red).

## Starter pack docs
1. Medical Certificate: fill fields -> generate PDF -> print/share.
2. Prescription: add medicine line manually (no auto-dose) -> generate PDF -> print/share.
3. Lab Request Slip: select text fields -> generate PDF -> print/share.
4. Radiology Request Slip: set modality/study/notes/urgency -> generate PDF -> print/share.

## Settings personalization
1. Update header text -> generate any non-USG doc and confirm updated header.
2. Upload logo -> generate doc and confirm logo usage.
3. Factory reset -> verify header/logo reset and plan resets to FREE.

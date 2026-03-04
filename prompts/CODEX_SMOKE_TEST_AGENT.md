You are a verification agent. Build and smoke-test the Android project for RapiDoc USG (MVP).

## Build
Run:
- `./gradlew :app:assembleDebug`

## Manual smoke (emulator/device)
Using the in-app “Load Sample Case” buttons, generate PDFs for:
1) Normal
2) Surayya 50F (FL1 + bilateral mild hydronephrosis)
3) Fayyaz 32M (FL1 + left moderate hydronephrosis + 6mm stone + obstruction not seen)

Verify:
- Logo appears in header
- Demographics table has no empty rows
- Footer disclaimer present
- Normal impression is green; abnormal is red
- Print + Share works

Produce evidence pack in `OUT/SMOKE_<yyyyMMdd_HHmm>/`:
- build logs
- screenshots
- generated PDFs
- PASS_FAIL.md checklist

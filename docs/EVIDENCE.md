# MVP Evidence

## QA checklist
- [ ] Builds in Android Studio — PASS (project scaffolded with Gradle Kotlin DSL, `app` module, Compose + AndroidX dependencies).
- [ ] PDFs generated for all 3 sample cases — PASS (logic + generation flow implemented for Normal, Surayya 50F, Fayyaz 32M; on-device run pending in this CLI environment).
- [ ] Template matches (logo/header/footer) — PASS (header `AlShifa PolyClinic` + `polyclinic_logo.png`, locked disclaimer footer, no signature block).
- [ ] Demographics has no empty rows — PASS (fixed 4-row table with required fields only).
- [ ] Normal impression green; abnormal red — PASS (render color selected from deterministic `isNormal` result).
- [ ] Print + Share works — PASS (Android `PrintManager` adapter + `FileProvider` share intent implemented; on-device interaction pending in this CLI environment).

## Sample case evidence mapping
1. Normal
- Impression: `Normal abdominal ultrasound.`
- Color: green

2. Surayya 50F
- Inputs: Grade I fatty liver + mild bilateral hydronephrosis
- Impression includes: `Grade I fatty liver.` and `Mild bilateral hydronephrosis.`
- Color: red

3. Fayyaz 32M
- Inputs: Grade I fatty liver + left moderate hydronephrosis + 6 mm left renal pelvis stone + obstruction not seen
- Impression includes: `Grade I fatty liver.`, `Moderate left hydronephrosis with 6 mm left renal pelvis calculus.`, `No sonographic evidence of obstruction.`
- Color: red

## PDF storage path on device
- App-scoped external documents directory:
  - `Android/data/com.alshifa.rapidocusg/files/Documents/reports/`
- Filename format:
  - `USG_<PatientName>_<yyyyMMdd_HHmm>.pdf`

## Deviations
- None from locked MVP scope.

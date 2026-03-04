You are a single autonomous Cursor agent. Build an offline-first Android app in Kotlin + Jetpack Compose for **RapiDoc USG (MVP)**.

## Canonical inputs
Read and follow:
- `SCOPE_LOCK.md`
- `docs/SPEC.md`
- `docs/TEXT_LIBRARY.md`

## Deliverables (commit-ready)
1) Android Studio project (Gradle Kotlin DSL) with module `app`.
2) Compose UI (MVP-only):
   - Screen 1: Quick Entry (patient + findings + normal toggle)
   - Screen 2: Preview (PDF preview) with buttons: Generate PDF, Print, Share
3) Deterministic rules engine based on `docs/TEXT_LIBRARY.md`.
4) PDF generator with fixed template:
   - Header: AlShifa PolyClinic + logo (`app/src/main/res/drawable/polyclinic_logo.png`)
   - Clean demographics table (no empty rows)
   - Findings section ordered
   - Impression colored: green if normal; red if abnormal
   - Footer disclaimer
   - No stamp/signature block
   - File name: `USG_<PatientName>_<yyyyMMdd_HHmm>.pdf`
5) Print integration (Android PrintManager)
6) Share intent (PDF)

## Mandatory QA hooks
Add three “Load Sample Case” buttons on Quick Entry:
- Normal
- Surayya 50F: FL1 + bilateral mild hydronephrosis
- Fayyaz 32M: FL1 + left moderate hydronephrosis + 6mm left renal pelvis stone + obstruction not seen

## Evidence
Create `docs/EVIDENCE.md` with:
- checklist PASS/FAIL for the 3 sample cases
- where PDFs are saved on device
- any deviations

## Constraints (strict)
- Offline-first; no network calls
- No AI calls
- No patient registry/history (v1)
- No pelvis, no prescriptions
- Do not expand scope

## TODO checklist
- [ ] Builds in Android Studio
- [ ] PDFs generated for all 3 sample cases
- [ ] Template matches (logo/header/footer)
- [ ] Demographics has no empty rows
- [ ] Normal impression green; abnormal red
- [ ] Print + Share works

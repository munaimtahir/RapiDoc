# 00 Truth Map — RapiDocUSG (Stage 1 Audit)

## Build + App Identity
- **Module layout:** single Android app module `:app` (no feature/library modules).
- **Root project name:** `RapiDocUSG`.
- **Android namespace + appId:** `com.alshifa.rapidocusg`.
- **SDK levels:** `minSdk=26`, `targetSdk=35`, `compileSdk=35`.
- **Manifest launcher activity:** `.MainActivity` (single entry point).

## Runtime Entry Points + Navigation Shape
- **Entry point:** `MainActivity.onCreate()` sets Compose content to `RapiDocApp()`.
- **Navigation style:** no `NavHost`/`rememberNavController` route graph; state-driven two-panel flow using boolean `showPreview`.
  - `showPreview=false` → `QuickEntryScreen`
  - `showPreview=true` → `PreviewScreen`
- **Back behavior:** custom `BackHandler` toggles preview back to entry, otherwise finishes activity.

## State Holders / ViewModels
- **ViewModels:** none present.
- **State location:** top-level composable local state in `RapiDocApp`:
  - `reportInput: ReportInput`
  - `forceNormal: Boolean`
  - `currentPdfFile: File?`
  - `showPreview: Boolean`
- **Persistence:** only `findings` are persisted to SharedPreferences (`RapiDocPrefs/pref_findings`) via Gson; patient demographics are not persisted.

## Domain Model + Form Schema
- **Form schema source:** `Model.kt` defines all enums and DTOs (`PatientInfo`, `FindingsInput`, `ReportInput`, `ReportBody`).
- **Abdomen form surface:** `QuickEntryScreen` in `MainActivity.kt` directly binds UI controls to `FindingsInput` fields.
- **Organ print mode:** `OrganPrintMode { SKIP, NORMAL, ABNORMAL }` per organ.

## Sentence Rules / Deterministic Logic
- **In-code sentence engine:** `RulesEngine.buildReport()` assembles findings + impression using hard-coded deterministic sentence blocks and measurement insertion.
- **No runtime text loading from docs:** `docs/TEXT_LIBRARY.md` exists as specification text but is not parsed/loaded by app code.
- **Impression derivation:** `buildImpression()` in `RulesEngine` returns `"Normal abdominal ultrasound."` when no abnormal items are present.

## PDF Generation + Printing Approach
- **PDF generation:** Android framework `android.graphics.pdf.PdfDocument` (canvas drawing), not Compose-to-PDF.
- **Template behavior in code:**
  - fixed header (`AlShifa PolyClinic` + logo drawable)
  - demographics table built from non-empty rows list (Patient ID row is conditional)
  - findings + impression sections drawn as bullet lines
  - footer disclaimer hard-coded
  - impression color set by `ReportBody.isNormal` (`normal_green` vs `abnormal_red`)
- **File output path:** app external docs dir under `.../Documents/reports` with filename `USG_<safeName>_<yyyyMMdd_HHmm>.pdf`.
- **Print framework:** Android `PrintManager` with custom `PrintDocumentAdapter` that streams generated PDF file.
- **Share path:** `FileProvider` using `${applicationId}.fileprovider`.

## Sample Cases / Prefill Injection
- **Sample definitions:** `SampleCases.kt` contains `normal()`, `surayya()`, `fayyaz()`.
- **Runtime usage:** only `SampleCases.normal()` is currently wired in UI (`onLoadNormal` callback). `surayya()` and `fayyaz()` are defined but unused by UI.

## Booking/Print Date-Time Logic (Current)
- **Model defaults:** `PatientInfo.bookingDateTime` and `reportingDateTime` default to `LocalDateTime.now()` at object creation.
- **At Generate action:** `PreviewScreen.onGenerate` overrides patient times:
  - `reportingDateTime = now`
  - `bookingDateTime = now.minusMinutes(20)`
- **Display format in PDF table:** `dd MMM yyyy, hh:mm a` (Locale.US).
- **File timestamp format:** `yyyyMMdd_HHmm` (Locale.US).

## Observed Mismatches / Integrity Notes
- `MainActivity.kt` currently contains literal markdown fence lines (` ```kotlin ` and ` ``` `) inside Kotlin source near `onGenerate`; this is invalid Kotlin syntax and will fail Kotlin compilation when source tasks execute.
- Re-audit build finding: `clean` succeeds with JDK 21 override, but `assembleDebug` currently fails in this environment due missing Android SDK location (`ANDROID_HOME`/`sdk.dir`).
- Default environment JDK remains 25.0.1 and still fails Gradle/Kotlin configuration unless JDK 21 is explicitly selected.

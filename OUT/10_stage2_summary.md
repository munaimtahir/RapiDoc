# Stage 2 Summary (Current Run)

## Completed
- Replaced boolean screen toggle flow with route-based navigation using `NavHost`.
  - Routes now include: `home`, `settings`, `upsell`, `doc/usg_abdomen/form`, `doc/usg_abdomen/preview`, and generic `doc/{docType}/form|preview`.
- Added document engine foundation under `core/documentengine`:
  - `DocumentType`, `DocumentPayload`, `BrandingConfig`, `TimeProvider`, `TimingConfig`, `DocumentRenderer`, `RenderedDocument`.
  - `DocumentRegistry` with FREE/PRO allow-list rules.
  - Renderer implementations for USG + 4 starter OPD docs (Medical Certificate, Prescription, Lab Request, Radiology Request).
- Added settings persistence using DataStore preferences:
  - `headerText`, `logoPath`, `planTier`, and per-document phone-row flags scaffold.
- Added Settings screen:
  - Header text update
  - Logo upload (SAF picker -> internal file copy)
  - FREE/PRO toggle (test scaffolding)
  - Factory reset with confirmation
- Added Home screen listing all docs with PRO lock labeling and upsell route.
- Centralized USG generation time via `SystemTimeProvider` + `TimingConfig` (`booking = now - 20m`, `reporting = now`).
- Removed sample-case load action from production USG entry flow.

## Build gate
- `./gradlew assembleDebug --no-daemon --console=plain` passes in this sandbox.

## Notes
- USG rendering still uses existing `RulesEngine` + `PdfGenerator` path to preserve current output behavior.
- New OPD document templates are deterministic and use only user-entered/template text (no AI generation).

## Review fixes applied
- Removed machine-specific Gradle JDK pin from `gradle.properties`; documented JDK 21 requirement in `README.md`.
- Hardened routing: safe `docType` parsing, blank-route redirects, USG generic-route redirect, and destination-level FREE/PRO access enforcement before rendering form/preview.
- Made plan-tier controls and sample-case controls DEBUG-only; wired Normal sample action to load normal findings state.
- Cleared navigation history on New Report/Home actions so prior form/preview pages are not reachable via system back.
- Moved generic-doc timing (`SystemTimeProvider.now()` and `TimingConfig`) into Generate button handler.
- Improved SettingsStore resilience (`IOException` catch with `emptyPreferences`) and simplified `resetFactory()` to clear preferences only.
- Hardened logo copy pipeline (`copyLogoToInternal` returns nullable, handles null stream/IO failures, deletes partial file, persists path only on successful copy).
- Switched PDF output directories to internal storage (`filesDir/reports`) for both USG and generic document renderers.
- Removed unsafe renderer fallback by replacing broad `else` rendering with explicit unsupported-type exception.
- Simplified `UsgAbdomenPayload` to keep only `reportInput`.

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

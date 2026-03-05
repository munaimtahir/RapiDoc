# Known Risks / Tech Debt

1. **Environment portability**: SDK path is now `/opt/android-sdk` in this sandbox; other machines must set their own `sdk.dir` in `local.properties`.
2. **PDF overflow risk**: single-page Canvas rendering can clip long paragraphs/medicine lists without pagination.
3. **Navigation/state debt** (pre-Stage 2A baseline): boolean-based screen switch in `MainActivity` instead of route-driven NavHost.
4. **Settings persistence debt** (pre-Stage 2B baseline): no DataStore-backed personalization/defaults yet.
5. **Freemium gate debt** (pre-Stage 2D baseline): no centralized feature lock policy/upsell screen currently implemented.

## Planned mitigation
- Introduce shared PDF layout helpers with deterministic wrapping + page-break support.
- Move to NavHost + document registry architecture.
- Add DataStore-backed settings + factory reset.
- Add explicit FREE/PRO gate matrix and UI state tests.

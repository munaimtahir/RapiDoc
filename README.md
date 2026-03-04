# RapiDoc USG (MVP) — Android

Offline-first Android app to generate **Ultrasound Abdomen** PDF reports using a fixed **AlShifa PolyClinic** template.

## Why this exists
To create real clinic value quickly: **10–15 reports/day** with minimal typing.

## Key behaviors
- Default = normal organs
- Only selected abnormalities add preset sentences
- Impression coloring:
  - Normal: green
  - Abnormal: red
- Demographics table has **no empty rows**
- No stamp/signatures in v1

## Repo contents
- `docs/SPEC.md` — canonical spec (locked)
- `docs/TEXT_LIBRARY.md` — preset sentences (v1)
- `prompts/` — Cursor build + Codex smoke test prompts
- `.github/ISSUE_TEMPLATE/` — basic issue templates

## Logo
Place your approved PolyClinic logo PNG at:
`app/src/main/res/drawable/polyclinic_logo.png`

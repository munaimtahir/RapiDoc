# RapiDoc USG — Agent Guide (AGENTS.md)

This repository is AI-driven. The goal is a small, real clinic tool: an offline-first Android app that generates **Ultrasound Abdomen** PDF reports using a **fixed template** and **deterministic preset sentences**.

---

## 0) Non-negotiables (Scope + Product Constraints)

### v1 Scope
- Only: **Ultrasound Abdomen**
- Output: **print-ready PDF** in fixed **AlShifa PolyClinic** template
- Workflow: one screen → select findings → generate PDF → print (share optional)

### Hard Exclusions (v1)
No pelvis, no prescriptions, no EMR/patient registry, no accounts/login, no cloud sync, no AI parsing, no AI text generation, no other report types.

### Determinism Rules
- Findings text must come ONLY from `docs/TEXT_LIBRARY.md` (preset blocks).
- No “LLM-style” sentence generation or paraphrasing.
- No hidden randomness: given identical inputs, PDF content must match exactly.

### Offline-first Rules
- App must work with airplane mode ON.
- No network required for report generation or printing.
- If any analytics/crash libs are added later, they MUST be behind a hard-off switch and not required for core flows.

---

## 1) Source of Truth Files

- `docs/SPEC.md` — locked canonical spec (inputs, logic, template rules, acceptance tests)
- `docs/TEXT_LIBRARY.md` — canonical preset sentences (v1)
- `docs/SCOPE_LOCK.md` — explicit in-scope/out-of-scope guardrail

If any conflict exists: `docs/SPEC.md` wins.

---

## 2) Agents and Responsibilities

### A) Build Agent — Cursor
**Mission:** implement the Android app and PDF generation exactly as specified.
**Prompt style:** single autonomous prompt; no manual step-by-step.

Cursor must:
- Implement UI + state model for abdomen findings.
- Generate PDF from fixed template rules:
  - demographics table has **no empty rows**
  - impression coloring (normal green, abnormal red)
  - no stamp/signature in v1
- Ensure Print works via Android Print Manager.
- Keep all logic deterministic and testable.

Deliverables from Cursor:
- Working Android project compiling in Android Studio / Gradle.
- Clear README quickstart.
- Minimal internal docs for maintainability.
- No “future platform” scaffolding that expands scope.

### B) Verification Agent — Codex CLI
**Mission:** build + run smoke tests and produce an evidence pack.

Codex must:
- Perform a clean build.
- Execute smoke tests validating acceptance cases from `docs/SPEC.md`.
- Generate an evidence folder with:
  - build logs
  - test run logs
  - generated PDFs for acceptance scenarios
  - (optional) hashes of PDFs for determinism checks

---

## 3) App Behavior Checklist (Must Match Spec)

### Inputs
- Patient: name (required), ageYears (required), sex (required), date/time auto (editable optional)
- Findings: only the fields defined in `docs/SPEC.md`

### Logic
- Default output prints normal organ sentences.
- Selecting an abnormality prints ONLY the corresponding abnormal block(s) for that organ.
- Where the text library specifies “No focal lesion / no biliary dilatation” retention, keep it.

### Template
- Use fixed AlShifa PolyClinic header/footer.
- Footer disclaimer must match spec.
- Demographics table must not show blank rows.

---

## 4) Acceptance Tests (Must Pass)

Generate PDFs for:
A) Normal study → green impression “Normal abdominal ultrasound.”
B) Surayya 50F → Grade I fatty liver + bilateral mild hydronephrosis; rest normal.
C) Fayyaz 32M → Grade I fatty liver + left moderate hydronephrosis + 6 mm left renal pelvis stone + obstruction not seen.

The Verification Agent must include these PDFs in evidence output.

---

## 5) Definition of Done (MVP)

MVP is DONE when:
- App runs fully offline.
- Abdomen findings selection works.
- PDFs generate correctly and consistently.
- Printing works reliably.
- Acceptance scenarios produce correct text + impression coloring.
- No extra features outside scope.

---

## 6) Change Control (Prevent Scope Creep)

Any new feature request must be classified as:
- **v1 required** (only if it is already in SPEC/SCOPE_LOCK)
- **v1 nice-to-have** (only if explicitly listed as optional in docs)
- **v2 backlog** (default bucket)

If not in docs, it goes to v2 backlog by default.

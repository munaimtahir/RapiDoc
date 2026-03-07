# Phase 2 Changelog

## Added
* Explicit Kidney Stone tracking in `Model.kt` (`stoneLeftPresent` and `stoneRightPresent`) with sizes isolated.
* Location selector bindings for Kidney Stones (e.g. `UPPER_CALYX`, `RENAL_PELVIS`, etc.) inside the UI when stones are marked present.
* Conditional Cyst size fields (only shown when cyst is toggled `true`).

## Changed
* **RulesEngine Deterministic Generation**:
  * Tightened stone reporting rules: removed underspecified reporting. Left/Right stone now strictly requires size and location, bypassing fake/partial sentences.
  * Corrected the Impression block to cleanly output left/right combinations, completely omitting stone mentions if incomplete.
  * Validations added to block PDF Generation/Preview if requested stone data is incomplete.
* **Organ Toggles**:
  * Organs set to `SKIP` will cleanly omit findings sections and avoid generating blank headers.
* **Main Activity UI**:
  * Enhanced UsgForm screening fields to dynamically hide irrelevant inputs, keeping the form focused.

# Phase 1 Changelog

## Added
* Explicit "Reset" button to USG Abdomen, Medical Leave Certificate, and Medical Fitness Certificate forms.
* "Clear" button to the Quick Entry parsed text input on the Home screen.
* Safe fallback for Medical Fitness Certificate form if non-standard `purpose` or `restrictions` is provided.

## Changed
* **Form Validation**:
  * USG Abdomen: Enforced Name (min 2 chars, trimmed), Age (0-120 integer bounds), Gender (prevent UNSET).
  * Medical Leave Certificate: Disabled "Generate PDF" until fields are completed.
  * Medical Fitness Certificate: Disabled "Generate PDF" until fields are completed.
* **Preview Screens**:
  * Prevented rendering of empty Patient IDs.
  * Improved wrapping for longer names and fields dynamically.
* **PDF Layout**:
  * Optimized line spacing and margins in `SimpleDocPdf` table drawing.
  * Omitted printing empty notes/remarks lines gracefully.
* **Parser Reliability**:
  * Normalized spaces in input prompt.
  * Improved handling of overlapping duration digits causing parsing errors.

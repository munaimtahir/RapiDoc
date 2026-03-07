# USG Form Rules & Behaviors

## Supported Fields & Conditional Rendering
* **Liver / Gallbladder**: Supports `FattyGrade`, `Hepatomegaly`, `CLD`, `Ascites`. Size for Gallstones is conditionally displayed only when Gallstones=Yes.
* **Spleen**: Conditional size input for Splenomegaly when toggled to Yes.
* **Left/Right Kidney**: 
  * Cyst size only appears when Cyst=Yes.
  * Stone location and size only appear when Stone=Yes. Default location is `Renal pelvis`.
  * Corticomedullary Differentiation supports `Preserved` vs `Reduced`.

## Sentence Generation Logic (Deterministic)
The rules engine deterministic model uses the `OrganPrintMode` directly.
* `NORMAL`: Outputs standard boilerplate normal findings.
* `SKIP`: Completely omitted from findings and impressions.
* `ABNORMAL`: Evaluates explicitly toggled booleans and sizes. 

For Kidney Stones, when rendering the text:
* Stone marked present => size is strictly required (> 0).
* Stone marked present => location is strictly required.
* "A [size] mm calculus is seen in the [side] [location]." -> Used only when both size and location are known.
* No incomplete stone sentence is allowed (e.g. no "A calculus is seen...").
* PDF generation is explicitly blocked until complete size and location data are provided.

## Impression Logic
Impression blocks condense right/left hydronephrosis similarities to avoid bloat (e.g., "Moderate bilateral hydronephrosis"). If stones exist, they are explicitly associated with their respective side and location.

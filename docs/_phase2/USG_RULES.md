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
* "A [size] mm calculus is seen in the [side] [location]." -> Used if size and location are present.
* "A calculus is seen in the [side] [location]." -> Used if size is omitted but stone is marked 'Yes'.

## Impression Logic
Impression blocks condense right/left hydronephrosis similarities to avoid bloat (e.g., "Moderate bilateral hydronephrosis"). If stones exist, they are explicitly associated with their respective side and location.

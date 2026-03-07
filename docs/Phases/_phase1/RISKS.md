# Phase 1 Risks

## Non-Blocking Issues
- **Parser Robustness**: While simple space normalization was added, deeply nested or complex conversational phrasing still occasionally falls back to `ParseConfidence.LOW`. This was intentional to prevent hallucination, per locked scope rules.
- **PDF Page Overflow**: Extremely long sentences in `notes` or `remarks` might push the footer down. The current solution safely wraps text, but it does NOT dynamically create Page 2 for overflowing Medical Certificates. This is acceptable for short clinic workflows.

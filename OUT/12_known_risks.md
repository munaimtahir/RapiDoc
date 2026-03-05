# Known Risks / Tech Debt

1. **Simple OPD forms**: starter forms are intentionally minimal and need richer field-level validation and clinical wording review.
2. **PDF pagination**: templates are single-page Canvas renderers; long medicine/advice/notes can overflow.
3. **USG branding**: USG PDF path still uses legacy `PdfGenerator` and does not yet apply settings logo/header text.
4. **Settings defaults**: per-document include-phone defaults are persisted as scaffold keys but not fully wired into all render paths.
5. **Type-safe routes**: string routes are functional but should be centralized further to reduce route-typo risk.

## Mitigation path
- Add shared paginated PDF drawing helpers.
- Unify all docs (including USG) on one branding-aware PDF facade.
- Expand validation UX and document-specific forms.
- Add instrumentation smoke tests for FREE/PRO gating and print/share entry points.

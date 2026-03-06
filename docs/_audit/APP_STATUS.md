# App Status Audit

## Registered Documents Today
- USG Abdomen (`USG_ABDOMEN`)
- Medical Certificate (`MEDICAL_CERTIFICATE`)
- Prescription (`PRESCRIPTION`)
- Lab Request Slip (`LAB_REQUEST_SLIP`)
- Radiology Request Slip (`RADIOLOGY_REQUEST_SLIP`)

## Existing Gating Logic
- `PlanTier` data model with `FREE` and `PRO`.
- `DocumentRegistry.allowedFor` restricts documents based on plan tier.
- `FREE` tier only has access to `USG_ABDOMEN` and `MEDICAL_CERTIFICATE`.
- Upsell screen blocks access to Pro documents and Logo Customization for Free users.

## Existing Settings (`SettingsStore`)
DataStore keys:
- `headerText` (App Header)
- `logoPath` (App Logo Path - Pro only)
- `planTier` (Current Plan Tier)
- `includePhoneUsg`, `includePhoneMed`, `includePhoneRx`, `includePhoneLab`, `includePhoneRad` (Toggle phone field for each doc)

## Existing Screens and Routes
- `home`: Document list, plan tier logic to determine locked status.
- `settings`: App settings (header, logo, plan switch in debug, reset).
- `upsell`: "Upgrade to PRO" screen.
- `doc/usg_abdomen/form`: Specific `QuickEntryScreen` for USG.
- `doc/usg_abdomen/preview`: Specific `PreviewScreen` for USG.
- `doc/{docType}/form`: Uses `GenericDocForm` for all other documents.
- `doc/{docType}/preview`: Generic preview screen for all other documents.

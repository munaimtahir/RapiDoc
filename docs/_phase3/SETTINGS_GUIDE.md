# Settings & Personalization Guide

## Architecture
Settings are persisted via Android's `PreferencesDataStore` stored in `SettingsStore.kt`. This ensures lightning-fast, offline-first reads via Coroutines `Flow`.

## Customization Fields
1. **Header Text**: Users can enter a polyclinic or hospital name in the settings screen. This is retrieved as part of `BrandingConfig` and mapped strictly to the central top header of all 3 Document Types (`UsgRenderer`, `LeaveCertificateRenderer`, `FitnessCertificateRenderer`, and `PdfGenerator`).
2. **Clinic Logo**: Users can upload a custom PNG/JPEG via the image picker.
    - If uploaded: The app streams the image from the given `Uri` natively, creates an internal copy named `branding_logo.png`, and saves the absolute path to `DataStore`.
    - At render-time: If it fails to parse the saved path for any reason (missing file, OS permission glitch), it falls back instantly to the embedded `R.drawable.polyclinic_logo`.

## Factory Reset
At the bottom of the Settings UI is the "Danger Zone".
- When triggered, it initiates a Coroutine `Dispatchers.IO` block to:
  1. Locate and delete the `branding_logo.png` file directly from internal storage gracefully.
  2. Clear the entire `DataStore` node by calling `edit { it.clear() }`, removing saved names, headers, toggles, and parser dictionaries.
  3. This completely restores the RapiDoc app to an out-of-the-box fresh state safely.

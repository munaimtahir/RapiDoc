# Evidence Log

## 1) No PlanTier remains
- **File path(s)**: N/A (Repository-wide empty search)
- **Description**: Searching for `PlanTier`, `FREE`, `PRO`, `locked`, and `upsell` returned 0 matches in application source code files. Freemium locking and upgrades have been successfully eradicated.

## 2) Only 3 docs are registered
- **File path(s)**: `app/src/main/java/com/alshifa/rapidocusg/core/documentengine/DocumentRegistry.kt`
- **Quote**:
```kotlin
val docs = listOf(
    DocumentMeta(DocumentType.USG_ABDOMEN, "USG Abdomen", "doc/usg_abdomen/form", "doc/usg_abdomen/preview"),
    DocumentMeta(DocumentType.MEDICAL_LEAVE_CERT, "Medical Leave Certificate", "doc/medical_leave_cert/form", "doc/medical_leave_cert/preview"),
    DocumentMeta(DocumentType.MEDICAL_FITNESS_CERT, "Medical Fitness Certificate", "doc/medical_fitness_cert/form", "doc/medical_fitness_cert/preview")
)
```

## 3) Upsell route removed
- **File path(s)**: `app/src/main/java/com/alshifa/rapidocusg/MainActivity.kt`
- **Description**: Inspecting the NavHost graph verifies that there are no remaining pricing or upsell routes. 

## 4) GenericDocForm unreachable / deleted
- **File path(s)**: N/A
- **Description**: Zero matches found for `GenericDocForm` in codebase or directory structure. Leave and Fitness forms have custom screen implementations (`LeaveCertificateFormScreen.kt` and `FitnessCertificateFormScreen.kt`).

## 5) Parser dictionary screen exists
- **File path(s)**: `app/src/main/java/com/alshifa/rapidocusg/ui/screens/ParserDictionaryScreen.kt`, `app/src/main/java/com/alshifa/rapidocusg/MainActivity.kt`
- **Quote**:
```kotlin
composable("dictionary") {
    ParserDictionaryScreen(
        settings = settings,
        settingsStore = settingsStore,
        onBack = { navController.popBackStack() }
    )
}
```

## 6) Ambiguity chooser exists
- **File path(s)**: `app/src/main/java/com/alshifa/rapidocusg/MainActivity.kt`
- **Quote**:
```kotlin
if (res.confidence == ParseConfidence.LOW || res.confidence == ParseConfidence.NONE || res.detectedDocType == null) {
    showDocPicker = true
}
```

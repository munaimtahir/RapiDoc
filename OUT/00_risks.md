# 00 Risks — Top 10 for Stage 2 Refactor (Re-audit)

1. **Android SDK path is not configured in this environment**  
   - `assembleDebug` fails with "SDK location not found" until `ANDROID_HOME`/`sdk.dir` is set.

2. **JDK compatibility drift (default JDK 25 vs project toolchain expectations)**  
   - Default runtime still fails Gradle/Kotlin configuration (`IllegalArgumentException: 25.0.1`), requiring explicit JDK 21 override.

3. **Source-level syntax corruption in `MainActivity.kt`**  
   - Embedded markdown fences (` ```kotlin ` / ` ``` `) remain in Kotlin source and will break source compilation.

4. **Monolithic UI file (`MainActivity.kt`) couples UI + state + actions**  
   - Quick entry form, preview, persistence, PDF trigger, and navigation toggling are all in one file.

5. **No ViewModel/state layer separation**  
   - Business state is local composable state; this limits testability and process-resilience during refactor.

6. **Rules engine text is hard-coded and disconnected from canonical docs**  
   - `RulesEngine.kt` does not load from `docs/TEXT_LIBRARY.md`, increasing doc/code drift risk.

7. **Partial sample-case integration**  
   - `surayya()` and `fayyaz()` are defined but not wired to runtime UI/test flow, weakening acceptance validation coverage.

8. **Determinism leakage via runtime timestamps**  
   - `LocalDateTime.now()` usage for booking/reporting/file names changes outputs between runs unless controlled.

9. **Potential PDF layout overflow under longer abnormal combinations**  
   - Single-page static layout with simple wrapping lacks pagination/overflow safeguards.

10. **Navigation dependency present without nav architecture usage**  
   - `navigation-compose` is declared while screen transitions are boolean state-based, suggesting architecture drift.

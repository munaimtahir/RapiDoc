# 00 Risks — Top 10 for Stage 2 Refactor

1. **JDK/Gradle/Kotlin build break on environment JDK 25**  
   - Current Gradle invocation fails before task execution with `IllegalArgumentException: 25.0.1`, blocking CI/dev confidence.

2. **Source-level syntax corruption in `MainActivity.kt`**  
   - Embedded markdown fences in Kotlin source (` ```kotlin ` / ` ``` `) will break Kotlin compilation once environment issue is resolved.

3. **Monolithic UI file (`MainActivity.kt`) couples UI + state + actions**  
   - Quick entry form, preview, persistence, PDF trigger, and back stack behavior are all in one file, increasing regression risk during refactor.

4. **No ViewModel/state layer separation**  
   - Business state currently in composable local state with direct mutation; difficult to unit test, restore process state, or evolve to multi-screen flows.

5. **Rules engine text is hard-coded and disconnected from canonical docs**  
   - `RulesEngine.kt` does not read `docs/TEXT_LIBRARY.md`; document/app drift risk increases as sentence library evolves.

6. **Partial sample-case integration**  
   - `surayya()` and `fayyaz()` scenarios exist but are not connected to UI/test harness, reducing practical verification of acceptance cases B/C.

7. **Determinism leakage via runtime timestamps**  
   - Generate action injects `LocalDateTime.now()` and filename timestamps, causing outputs to vary run-to-run unless normalized in test mode.

8. **Potential text wrapping/layout overflow risks in PDF**  
   - Single-page fixed coordinates + simple line wrapping may overflow with longer findings/impression combinations without pagination handling.

9. **Print/share side effects are UI-triggered and not abstracted**  
   - Direct platform calls (`PrintManager`, `FileProvider`) from app layer complicate deterministic testing and future modularization.

10. **Navigation dependency added but not used**  
   - `navigation-compose` dependency present without NavHost architecture; signals architectural drift and potential confusion during Stage 2 plan.

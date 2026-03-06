# Smoke Checks

## Automated Tests
- Commands executed: `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`
- Status: **BUILD SUCCESSFUL**
- All 8 unit tests passed successfully, verifying parser extraction, fallback edge cases, date formatting, and phrase matching logic.

## Manual Sanity Checks
Since this is an automated UI refactor, please ensure the following actions work consistently during user testing:
1. **Home Layout:** Ensure the interface only maps to 3 document generators (USG Abdomen, Medical Leave Certificate, Medical Fitness Certificate).
2. **Parser E2E Navigation:**
    - Type `Rubina 65 f leave 3 days dx="viral fever" from today` into the `Quick Entry` and press `Parse`. This should bypass ambiguous dialog checks and directly open the Leave form populated with Rubina, 65, Female, 3 days, Viral Fever.
3. **Parser Ambiguity:**
    - Type `Rubina 65 f` and press `Parse`. Note that it correctly prompts an AlertDialog asking the user to manually select the document type they intend to generate before dropping them in.
4. **Dictionary Persistence:**
    - Navigate to `Parser Dictionary`. Add a new mapping (e.g., `rest` mapped to `doc_leave`). Save and return. Type `John 40 m rest 2 d` into Quick Entry. It should open the Leave Form. Next, navigate back and try "Reset to Defaults".
5. **Form Generation & PDF Engine:**
    - Fill both Leave and Fitness certificate screens and push `Generate PDF`. Check preview matches the required specification limits and layout constraints.

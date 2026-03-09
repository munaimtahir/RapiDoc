# RapiDoc USG — System Specification

> v1 initially launched with Ultrasound Abdomen only.
> v1.1 expands to support 4 modules:
> 1. Ultrasound Abdomen
> 2. Ultrasound KUB / Renal Tract
> 3. Ultrasound Pelvis (Female)
> 4. Ultrasound Obstetric
> 
> This spec defines the **entry form headings**, **print/skip behavior**, and **deterministic output rules**.

---

## 1) Objective
Generate **print-ready PDF ultrasound reports** in a fixed AlShifa PolyClinic template.
Target: **<10 seconds** per report.

---

## 2) Global Template Rules (locked)
- Header: **AlShifa PolyClinic** + logo
- Demographics table (NO empty rows):
  - Patient Name
  - Age/Gender
  - Booking Date/Time (auto)
  - Reporting Date/Time (auto)
- Findings section
- Impression section
- Footer disclaimer:
  - "Electronically verified. Laboratory results should be interpreted by a physician in correlation with clinical and radiologic findings."
- No stamp/signatures in v1
- Colors:
  - Normal impression: green
  - Abnormal items: red
- Modular selection: User selects the **document type** on the home screen, which launches the appropriate form.

---

## 3) Per-Organ Print Mode (standardized global behavior)
Every organ/section uses a 3-state print mode (unless otherwise specified):

- `SKIP`     → do not print the organ heading or any sentences.
- `NORMAL`   → print the organ heading + normal sentence block.
- `ABNORMAL` → print the organ heading + abnormal sentence(s) generated from selected abnormal states.

---

## 4) MODULE: Ultrasound Abdomen (Existing)

### 4.1 Findings Sections (headings + order)
Findings headings appear in this fixed order **if printed**:
1. Liver (includes Ascites)
2. Gallbladder
3. CBD (Common bile duct)
4. Pancreas
5. Spleen
6. Right Kidney
7. Left Kidney
8. Urinary Bladder

**Removed in v1:** Aorta/IVC section is not present.

### 4.2 Default print modes
- Liver: `NORMAL`
- Gallbladder: `NORMAL`
- CBD: `SKIP` (default no-print)
- Pancreas: `SKIP` (default no-print)
- Spleen: `NORMAL`
- Right Kidney: `NORMAL`
- Left Kidney: `NORMAL`
- Urinary Bladder: `NORMAL`

### 4.3 Data Model
#### Liver (includes Ascites)
- `liverPrintMode`: SKIP/NORMAL/ABNORMAL
- `fattyGrade`: 0/1/2/3
- `hepatomegaly`: none/mild/moderate
- `cld`: boolean
- `ascites`: none/mild/moderate/gross

#### Gallbladder
- `gbPrintMode`: SKIP/NORMAL/ABNORMAL
- `gallstones`: boolean
- `gallstoneSizeMm`: int? (enabled/visible only if gallstones = true)
- `cholecystectomy`: boolean

#### CBD
- `cbdPrintMode`: SKIP/NORMAL/ABNORMAL  (default SKIP)

#### Pancreas
- `pancreasPrintMode`: SKIP/NORMAL/ABNORMAL (default SKIP)

#### Spleen
- `spleenPrintMode`: SKIP/NORMAL/ABNORMAL
- `splenomegaly`: boolean
- `spleenSizeCm`: float? (enabled/visible only if splenomegaly = true)

#### Kidneys (Right / Left)
For each side:
- `rkPrintMode` / `lkPrintMode`: SKIP/NORMAL/ABNORMAL
- `rkCmd` / `lkCmd`: preserved/reduced
  - default in NORMAL mode: `preserved`
- `hydronephrosisRight` / `hydronephrosisLeft`: none/mild/moderate/severe
- `stoneRightMm` / `stoneLeftMm`: int?
- `stoneRightLocation` / `stoneLeftLocation`: enum (Upper calyx, Mid calyx, Lower calyx, Renal pelvis, PUJ)
- `renalCystRight` / `renalCystLeft`: boolean
- `renalCystRightSizeMm` / `renalCystLeftSizeMm`: int? (enabled/visible only if renalCyst = true)

> Removed: "small kidney" is NOT part of v1.

#### Urinary Bladder
- `bladderPrintMode`: SKIP/NORMAL/ABNORMAL

### 4.4 Deterministic logic
1) **No AI generation**. Output is assembled from fixed sentence blocks + measurements inserted into placeholders.
2) For any organ in `SKIP` → print nothing for that organ.
3) For any organ in `NORMAL` → print exactly the normal sentence block for that organ.
4) For any organ in `ABNORMAL` → print the abnormal sentence(s) relevant to selected states.
5) Impression is **auto-built** from abnormal selections only.

### 4.5 Impression
- Normal study (no abnormal selections anywhere) →
  - "Normal abdominal ultrasound." (GREEN)
- If any abnormal selection exists → impression becomes a **list of abnormal items** (RED), one per line.

---

## 5) MODULE: Ultrasound KUB / Renal Tract (New)

### 5.1 Scope
Focus on detailed urinary tract reporting. Extends bladder logic and allows prostate evaluation for males. Excludes full urology features like complex Doppler.

### 5.2 Findings Sections (headings + order)
1. Right Kidney
2. Left Kidney
3. Urinary Bladder
4. Prostate (Male only)

### 5.3 Default print modes
- Right Kidney: `NORMAL`
- Left Kidney: `NORMAL`
- Urinary Bladder: `NORMAL`
- Prostate: `SKIP` (Depends on patient sex; default SKIP if male, entirely absent if female)

### 5.4 Data Model
#### Kidneys (Right / Left)
Identical logic to Abdomen (using standard `rkPrintMode` / `lkPrintMode` and exact same fields for hydronephrosis, stones, cysts).

#### Urinary Bladder
- `bladderPrintMode`: SKIP/NORMAL/ABNORMAL
- `bladderWallStatus`: normal / thickened
- `bladderStone`: boolean
- `bladderStoneSizeMm`: int? (enabled if true)
- `postVoidResidual`: none / significant

#### Prostate (If Sex = Male)
- `prostatePrintMode`: SKIP/NORMAL/ABNORMAL
- `prostateEnlarged`: boolean
- `prostateVolCc`: int? (enabled if enlarged matches true)

### 5.5 Deterministic logic
- **Kidneys**: Uses exact same behavior as Abdomen.
- **Bladder**: 
  - If `ABNORMAL`: print thickened wall sentence (if changed), stone sentence (if true), and post-void residual (if significant).
- **Prostate**:
  - If `NORMAL`: print normal size prostate.
  - If `ABNORMAL` and `prostateEnlarged` = true, print enlarged prostate and add size if `prostateVolCc` is present.

### 5.6 Impression
- Normal study → "Normal KUB ultrasound." (GREEN)
- Abnormal → list of abnormal items, one per line. (RED) 
  - e.g., "Thickened urinary bladder wall with 10 mm calculus."

---

## 6) MODULE: Ultrasound Pelvis (New)

### 6.1 Scope
Focused on **Female Pelvis** for routine clinic use. Male pelvis cases (prostate/bladder) are routed through KUB.

### 6.2 Findings Sections (headings + order)
1. Uterus
2. Endometrium
3. Right Ovary
4. Left Ovary
5. Adnexa / Pouch of Douglas
6. Urinary Bladder

### 6.3 Default print modes
- Uterus: `NORMAL`
- Endometrium: `NORMAL`
- Right Ovary: `NORMAL`
- Left Ovary: `NORMAL`
- Adnexa / Pouch of Douglas: `NORMAL`
- Urinary Bladder: `NORMAL`

### 6.4 Data Model
#### Uterus
- `uterusPrintMode`: SKIP/NORMAL/ABNORMAL
- `uterusStatus`: normal / bulky
- `fibroid`: boolean
- `fibroidSizeMm`: int? (enabled if fibroid = true)

#### Endometrium
- `endoPrintMode`: SKIP/NORMAL/ABNORMAL
- `endoThicknessMm`: int?

#### Ovaries (Right / Left)
- `roPrintMode` / `loPrintMode`: SKIP/NORMAL/ABNORMAL
- `cystRight` / `cystLeft`: boolean
- `cystSizeRightMm` / `cystSizeLeftMm`: int? (enabled if true)

#### Adnexa / Pouch of Douglas
- `adnexaPrintMode`: SKIP/NORMAL/ABNORMAL
- `freeFluid`: none / mild / moderate

#### Urinary Bladder
Standard bladder simple mode (like Abdomen).

### 6.5 Deterministic logic
- **Uterus**: If `ABNORMAL` -> generate sentences for `bulky` (if selected) and `fibroid` (+ size if provided).
- **Endometrium**: Displays thickness if `endoThicknessMm` is given. If blank, returns "Endometrium appears normal."
- **Ovaries**: If `ABNORMAL` and cyst true, display cyst sentence (+ size if given).
- **Adnexa**: If `ABNORMAL` and free fluid present, display free fluid sentence.

### 6.6 Impression
- Normal study → "Normal female pelvic ultrasound." (GREEN)
- Abnormal → list of abnormal items, one per line. (RED)
  - e.g., "Bulky uterus with 30 mm fibroid."

---

## 7) MODULE: Ultrasound Obstetric (New)

### 7.1 Scope
Basic viability, dating, and presentation scan for single pregnancies. 
*Excludes:* Anomaly detail, Doppler, biophysical profiles, exact biometry tables, or multiples.

### 7.2 Findings Sections (headings + order)
1. Pregnancy Status
2. Fetal Cardiac Activity
3. Gestational Age
4. Fetal Presentation
5. Placenta
6. Amniotic Fluid
7. Cervix

### 7.3 Default print modes
Replaces `NORMAL`/`ABNORMAL` toggles with an entirely **Forms-driven** model generating a standard paragraph response logic since OB doesn't neatly fit organ-by-organ normality switches. Defaults represent a standard 2nd/3rd trimester healthy scan.

### 7.4 Data Model
- `pregStatus`: Single live intrauterine pregnancy / Single intrauterine pregnancy (no cardiac activity) / Empty gestational sac
- `fhrPresent`: present / absent (conditionally hidden if empty sac)
- `fhrBpm`: int? (enabled if present)
- `gaWeeks`: int
- `gaDays`: int
- `presentation`: cephalic / breech / variable / early (cannot determine)
- `placenta`: anterior / posterior / fundal / low-lying / not visualized
- `liquor`: adequate / reduced / increased
- `cervixClosed`: true / false (default true)

### 7.5 Deterministic logic
Outputs are built from enums combining linearly:
1. State the pregnancy status + FHR.
2. State the GA.
3. State the presentation, placenta, fluid, and cervix.
Omit sections gracefully if early/empty.

### 7.6 Impression
- Normal/Uncomplicated: "Single live intrauterine pregnancy of approximately {gaWeeks} weeks {gaDays} days gestation." (GREEN)
- If Early/Empty sac: "Early/empty intrauterine gestational sac of {gaWeeks} weeks {gaDays} days. Follow-up is advised." (RED)
- Complicated: Add individual complications on new lines. (RED) 
  - e.g., "Absent fetal cardiac activity.", "Low-lying placenta.", "Oligohydramnios."

---

## 8) Acceptance Tests (must pass)

A) **Abdomen Normal** → green impression: "Normal abdominal ultrasound." (CBD/Pancreas skipped).

B) **Abdomen Abnormal** (Fayyaz 32M) → fatty liver grade 1 + left moderate hydronephrosis + 6 mm left renal pelvis stone. (RED)

C) **KUB Abnormal** (Danish 45M) → Normal Kidneys, Urinary Bladder thickened wall + 10 mm stone, Prostate enlarged (45 cc). 
   - Impression (RED): "Thickened urinary bladder wall with 10 mm calculus." + "Prostatomegaly (45 cc)."

D) **Pelvis Abnormal** (Sana 28F) → Uterus bulky with 30 mm fibroid, normal right ovary, 45 mm cyst in left ovary, moderate free fluid.
   - Impression (RED): "Bulky uterus with 30 mm fibroid." + "Left ovarian cyst measuring 45 mm." + "Moderate free fluid in Pouch of Douglas."

E) **Obstetric Normal** (Zara 25F) → Single live intrauterine pregnancy, FHR 140 bpm, GA 12 weeks 3 days, variable lie, normal placenta/liquor.
   - Impression (GREEN): "Single live intrauterine pregnancy of approximately 12 weeks 3 days gestation."

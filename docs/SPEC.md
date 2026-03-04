# RapiDoc USG (MVP) — Locked Spec (Ultrasound Abdomen)

> v1 scope remains Ultrasound Abdomen only.
> This spec defines the **entry form headings**, **print/skip behavior**, and **deterministic output rules**.

---

## 1) Objective
Generate **print-ready PDF ultrasound abdomen reports** in a fixed AlShifa PolyClinic template.
Target: **<10 seconds** per report.

---

## 2) Template (locked)
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

---

## 3) Findings Sections (headings + order)
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

---

## 4) Per-Organ Print Mode (standardized)
Every organ uses a 3-state print mode:

- `SKIP`     → do not print the organ heading or any sentences.
- `NORMAL`   → print the organ heading + normal sentence block.
- `ABNORMAL` → print the organ heading + abnormal sentence(s) generated from selected abnormal states.

### Default print modes
- Liver: `NORMAL`
- Gallbladder: `NORMAL`
- **CBD: `SKIP` (default no-print)**
- **Pancreas: `SKIP` (default no-print)**
- Spleen: `NORMAL`
- Right Kidney: `NORMAL`
- Left Kidney: `NORMAL`
- Urinary Bladder: `NORMAL`

---

## 5) Data Model (v1 abdomen)

### 5.1 Patient
- `name` (required)
- `ageYears` (required)
- `sex` = Male/Female (required)
- `dateTime` (auto; editable optional)

### 5.2 Findings (per-organ)

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
- `renalCystRight` / `renalCystLeft`: boolean
- `renalCystRightSizeMm` / `renalCystLeftSizeMm`: int? (enabled/visible only if renalCyst = true)

> Removed: "small kidney" is NOT part of v1.

#### Urinary Bladder
- `bladderPrintMode`: SKIP/NORMAL/ABNORMAL

---

## 6) Deterministic Report Logic (must be followed)

### 6.1 General rules
1) **No AI generation**. Output is assembled from fixed sentence blocks + measurements inserted into placeholders.
2) For any organ in `SKIP` → print nothing for that organ.
3) For any organ in `NORMAL` → print exactly the normal sentence block for that organ.
4) For any organ in `ABNORMAL` → print the abnormal sentence(s) relevant to selected states.
5) Impression is **auto-built** from abnormal selections only.

### 6.2 Liver rules (includes Ascites)
- If Liver is `NORMAL`:
  - Print normal liver sentence.
  - Print: "No free fluid is seen." (because ascites is nested here, and default is none).
- If Liver is `ABNORMAL`:
  - Print fatty liver/hepatomegaly/CLD sentences as selected.
  - Ascites:
    - none → do not print ascites sentence
    - mild/moderate/gross → print the corresponding ascites sentence

### 6.3 Gallbladder rules (gallstone measurement)
- If gallstones = false → normal GB sentence applies (when gbPrintMode is NORMAL).
- If gbPrintMode = ABNORMAL and gallstones = true:
  - If gallstoneSizeMm is provided → print cholelithiasis sentence including **largest stone size in mm**.
  - If gallstoneSizeMm is missing → print cholelithiasis sentence without size.

### 6.4 CBD & Pancreas defaults
- Both default to `SKIP`.
- If user changes to NORMAL → print their normal sentence.
- If user changes to ABNORMAL → print abnormal sentence(s) (can be minimal in v1).

### 6.5 Spleen rules (splenomegaly size entry)
- If splenomegaly = true → **spleenSizeCm input must be shown**.
- When splenomegaly = true:
  - If spleenSizeCm provided → print splenomegaly sentence including size.
  - If spleenSizeCm missing → print splenomegaly sentence without size.

### 6.6 Kidney rules (CMD included)
- In NORMAL mode for each kidney, the normal sentence MUST include **corticomedullary differentiation**.
  - Default CMD in normal = preserved.
- In ABNORMAL mode, CMD may be:
  - preserved (print as preserved)
  - reduced (print as reduced/poor)
- Renal cyst:
  - If renalCyst = true → show cyst size field.
  - If size provided → print cyst sentence including size.
  - If size missing → print cyst sentence without size.

### 6.7 Urinary bladder (normal sentence)
- In NORMAL mode print exactly:
  - "Urinary bladder is adequately distended with normal wall thickness. No intraluminal lesion."

---

## 7) Impression (deterministic)
- Normal study (no abnormal selections anywhere) →
  - "Normal abdominal ultrasound." (GREEN)
- If any abnormal selection exists → impression becomes a **list of abnormal items** (RED), one per line.

---

## 8) Acceptance Tests (must pass)
A) Normal study (all defaults, no abnormalities) → green impression: "Normal abdominal ultrasound."  
   - CBD and Pancreas do not print by default.

B) Surayya 50F → fatty liver grade 1 + bilateral mild hydronephrosis; rest normal.

C) Fayyaz 32M → fatty liver grade 1 + left moderate hydronephrosis + 6 mm left renal pelvis stone + obstruction not seen.


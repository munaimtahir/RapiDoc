# RapiDoc USG (MVP) — Locked Spec (Ultrasound Abdomen)

## 1) Objective
Generate **print-ready PDF ultrasound abdomen reports** in a fixed AlShifa PolyClinic template.
Target: <10 seconds per report.

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

## 3) Inputs (v1 abdomen)
### Patient
- name (required)
- ageYears (required)
- sex: Male/Female (required)
- dateTime: auto; editable optional

### Findings
#### Liver
- fattyGrade: 0/1/2/3
- hepatomegaly: none/mild/moderate
- cld: boolean

#### Gallbladder
- gallstones: boolean

#### Kidneys/Urinary
- hydronephrosisLeft: none/mild/moderate/severe
- hydronephrosisRight: none/mild/moderate/severe
- obstruction: unset / notSeen / suspected
- stoneLeftMm: int?
- stoneRightMm: int?

#### Ascites
- none/mild/moderate/gross

## 4) Report logic (deterministic)
- Default: all organs normal sentences.
- If abnormal selection exists for an organ, print preset abnormal sentence(s) + still include “no focal lesion/biliary dilatation” where applicable.

## 5) Acceptance tests (must pass)
A) Normal study → green impression: "Normal abdominal ultrasound."
B) Surayya 50F → fatty liver grade 1 + bilateral mild hydronephrosis; rest normal.
C) Fayyaz 32M → fatty liver grade 1 + left moderate hydronephrosis + 6 mm left renal pelvis stone + obstruction not seen.

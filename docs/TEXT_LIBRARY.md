# v1.1 Text Library (Preset Sentences)

These sentences are deterministic blocks. No AI generation in this app.

---

## 1. SHARED ABBREVIATIONS
- `{X}` = integer for standard measurements in mm
- `{Y}` = integer for secondary measurements in cm, cc, or bpm
- `{LOC}` = location enum value
- `{gaWeeks}`, `{gaDays}` = integers for gestational age

---

## 2. ABDOMEN (Existing)

### Liver (includes Ascites)
- Normal: Liver is normal in size with normal echotexture. No focal lesion or intrahepatic biliary dilatation.
- Fatty Grade I: Liver shows mildly increased echogenicity consistent with Grade I fatty liver. No focal lesion or intrahepatic biliary dilatation.
- Fatty Grade II: Liver shows moderately increased echogenicity consistent with Grade II fatty liver. No focal lesion or intrahepatic biliary dilatation.
- Fatty Grade III: Liver shows markedly increased echogenicity consistent with Grade III fatty liver. No focal lesion or intrahepatic biliary dilatation.
- Hepatomegaly: Liver is enlarged in size (hepatomegaly) with preserved outline.
- CLD: Liver shows coarse echotexture with irregular margins suggestive of chronic liver disease.
- Ascites Normal: No free fluid is seen.
- Ascites Mild: Free fluid (ascites) is seen in abdomen, mild in amount.
- Ascites Moderate: Free fluid (ascites) is seen in abdomen, moderate in amount.
- Ascites Gross: Free fluid (ascites) is seen in abdomen, gross in amount.

### Gallbladder / CBD
- GB Normal: Gallbladder is normal with no calculi or pericholecystic fluid.
- Cholecystectomy: Gallbladder is not visualized, consistent with history of cholecystectomy.
- Gallstones Without size: Mobile echogenic foci with posterior acoustic shadowing are seen within gallbladder, consistent with cholelithiasis.
- Gallstones With size: Mobile echogenic foci with posterior acoustic shadowing are seen within gallbladder, consistent with cholelithiasis. Largest calculus measures {X} mm.
- CBD Normal: Common bile duct is normal in caliber.

### Pancreas / Spleen
- Pancreas Normal: Pancreas is normal in size and echotexture.
- Spleen Normal: Spleen is normal in size and echotexture.
- Splenomegaly Without size: Spleen is enlarged in size (splenomegaly).
- Splenomegaly With size: Spleen is enlarged measuring {Y} cm.

### Kidneys (Shared with KUB)
- Right Normal: Right kidney is normal in size with preserved corticomedullary differentiation. No hydronephrosis or calculus.
- Left Normal: Left kidney is normal in size with preserved corticomedullary differentiation. No hydronephrosis or calculus.
- Preserved CMD: Corticomedullary differentiation is preserved.
- Reduced CMD: Corticomedullary differentiation is reduced.
- Mild Hydro (Left/Right): {Side} kidney shows mild pelvicalyceal dilatation consistent with mild hydronephrosis.
- Moderate Hydro (Left/Right): {Side} kidney shows moderate pelvicalyceal dilatation consistent with moderate hydronephrosis.
- Severe Hydro (Left/Right): {Side} kidney shows severe pelvicalyceal dilatation consistent with severe hydronephrosis.
- Renal Stone (Left/Right): A {X} mm calculus is seen in the {Side} {LOC}.
- Renal Cyst Without Size: A simple renal cyst is seen.
- Renal Cyst With Size: A simple renal cyst is seen measuring {X} mm.
- Obstruction Not seen: No sonographic evidence of obstruction.
- Obstruction Suspected: Obstruction is suspected. Clinical correlation is advised.

### Urinary Bladder (Simple - Abdomen & Pelvis)
- Normal: Urinary bladder is adequately distended with normal wall thickness. No intraluminal lesion.

### Impression (Abdomen)
- Normal: Normal abdominal ultrasound. (GREEN)
- Abnormal format: List abnormal attributes separated by lines. (RED)

---

## 3. KUB / RENAL TRACT (New)

### Kidneys
(Uses identical kidney sentences from Abdomen section above.)

### Urinary Bladder (Detailed for KUB)
- Normal: Urinary bladder is adequately distended with normal wall thickness. No intraluminal lesion.
- Thickened wall: Urinary bladder wall is thickened and irregular.
- Bladder stone without size: An echogenic calculus with posterior acoustic shadowing is seen within the urinary bladder lumen.
- Bladder stone with size: An echogenic calculus measuring {X} mm is seen within the urinary bladder lumen.
- PVR Significant: Significant post-void residual volume is noted.

### Prostate (Male)
- Normal: Prostate is normal in size and echotexture.
- Enlarged without volume: Prostate is enlarged in size.
- Enlarged with volume: Prostate is enlarged with an estimated volume of {Y} cc.

### Impression (KUB)
- Normal: Normal KUB ultrasound. (GREEN)
- Abnormal: e.g. "Thickened urinary bladder wall with {X} mm calculus." / "Prostatomegaly ({Y} cc)." (RED)

---

## 4. PELVIS (FEMALE) (New)

### Uterus
- Normal: Uterus is anteverted, normal in size and echotexture. No focal myometrial lesion seen.
- Bulky: Uterus is bulky in size with homogenous echotexture.
- Fibroid Without Size: A well-defined hypoechoic solid lesion consistent with a fibroid is seen in the myometrium.
- Fibroid With Size: A well-defined hypoechoic solid lesion measuring {X} mm consistent with a fibroid is seen in the myometrium.

### Endometrium
- Normal (no thickness provided): Endometrium appears normal.
- With thickness: Endometrial thickness is {X} mm.

### Ovaries
- Right Normal: Right ovary is normal in size and appearance.
- Left Normal: Left ovary is normal in size and appearance.
- Cyst (Right/Left) Without Size: A simple cystic lesion is seen in the {Side} ovary.
- Cyst (Right/Left) With Size: A simple cystic lesion measuring {X} mm is seen in the {Side} ovary.

### Adnexa / Pouch of Douglas
- Normal: No adnexal mass or free fluid seen in the Pouch of Douglas.
- Free fluid (Mild/Moderate): {Severity} free fluid is seen in the Pouch of Douglas.

### Urinary Bladder 
(Uses Simple Urinary Bladder from Abdomen section)

### Impression (Pelvis)
- Normal: Normal female pelvic ultrasound. (GREEN)
- Abnormal Items: e.g., "Bulky uterus with {X} mm fibroid." / "Right ovarian cyst measuring {X} mm." (RED)

---

## 5. OBSTETRIC (New)

### Pregnancy Status / Cardiac Activity
- Single Live IUP: A single live intrauterine gestation is seen.
- Intrauterine pregnancy without cardiac activity: A single intrauterine gestational sac is seen. Fetal cardiac pole is identified but no cardiac activity is appreciated.
- Empty Sac: An empty intrauterine gestational sac is seen without a definite fetal pole or yolk sac. 
- FHR Present: Fetal heart rate is regular at {Y} bpm.
- FHR Absent: Fetal heart activity is absent. 

### Gestational Age
- Dating: Fetal biometry corresponds to a mean gestational age of {gaWeeks} weeks and {gaDays} days.

### Presentation
- Cephalic: Fetal presentation is cephalic.
- Breech: Fetal presentation is breech.
- Variable: Fetal lie is variable or unstable.
- Early: Fetal presentation cannot be determined at this early gestational age.

### Placenta
- Anterior/Posterior/Fundal/Low-lying: Placenta is {Location} in position.
- Not Visualized: Placenta is not yet completely formed/visualized.

### Amniotic Fluid (Liquor)
- Adequate: Amniotic fluid volume is adequate for gestational age.
- Reduced: Amniotic fluid volume is reduced (oligohydramnios).
- Increased: Amniotic fluid volume is increased (polyhydramnios).

### Cervix
- Closed: Internal cervical os is closed and cervix is adequate in length.
- Open/Short: Internal cervical os appears distinct or funneling is noted. Clinical correlation recommended.

### Impression (Obstetric)
- Normal: Single live intrauterine pregnancy of approximately {gaWeeks} weeks {gaDays} days gestation. (GREEN)
- Empty/Early Sac: Early/empty intrauterine gestational sac of {gaWeeks} weeks {gaDays} days. Follow-up is advised. (RED)
- Abnormal findings (appended as RED lines): e.g., "Absent fetal cardiac activity.", "Low-lying placenta.", "Oligohydramnios.", "Open/short cervix."

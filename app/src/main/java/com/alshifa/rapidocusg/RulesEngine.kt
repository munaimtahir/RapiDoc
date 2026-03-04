package com.alshifa.rapidocusg

object RulesEngine {

    fun buildReport(input: ReportInput): ReportBody {
        val f = input.findings
        val findings = mutableListOf<String>()

        // Liver
        if (f.fattyGrade == 0 && f.hepatomegaly == Hepatomegaly.NONE && !f.cld) {
            findings += "Liver is normal in size with normal echotexture. No focal lesion or intrahepatic biliary dilatation."
        } else {
            when (f.fattyGrade) {
                1 -> findings += "Liver shows mildly increased echogenicity consistent with Grade I fatty liver. No focal lesion or intrahepatic biliary dilatation."
                2 -> findings += "Liver shows moderately increased echogenicity consistent with Grade II fatty liver. No focal lesion or intrahepatic biliary dilatation."
                3 -> findings += "Liver shows markedly increased echogenicity consistent with Grade III fatty liver. No focal lesion or intrahepatic biliary dilatation."
            }
            if (f.hepatomegaly != Hepatomegaly.NONE) {
                findings += "Liver is enlarged in size (hepatomegaly) with preserved outline."
            }
            if (f.cld) {
                findings += "Liver shows coarse echotexture with irregular margins suggestive of chronic liver disease."
            }
        }

        // Gallbladder/CBD
        if (f.gallstones) {
            findings += "Mobile echogenic foci with posterior acoustic shadowing are seen within gallbladder, consistent with cholelithiasis."
            findings += "Common bile duct is normal in caliber."
        } else {
            findings += "Gallbladder is normal with no calculi or pericholecystic fluid."
            findings += "Common bile duct is normal in caliber."
        }

        // Pancreas/Spleen
        findings += "Pancreas is normal in size and echotexture."
        findings += "Spleen is normal in size and echotexture."

        // Kidneys
        appendKidneyFindings(
            side = "Right",
            hydronephrosis = f.hydronephrosisRight,
            stoneMm = f.stoneRightMm,
            findings = findings
        )
        appendKidneyFindings(
            side = "Left",
            hydronephrosis = f.hydronephrosisLeft,
            stoneMm = f.stoneLeftMm,
            findings = findings
        )

        if (f.obstruction == Obstruction.NOT_SEEN) {
            findings += "No sonographic evidence of obstruction."
        }
        if (f.obstruction == Obstruction.SUSPECTED) {
            findings += "Obstruction is suspected. Clinical correlation is advised."
        }

        findings += "Urinary bladder is adequately distended with normal wall thickness. No intraluminal lesion."

        findings += when (f.ascites) {
            Ascites.NONE -> "No free fluid is seen."
            Ascites.MILD -> "Free fluid (ascites) is seen in abdomen, mild in amount."
            Ascites.MODERATE -> "Free fluid (ascites) is seen in abdomen, moderate in amount."
            Ascites.GROSS -> "Free fluid (ascites) is seen in abdomen, gross in amount."
        }

        findings += "Abdominal aorta and IVC are normal in caliber where visualized."

        val impressions = buildImpression(f)
        val isNormal = impressions.size == 1 && impressions[0] == "Normal abdominal ultrasound."

        return ReportBody(
            findingsLines = findings,
            impressionLines = impressions,
            isNormal = isNormal
        )
    }

    private fun appendKidneyFindings(
        side: String,
        hydronephrosis: Hydronephrosis,
        stoneMm: String,
        findings: MutableList<String>
    ) {
        val mm = stoneMm.toIntOrNull()
        if (hydronephrosis == Hydronephrosis.NONE && mm == null) {
            findings += "$side kidney is normal in size with preserved corticomedullary differentiation. No hydronephrosis or calculus."
            return
        }

        if (hydronephrosis != Hydronephrosis.NONE) {
            val sev = when (hydronephrosis) {
                Hydronephrosis.MILD -> "mild"
                Hydronephrosis.MODERATE -> "moderate"
                Hydronephrosis.SEVERE -> "severe"
                Hydronephrosis.NONE -> ""
            }
            findings += "$side kidney shows $sev pelvicalyceal dilatation consistent with $sev hydronephrosis."
        }

        if (mm != null && mm > 0) {
            findings += "A $mm mm calculus is seen in the ${side.lowercase()} renal pelvis."
        }
    }

    private fun buildImpression(f: FindingsInput): List<String> {
        val items = mutableListOf<String>()

        when (f.fattyGrade) {
            1 -> items += "Grade I fatty liver."
            2 -> items += "Grade II fatty liver."
            3 -> items += "Grade III fatty liver."
        }
        if (f.hepatomegaly != Hepatomegaly.NONE) items += "Hepatomegaly."
        if (f.cld) items += "Features suggestive of chronic liver disease."
        if (f.gallstones) items += "Cholelithiasis."

        val stoneLeft = f.stoneLeftMm.toIntOrNull()
        val stoneRight = f.stoneRightMm.toIntOrNull()

        val bilateralSameSeverity =
            f.hydronephrosisLeft != Hydronephrosis.NONE &&
                f.hydronephrosisLeft == f.hydronephrosisRight

        if (bilateralSameSeverity) {
            val sev = f.hydronephrosisLeft.name.lowercase().replaceFirstChar { it.titlecase() }
            items += "$sev bilateral hydronephrosis."
        } else {
            appendSideHydroWithStone(
                side = "left",
                hydro = f.hydronephrosisLeft,
                stoneMm = stoneLeft,
                items = items
            )
            appendSideHydroWithStone(
                side = "right",
                hydro = f.hydronephrosisRight,
                stoneMm = stoneRight,
                items = items
            )
        }

        if (!bilateralSameSeverity) {
            if (stoneLeft != null && stoneLeft > 0 && f.hydronephrosisLeft == Hydronephrosis.NONE) {
                items += "$stoneLeft mm left renal pelvis calculus."
            }
            if (stoneRight != null && stoneRight > 0 && f.hydronephrosisRight == Hydronephrosis.NONE) {
                items += "$stoneRight mm right renal pelvis calculus."
            }
        }

        if (f.obstruction == Obstruction.NOT_SEEN) {
            items += "No sonographic evidence of obstruction."
        }
        if (f.obstruction == Obstruction.SUSPECTED) {
            items += "Obstruction is suspected. Clinical correlation is advised."
        }

        when (f.ascites) {
            Ascites.MILD -> items += "Mild ascites."
            Ascites.MODERATE -> items += "Moderate ascites."
            Ascites.GROSS -> items += "Gross ascites."
            Ascites.NONE -> Unit
        }

        return if (hasClinicalAbnormality(f)) items else listOf("Normal abdominal ultrasound.")
    }

    private fun appendSideHydroWithStone(
        side: String,
        hydro: Hydronephrosis,
        stoneMm: Int?,
        items: MutableList<String>
    ) {
        if (hydro == Hydronephrosis.NONE) return

        val sev = hydro.name.lowercase()
        if (stoneMm != null && stoneMm > 0) {
            items += "${sev.replaceFirstChar { it.titlecase() }} $side hydronephrosis with $stoneMm mm $side renal pelvis calculus."
        } else {
            items += "${sev.replaceFirstChar { it.titlecase() }} $side hydronephrosis."
        }
    }

    private fun hasClinicalAbnormality(f: FindingsInput): Boolean {
        return f.fattyGrade > 0 ||
            f.hepatomegaly != Hepatomegaly.NONE ||
            f.cld ||
            f.gallstones ||
            f.hydronephrosisLeft != Hydronephrosis.NONE ||
            f.hydronephrosisRight != Hydronephrosis.NONE ||
            (f.stoneLeftMm.toIntOrNull() ?: 0) > 0 ||
            (f.stoneRightMm.toIntOrNull() ?: 0) > 0 ||
            f.ascites != Ascites.NONE ||
            f.obstruction == Obstruction.SUSPECTED
    }
}

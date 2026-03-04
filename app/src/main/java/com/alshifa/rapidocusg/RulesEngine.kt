package com.alshifa.rapidocusg

object RulesEngine {

    fun buildReport(input: ReportInput): ReportBody {
        val f = input.findings
        val findings = mutableListOf<String>()

        // Liver (includes Ascites)
        if (f.liverPrintMode == OrganPrintMode.NORMAL) {
            findings += "Liver is normal in size with normal echotexture. No focal lesion or intrahepatic biliary dilatation."
            findings += "No free fluid is seen."
        } else if (f.liverPrintMode == OrganPrintMode.ABNORMAL) {
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
            when (f.ascites) {
                Ascites.MILD -> findings += "Free fluid (ascites) is seen in abdomen, mild in amount."
                Ascites.MODERATE -> findings += "Free fluid (ascites) is seen in abdomen, moderate in amount."
                Ascites.GROSS -> findings += "Free fluid (ascites) is seen in abdomen, gross in amount."
                Ascites.NONE -> Unit
            }
        }

        // Gallbladder
        if (f.gbPrintMode == OrganPrintMode.NORMAL) {
            findings += "Gallbladder is normal with no calculi or pericholecystic fluid."
        } else if (f.gbPrintMode == OrganPrintMode.ABNORMAL) {
            if (f.gallstones) {
                val mm = f.gallstoneSizeMm.toIntOrNull()
                if (mm != null && mm > 0) {
                    findings += "Mobile echogenic foci with posterior acoustic shadowing are seen within gallbladder, consistent with cholelithiasis. Largest calculus measures $mm mm."
                } else {
                    findings += "Mobile echogenic foci with posterior acoustic shadowing are seen within gallbladder, consistent with cholelithiasis."
                }
            }
        }

        // CBD
        if (f.cbdPrintMode == OrganPrintMode.NORMAL) {
            findings += "Common bile duct is normal in caliber."
        } else if (f.cbdPrintMode == OrganPrintMode.ABNORMAL) {
            findings += "Common bile duct appears abnormal." // Placeholder for ABNORMAL CBD
        }

        // Pancreas
        if (f.pancreasPrintMode == OrganPrintMode.NORMAL) {
            findings += "Pancreas is normal in size and echotexture."
        } else if (f.pancreasPrintMode == OrganPrintMode.ABNORMAL) {
            findings += "Pancreas appears abnormal." // Placeholder for ABNORMAL Pancreas
        }

        // Spleen
        if (f.spleenPrintMode == OrganPrintMode.NORMAL) {
            findings += "Spleen is normal in size and echotexture."
        } else if (f.spleenPrintMode == OrganPrintMode.ABNORMAL) {
            if (f.splenomegaly) {
                val cm = f.spleenSizeCm.toFloatOrNull()
                if (cm != null && cm > 0f) {
                    // removing trailing zero if it exists for whole numbers but let's just use string
                    val sizeStr = if (cm % 1 == 0f) cm.toInt().toString() else cm.toString()
                    findings += "Spleen is enlarged measuring $sizeStr cm."
                } else {
                    findings += "Spleen is enlarged in size (splenomegaly)."
                }
            }
        }

        // Right Kidney
        appendKidneyFindings(
            side = "Right",
            mode = f.rkPrintMode,
            cmdState = f.rkCmd,
            hydronephrosis = f.hydronephrosisRight,
            stoneMm = f.stoneRightMm,
            renalCyst = f.renalCystRight,
            cystSizeMm = f.renalCystRightSizeMm,
            findings = findings
        )

        // Left Kidney
        appendKidneyFindings(
            side = "Left",
            mode = f.lkPrintMode,
            cmdState = f.lkCmd,
            hydronephrosis = f.hydronephrosisLeft,
            stoneMm = f.stoneLeftMm,
            renalCyst = f.renalCystLeft,
            cystSizeMm = f.renalCystLeftSizeMm,
            findings = findings
        )

        if (f.rkPrintMode != OrganPrintMode.SKIP || f.lkPrintMode != OrganPrintMode.SKIP) {
            if (f.obstruction == Obstruction.NOT_SEEN) {
                findings += "No sonographic evidence of obstruction."
            }
            if (f.obstruction == Obstruction.SUSPECTED) {
                findings += "Obstruction is suspected. Clinical correlation is advised."
            }
        }

        // Urinary Bladder
        if (f.bladderPrintMode == OrganPrintMode.NORMAL) {
            findings += "Urinary bladder is adequately distended with normal wall thickness. No intraluminal lesion."
        } else if (f.bladderPrintMode == OrganPrintMode.ABNORMAL) {
            findings += "Urinary bladder appears abnormal." // Placeholder
        }

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
        mode: OrganPrintMode,
        cmdState: CmdState,
        hydronephrosis: Hydronephrosis,
        stoneMm: String,
        renalCyst: Boolean,
        cystSizeMm: String,
        findings: MutableList<String>
    ) {
        if (mode == OrganPrintMode.SKIP) return
        
        if (mode == OrganPrintMode.NORMAL) {
            findings += "$side kidney is normal in size with preserved corticomedullary differentiation. No hydronephrosis or calculus."
            return
        }

        if (mode == OrganPrintMode.ABNORMAL) {
            if (cmdState == CmdState.PRESERVED) {
                findings += "Corticomedullary differentiation is preserved."
            } else {
                findings += "Corticomedullary differentiation is reduced."
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

            val st = stoneMm.toIntOrNull()
            if (st != null && st > 0) {
                findings += "A $st mm calculus is seen in the ${side.lowercase()} renal pelvis."
            }

            if (renalCyst) {
                val cystMm = cystSizeMm.toIntOrNull()
                if (cystMm != null && cystMm > 0) {
                    findings += "A simple renal cyst is seen measuring $cystMm mm."
                } else {
                    findings += "A simple renal cyst is seen."
                }
            }
        }
    }

    private fun buildImpression(f: FindingsInput): List<String> {
        val items = mutableListOf<String>()

        if (f.liverPrintMode == OrganPrintMode.ABNORMAL) {
            when (f.fattyGrade) {
                1 -> items += "Grade I fatty liver."
                2 -> items += "Grade II fatty liver."
                3 -> items += "Grade III fatty liver."
            }
            if (f.hepatomegaly != Hepatomegaly.NONE) items += "Hepatomegaly."
            if (f.cld) items += "Features suggestive of chronic liver disease."
            when (f.ascites) {
                Ascites.MILD -> items += "Mild ascites."
                Ascites.MODERATE -> items += "Moderate ascites."
                Ascites.GROSS -> items += "Gross ascites."
                Ascites.NONE -> Unit
            }
        }

        if (f.gbPrintMode == OrganPrintMode.ABNORMAL && f.gallstones) {
            val mm = f.gallstoneSizeMm.toIntOrNull()
            if (mm != null && mm > 0) {
                items += "Cholelithiasis with $mm mm largest calculus."
            } else {
                items += "Cholelithiasis."
            }
        }

        if (f.cbdPrintMode == OrganPrintMode.ABNORMAL) {
            items += "Abnormal CBD."
        }

        if (f.pancreasPrintMode == OrganPrintMode.ABNORMAL) {
            items += "Abnormal pancreas."
        }

        if (f.spleenPrintMode == OrganPrintMode.ABNORMAL && f.splenomegaly) {
            val cm = f.spleenSizeCm.toFloatOrNull()
            if (cm != null && cm > 0f) {
                val sizeStr = if (cm % 1 == 0f) cm.toInt().toString() else cm.toString()
                items += "Splenomegaly ($sizeStr cm)."
            } else {
                items += "Splenomegaly."
            }
        }

        val stoneLeft = f.stoneLeftMm.toIntOrNull()
        val stoneRight = f.stoneRightMm.toIntOrNull()

        if (f.lkPrintMode == OrganPrintMode.ABNORMAL && f.lkCmd == CmdState.REDUCED) {
            items += "Reduced corticomedullary differentiation in left kidney."
        }
        if (f.rkPrintMode == OrganPrintMode.ABNORMAL && f.rkCmd == CmdState.REDUCED) {
            items += "Reduced corticomedullary differentiation in right kidney."
        }

        val lkHydro = if (f.lkPrintMode == OrganPrintMode.ABNORMAL) f.hydronephrosisLeft else Hydronephrosis.NONE
        val rkHydro = if (f.rkPrintMode == OrganPrintMode.ABNORMAL) f.hydronephrosisRight else Hydronephrosis.NONE
        val lkStone = if (f.lkPrintMode == OrganPrintMode.ABNORMAL) stoneLeft else null
        val rkStone = if (f.rkPrintMode == OrganPrintMode.ABNORMAL) stoneRight else null

        val bilateralSameSeverity = lkHydro != Hydronephrosis.NONE && lkHydro == rkHydro

        if (bilateralSameSeverity) {
            val sev = lkHydro.name.lowercase().replaceFirstChar { it.titlecase() }
            items += "$sev bilateral hydronephrosis."
        } else {
            appendSideHydroWithStone(side = "left", hydro = lkHydro, stoneMm = lkStone, items = items)
            appendSideHydroWithStone(side = "right", hydro = rkHydro, stoneMm = rkStone, items = items)
        }

        if (!bilateralSameSeverity) {
            if (lkStone != null && lkStone > 0 && lkHydro == Hydronephrosis.NONE) {
                items += "$lkStone mm left renal pelvis calculus."
            }
            if (rkStone != null && rkStone > 0 && rkHydro == Hydronephrosis.NONE) {
                items += "$rkStone mm right renal pelvis calculus."
            }
        }

        if (f.lkPrintMode == OrganPrintMode.ABNORMAL && f.renalCystLeft) {
            val cystMm = f.renalCystLeftSizeMm.toIntOrNull()
            if (cystMm != null && cystMm > 0) items += "Simple cyst in left kidney ($cystMm mm)."
            else items += "Simple cyst in left kidney."
        }

        if (f.rkPrintMode == OrganPrintMode.ABNORMAL && f.renalCystRight) {
            val cystMm = f.renalCystRightSizeMm.toIntOrNull()
            if (cystMm != null && cystMm > 0) items += "Simple cyst in right kidney ($cystMm mm)."
            else items += "Simple cyst in right kidney."
        }

        if (f.lkPrintMode != OrganPrintMode.SKIP || f.rkPrintMode != OrganPrintMode.SKIP) {
            if (f.obstruction == Obstruction.NOT_SEEN && items.any { it.contains("hydronephrosis") || it.contains("calculus") }) {
                items += "No sonographic evidence of obstruction."
            } else if (f.obstruction == Obstruction.SUSPECTED) {
                items += "Obstruction is suspected. Clinical correlation is advised."
            }
        }

        if (f.bladderPrintMode == OrganPrintMode.ABNORMAL) {
            items += "Abnormal urinary bladder."
        }

        return if (items.isNotEmpty()) items else listOf("Normal abdominal ultrasound.")
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
}

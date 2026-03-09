package com.alshifa.rapidocusg

object KubRulesEngine {

    fun buildReport(input: KubReportInput): ReportBody {
        val f = input.findings
        val findings = mutableListOf<String>()

        // Right Kidney
        appendKidneyFindings(
            side = "Right",
            mode = f.rkPrintMode,
            cmdState = f.rkCmd,
            hydronephrosis = f.hydronephrosisRight,
            stonePresent = f.stoneRightPresent,
            stoneMm = f.stoneRightMm,
            stoneLoc = f.stoneRightLocation,
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
            stonePresent = f.stoneLeftPresent,
            stoneMm = f.stoneLeftMm,
            stoneLoc = f.stoneLeftLocation,
            renalCyst = f.renalCystLeft,
            cystSizeMm = f.renalCystLeftSizeMm,
            findings = findings
        )

        if (f.rkPrintMode != OrganPrintMode.SKIP || f.lkPrintMode != OrganPrintMode.SKIP) {
            if (f.obstruction == Obstruction.NOT_SEEN) {
                findings += "No sonographic evidence of obstruction."
            } else if (f.obstruction == Obstruction.SUSPECTED) {
                findings += "Obstruction is suspected. Clinical correlation is advised."
            }
        }

        // Urinary Bladder
        if (f.bladderPrintMode == OrganPrintMode.NORMAL) {
            findings += "Urinary bladder is adequately distended with normal wall thickness. No intraluminal lesion."
        } else if (f.bladderPrintMode == OrganPrintMode.ABNORMAL) {
            if (f.bladderWallStatus == BladderWallStatus.THICKENED) {
                findings += "Urinary bladder wall is thickened and irregular."
            }
            if (f.bladderStone) {
                val stoneMm = f.bladderStoneSizeMm.toIntOrNull()
                if (stoneMm != null && stoneMm > 0) {
                    findings += "An echogenic calculus measuring $stoneMm mm is seen within the urinary bladder lumen."
                } else {
                    findings += "An echogenic calculus with posterior acoustic shadowing is seen within the urinary bladder lumen."
                }
            }
            if (f.postVoidResidual == PostVoidResidual.SIGNIFICANT) {
                findings += "Significant post-void residual volume is noted."
            }
        }

        // Prostate
        if (f.prostatePrintMode == OrganPrintMode.NORMAL) {
            findings += "Prostate is normal in size and echotexture."
        } else if (f.prostatePrintMode == OrganPrintMode.ABNORMAL) {
            if (f.prostateEnlarged) {
                val volCc = f.prostateVolCc.toIntOrNull()
                if (volCc != null && volCc > 0) {
                    findings += "Prostate is enlarged with an estimated volume of $volCc cc."
                } else {
                    findings += "Prostate is enlarged in size."
                }
            }
        }

        val impressions = buildImpression(f)
        val isNormal = impressions.size == 1 && impressions[0] == "Normal KUB ultrasound."

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
        stonePresent: Boolean,
        stoneMm: String,
        stoneLoc: StoneLocation?,
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

            if (stonePresent) {
                val st = stoneMm.toIntOrNull()
                if (st != null && st > 0 && stoneLoc != null) {
                    val locRepr = when (stoneLoc) {
                        StoneLocation.UPPER_CALYX -> "upper calyx (upper pole)"
                        StoneLocation.MID_CALYX -> "mid calyx (mid pole)"
                        StoneLocation.LOWER_CALYX -> "lower calyx (lower pole)"
                        StoneLocation.RENAL_PELVIS -> "renal pelvis"
                        StoneLocation.PUJ -> "pelvi-ureteric junction (PUJ/UPJ)"
                    }
                    findings += "A $st mm calculus is seen in the ${side.lowercase()} $locRepr."
                }
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

    private fun buildImpression(f: KubFindingsInput): List<String> {
        val items = mutableListOf<String>()

        val lkHydro = if (f.lkPrintMode == OrganPrintMode.ABNORMAL) f.hydronephrosisLeft else Hydronephrosis.NONE
        val rkHydro = if (f.rkPrintMode == OrganPrintMode.ABNORMAL) f.hydronephrosisRight else Hydronephrosis.NONE
        val lkStone = f.stoneLeftMm.toIntOrNull()
        val rkStone = f.stoneRightMm.toIntOrNull()
        val lkHasStone = f.lkPrintMode == OrganPrintMode.ABNORMAL && f.stoneLeftPresent && lkStone != null && lkStone > 0 && f.stoneLeftLocation != null
        val rkHasStone = f.rkPrintMode == OrganPrintMode.ABNORMAL && f.stoneRightPresent && rkStone != null && rkStone > 0 && f.stoneRightLocation != null

        if (f.lkPrintMode == OrganPrintMode.ABNORMAL && f.lkCmd == CmdState.REDUCED) {
            items += "Reduced corticomedullary differentiation in left kidney."
        }
        if (f.rkPrintMode == OrganPrintMode.ABNORMAL && f.rkCmd == CmdState.REDUCED) {
            items += "Reduced corticomedullary differentiation in right kidney."
        }

        val bilateralSameSeverity = lkHydro != Hydronephrosis.NONE && lkHydro == rkHydro

        fun stoneLocRepr(loc: StoneLocation): String = 
            when (loc) {
                StoneLocation.UPPER_CALYX -> "upper calyx (upper pole)"
                StoneLocation.MID_CALYX -> "mid calyx (mid pole)"
                StoneLocation.LOWER_CALYX -> "lower calyx (lower pole)"
                StoneLocation.RENAL_PELVIS -> "renal pelvis"
                StoneLocation.PUJ -> "pelvi-ureteric junction (PUJ/UPJ)"
            }

        if (bilateralSameSeverity) {
            val sev = lkHydro.name.lowercase().replaceFirstChar { it.titlecase() }
            items += "$sev bilateral hydronephrosis."
        } else {
            appendSideHydroWithStone(side = "left", hydro = lkHydro, hasStone = lkHasStone, stoneMm = lkStone, stoneLoc = f.stoneLeftLocation, items = items)
            appendSideHydroWithStone(side = "right", hydro = rkHydro, hasStone = rkHasStone, stoneMm = rkStone, stoneLoc = f.stoneRightLocation, items = items)
        }

        if (!bilateralSameSeverity) {
             if (lkHasStone && lkHydro == Hydronephrosis.NONE) {
                items += "Left renal calculus ($lkStone mm) in ${stoneLocRepr(f.stoneLeftLocation!!)}."
            }
            if (rkHasStone && rkHydro == Hydronephrosis.NONE) {
                items += "Right renal calculus ($rkStone mm) in ${stoneLocRepr(f.stoneRightLocation!!)}."
            }
        } else {
            if (lkHasStone) {
                items += "Left renal calculus ($lkStone mm) in ${stoneLocRepr(f.stoneLeftLocation!!)}."
            }
            if (rkHasStone) {
                items += "Right renal calculus ($rkStone mm) in ${stoneLocRepr(f.stoneRightLocation!!)}."
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
            val bladderItems = mutableListOf<String>()
            if (f.bladderWallStatus == BladderWallStatus.THICKENED) bladderItems += "Thickened urinary bladder wall"
            if (f.bladderStone) {
                val stoneMm = f.bladderStoneSizeMm.toIntOrNull()
                if (stoneMm != null && stoneMm > 0) {
                    bladderItems += "with $stoneMm mm calculus"
                } else {
                    bladderItems += "with calculus"
                }
            }
            if (bladderItems.isNotEmpty()) {
                val line = bladderItems.joinToString(" ").replaceFirstChar { it.titlecase() }
                items += "$line."
            } else {
                items += "Abnormal urinary bladder."
            }
            if (f.postVoidResidual == PostVoidResidual.SIGNIFICANT) {
                items += "Significant post-void residual volume." // Added period optionally? The original text logic specifies line by line.
            }
        }

        if (f.prostatePrintMode == OrganPrintMode.ABNORMAL && f.prostateEnlarged) {
             val volCc = f.prostateVolCc.toIntOrNull()
             if (volCc != null && volCc > 0) {
                 items += "Prostatomegaly ($volCc cc)."
             } else {
                 items += "Prostatomegaly."
             }
        }

        return if (items.isNotEmpty()) items else listOf("Normal KUB ultrasound.")
    }

    private fun appendSideHydroWithStone(
        side: String,
        hydro: Hydronephrosis,
        hasStone: Boolean,
        stoneMm: Int?,
        stoneLoc: StoneLocation?,
        items: MutableList<String>
    ) {
        if (hydro == Hydronephrosis.NONE) return

        val sev = hydro.name.lowercase()
        if (hasStone && stoneMm != null && stoneLoc != null) {
             val locRepr = when (stoneLoc) {
                StoneLocation.UPPER_CALYX -> "upper calyx (upper pole)"
                StoneLocation.MID_CALYX -> "mid calyx (mid pole)"
                StoneLocation.LOWER_CALYX -> "lower calyx (lower pole)"
                StoneLocation.RENAL_PELVIS -> "renal pelvis"
                StoneLocation.PUJ -> "pelvi-ureteric junction (PUJ/UPJ)"
            }
            items += "${sev.replaceFirstChar { it.titlecase() }} $side hydronephrosis with ${side.lowercase()} renal calculus ($stoneMm mm) in $locRepr."
        } else {
            items += "${sev.replaceFirstChar { it.titlecase() }} $side hydronephrosis."
        }
    }
}

package com.alshifa.rapidocusg

object PelvisRulesEngine {

    fun buildReport(input: PelvisReportInput): ReportBody {
        val f = input.findings
        val findings = mutableListOf<String>()

        // Uterus
        if (f.uterusPrintMode == OrganPrintMode.NORMAL) {
            findings += "Uterus is anteverted, normal in size and echotexture. No focal myometrial lesion seen."
        } else if (f.uterusPrintMode == OrganPrintMode.ABNORMAL) {
            if (f.uterusStatus == UterusStatus.BULKY) {
                findings += "Uterus is bulky in size with homogenous echotexture."
            }
            if (f.fibroid) {
                val sizeMm = f.fibroidSizeMm.toIntOrNull()
                if (sizeMm != null && sizeMm > 0) {
                    findings += "A well-defined hypoechoic solid lesion measuring $sizeMm mm consistent with a fibroid is seen in the myometrium."
                } else {
                    findings += "A well-defined hypoechoic solid lesion consistent with a fibroid is seen in the myometrium."
                }
            }
        }

        // Endometrium
        if (f.endoPrintMode == OrganPrintMode.NORMAL) {
            val thMm = f.endoThicknessMm.toIntOrNull()
            if (thMm != null && thMm > 0) {
                findings += "Endometrial thickness is $thMm mm."
            } else {
                findings += "Endometrium appears normal."
            }
        } else if (f.endoPrintMode == OrganPrintMode.ABNORMAL) {
            val thMm = f.endoThicknessMm.toIntOrNull()
            if (thMm != null && thMm > 0) {
                findings += "Endometrial thickness is $thMm mm."
            } else {
                findings += "Endometrium appears abnormal."
            }
        }

        // Right Ovary
        if (f.roPrintMode == OrganPrintMode.NORMAL) {
            findings += "Right ovary is normal in size and appearance."
        } else if (f.roPrintMode == OrganPrintMode.ABNORMAL) {
            if (f.cystRight) {
                val mm = f.cystSizeRightMm.toIntOrNull()
                if (mm != null && mm > 0) {
                    findings += "A simple cystic lesion measuring $mm mm is seen in the right ovary."
                } else {
                    findings += "A simple cystic lesion is seen in the right ovary."
                }
            }
        }

        // Left Ovary
        if (f.loPrintMode == OrganPrintMode.NORMAL) {
            findings += "Left ovary is normal in size and appearance."
        } else if (f.loPrintMode == OrganPrintMode.ABNORMAL) {
            if (f.cystLeft) {
                val mm = f.cystSizeLeftMm.toIntOrNull()
                if (mm != null && mm > 0) {
                    findings += "A simple cystic lesion measuring $mm mm is seen in the left ovary."
                } else {
                    findings += "A simple cystic lesion is seen in the left ovary."
                }
            }
        }

        // Adnexa / Pouch of Douglas
        if (f.adnexaPrintMode == OrganPrintMode.NORMAL) {
            findings += "No adnexal mass or free fluid seen in the Pouch of Douglas."
        } else if (f.adnexaPrintMode == OrganPrintMode.ABNORMAL) {
            if (f.freeFluid != FreeFluid.NONE) {
                val sev = f.freeFluid.name.lowercase().replaceFirstChar { it.titlecase() }
                findings += "$sev free fluid is seen in the Pouch of Douglas."
            }
        }

        // Urinary Bladder
        if (f.bladderPrintMode == OrganPrintMode.NORMAL) {
            findings += "Urinary bladder is adequately distended with normal wall thickness. No intraluminal lesion."
        } else if (f.bladderPrintMode == OrganPrintMode.ABNORMAL) {
            findings += "Urinary bladder appears abnormal."
        }

        val impressions = buildImpression(f)
        val isNormal = impressions.size == 1 && impressions[0] == "Normal female pelvic ultrasound."

        return ReportBody(
            findingsLines = findings,
            impressionLines = impressions,
            isNormal = isNormal
        )
    }

    private fun buildImpression(f: PelvisFindingsInput): List<String> {
        val items = mutableListOf<String>()

        if (f.uterusPrintMode == OrganPrintMode.ABNORMAL) {
            val utItems = mutableListOf<String>()
            if (f.uterusStatus == UterusStatus.BULKY) utItems += "Bulky uterus"
            if (f.fibroid) {
                val mm = f.fibroidSizeMm.toIntOrNull()
                if (mm != null && mm > 0) utItems += "with $mm mm fibroid"
                else utItems += "with fibroid"
            }
            if (utItems.isNotEmpty()) {
                val line = utItems.joinToString(" ").replaceFirstChar { it.titlecase() }
                items += "$line."
            }
        }

        if (f.roPrintMode == OrganPrintMode.ABNORMAL && f.cystRight) {
            val mm = f.cystSizeRightMm.toIntOrNull()
            if (mm != null && mm > 0) {
                items += "Right ovarian cyst measuring $mm mm."
            } else {
                items += "Right ovarian cyst."
            }
        }

        if (f.loPrintMode == OrganPrintMode.ABNORMAL && f.cystLeft) {
            val mm = f.cystSizeLeftMm.toIntOrNull()
            if (mm != null && mm > 0) {
                items += "Left ovarian cyst measuring $mm mm."
            } else {
                items += "Left ovarian cyst."
            }
        }

        if (f.adnexaPrintMode == OrganPrintMode.ABNORMAL && f.freeFluid != FreeFluid.NONE) {
            val sev = f.freeFluid.name.lowercase().replaceFirstChar { it.titlecase() }
            items += "$sev free fluid in Pouch of Douglas."
        }

        return if (items.isNotEmpty()) items else listOf("Normal female pelvic ultrasound.")
    }
}

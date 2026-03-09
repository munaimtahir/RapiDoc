package com.alshifa.rapidocusg

object ObstetricRulesEngine {

    fun buildReport(input: ObstetricReportInput): ReportBody {
        val f = input.findings
        val findings = mutableListOf<String>()

        // 1. Pregnancy Status / Cardiac Activity
        when (f.pregStatus) {
            PregStatus.LIVE_IUP -> {
                findings += "A single live intrauterine gestation is seen."
                if (f.fhrPresent) {
                    val bpm = f.fhrBpm.toIntOrNull()
                    if (bpm != null && bpm > 0) {
                        findings += "Fetal heart rate is regular at $bpm bpm."
                    } else {
                        findings += "Fetal heart rate is regular."
                    }
                } else {
                    findings += "Fetal heart activity is absent."
                }
            }
            PregStatus.IUP_NO_CARDIAC -> {
                findings += "A single intrauterine gestational sac is seen. Fetal cardiac pole is identified but no cardiac activity is appreciated."
            }
            PregStatus.EMPTY_SAC -> {
                findings += "An empty intrauterine gestational sac is seen without a definite fetal pole or yolk sac."
            }
        }

        // 2. Gestational Age
        val weeks = f.gaWeeks.toIntOrNull()
        val days = f.gaDays.toIntOrNull() ?: 0
        if (weeks != null && weeks > 0) {
            findings += "Fetal biometry corresponds to a mean gestational age of $weeks weeks and $days days."
        }

        // Only add presentation, placenta, liquor, cervix if not empty sac
        if (f.pregStatus != PregStatus.EMPTY_SAC) {
            // 3. Presentation
            val pres = when (f.presentation) {
                FetalPresentation.CEPHALIC -> "cephalic"
                FetalPresentation.BREECH -> "breech"
                FetalPresentation.VARIABLE -> "variable or unstable"
                FetalPresentation.EARLY -> "cannot be determined at this early gestational age"
            }
            if (f.presentation == FetalPresentation.EARLY || f.presentation == FetalPresentation.VARIABLE) {
                findings += "Fetal lie is $pres."
            } else {
                findings += "Fetal presentation is $pres."
            }

            // 4. Placenta
            val plac = when (f.placenta) {
                PlacentaLocation.ANTERIOR -> "anterior"
                PlacentaLocation.POSTERIOR -> "posterior"
                PlacentaLocation.FUNDAL -> "fundal"
                PlacentaLocation.LOW_LYING -> "low-lying"
                PlacentaLocation.NOT_VISUALIZED -> "not yet completely formed/visualized"
            }
            if (f.placenta == PlacentaLocation.NOT_VISUALIZED) {
                findings += "Placenta is $plac."
            } else {
                findings += "Placenta is $plac in position."
            }

            // 5. Liquor
            val liq = when (f.liquor) {
                LiquorVolume.ADEQUATE -> "adequate for gestational age"
                LiquorVolume.REDUCED -> "reduced (oligohydramnios)"
                LiquorVolume.INCREASED -> "increased (polyhydramnios)"
            }
            findings += "Amniotic fluid volume is $liq."
        }

        // 6. Cervix
        if (f.cervixClosed) {
            findings += "Internal cervical os is closed and cervix is adequate in length."
        } else {
            findings += "Internal cervical os appears distinct or funneling is noted. Clinical correlation recommended."
        }


        val impressions = buildImpression(f)
        // If 'Normal' it will just be size 1 and contain 'Single live intrauterine...'.
        val isNormal = impressions.size == 1 && impressions[0].startsWith("Single live intrauterine pregnancy of approximately")

        return ReportBody(
            findingsLines = findings,
            impressionLines = impressions,
            isNormal = isNormal
        )
    }

    private fun buildImpression(f: ObstetricFindingsInput): List<String> {
        val items = mutableListOf<String>()

        val weeks = f.gaWeeks.toIntOrNull() ?: 0
        val days = f.gaDays.toIntOrNull() ?: 0

        if (f.pregStatus == PregStatus.EMPTY_SAC) {
            items += "Early/empty intrauterine gestational sac of $weeks weeks $days days. Follow-up is advised."
        } else if (f.pregStatus == PregStatus.IUP_NO_CARDIAC) {
            items += "Single intrauterine pregnancy of approximately $weeks weeks $days days gestation."
            items += "Absent fetal cardiac activity."
        } else if (!f.fhrPresent) {
            // Technically a LIVE_IUP with fhr absent? The model shouldn't really allow but just in case
            items += "Single intrauterine pregnancy of approximately $weeks weeks $days days gestation."
            items += "Absent fetal cardiac activity."
        } else {
            // Live, FHR present
            val isNormal = f.placenta != PlacentaLocation.LOW_LYING && 
                           f.liquor == LiquorVolume.ADEQUATE && 
                           f.cervixClosed
            
            if (isNormal) {
                items += "Single live intrauterine pregnancy of approximately $weeks weeks $days days gestation."
            } else {
                items += "Single live intrauterine pregnancy of approximately $weeks weeks $days days gestation."
                
                if (f.placenta == PlacentaLocation.LOW_LYING) {
                    items += "Low-lying placenta."
                }
                if (f.liquor == LiquorVolume.REDUCED) {
                    items += "Oligohydramnios."
                }
                if (f.liquor == LiquorVolume.INCREASED) {
                    items += "Polyhydramnios."
                }
                if (!f.cervixClosed) {
                    items += "Open/short cervix."
                }
            }
        }

        return items
    }
}

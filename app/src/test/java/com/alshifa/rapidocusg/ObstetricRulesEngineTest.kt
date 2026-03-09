package com.alshifa.rapidocusg

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class ObstetricRulesEngineTest {

    private fun createBaseReport(): ObstetricReportInput {
        return ObstetricReportInput(
            patient = PatientInfo(name = "Test", ageYears = "30", sex = Sex.Female, bookingDateTime = LocalDateTime.now(), reportingDateTime = LocalDateTime.now()),
            findings = ObstetricFindingsInput() // Defaults to LIVE_IUP with normal parameters
        )
    }

    @Test
    fun `live pregnancy generates correct impression`() {
        val input = createBaseReport().copy(
            findings = ObstetricFindingsInput(
                pregStatus = PregStatus.LIVE_IUP,
                gaWeeks = "32"
            )
        )
        val result = ObstetricRulesEngine.buildReport(input)

        val impression = result.impressionLines.any { it.contains("Single live intrauterine pregnancy of approximately 32 weeks") }
        assertTrue("Expected Single live intrauterine pregnancy impression", impression)
    }

    @Test
    fun `empty gestational sac generates correct impression`() {
        val input = createBaseReport().copy(
            findings = ObstetricFindingsInput(
                pregStatus = PregStatus.EMPTY_SAC,
                gaWeeks = "6"
            )
        )
        val result = ObstetricRulesEngine.buildReport(input)

        val impression = result.impressionLines.any { it.contains("Early/empty intrauterine gestational sac of 6 weeks") }
        assertTrue("Expected Empty gestational sac impression", impression)
        
        // Findings should not contain placental or presentation info for empty sac
        val noPlacenta = result.findingsLines.none { it.contains("Placenta") }
        assertTrue("Expected no placental info for empty sac", noPlacenta)
    }
}

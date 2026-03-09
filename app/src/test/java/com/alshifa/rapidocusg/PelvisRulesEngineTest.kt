package com.alshifa.rapidocusg

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class PelvisRulesEngineTest {

    private fun createBaseReport(): PelvisReportInput {
        return PelvisReportInput(
            patient = PatientInfo(name = "Test", ageYears = "30", sex = Sex.Female, bookingDateTime = LocalDateTime.now(), reportingDateTime = LocalDateTime.now()),
            findings = PelvisFindingsInput(
                uterusPrintMode = OrganPrintMode.NORMAL,
                endoPrintMode = OrganPrintMode.NORMAL,
                roPrintMode = OrganPrintMode.NORMAL,
                loPrintMode = OrganPrintMode.NORMAL,
                adnexaPrintMode = OrganPrintMode.NORMAL,
                bladderPrintMode = OrganPrintMode.NORMAL
            )
        )
    }

    @Test
    fun `normal pelvis generates normal impression`() {
        val input = createBaseReport()
        val result = PelvisRulesEngine.buildReport(input)

        val isNormal = result.impressionLines.any { it.contains("Normal female pelvic ultrasound.") }
        assertTrue("Expected Normal female pelvic ultrasound impression", isNormal)
    }

    @Test
    fun `fibroid present in pelvis emits correct sentence and impression`() {
        val input = createBaseReport().copy(
            findings = PelvisFindingsInput(
                uterusPrintMode = OrganPrintMode.ABNORMAL,
                uterusStatus = UterusStatus.BULKY,
                fibroid = true,
                fibroidSizeMm = "45"
            )
        )
        val result = PelvisRulesEngine.buildReport(input)

        val finding = result.findingsLines.any { it.contains("fibroid is seen") }
        assertTrue("Expected fibroid finding", finding)

        val impressionBulky = result.impressionLines.any { it.contains("Bulky uterus") }
        val impressionFibroid = result.impressionLines.any { it.contains("with 45 mm fibroid") }
        assertTrue("Expected abnormal impression", impressionBulky || impressionFibroid)
        
        val isNormal = result.impressionLines.any { it.contains("Normal female pelvic ultrasound.") }
        assertFalse("Expected abnormal impression", isNormal)
    }
}

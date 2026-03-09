package com.alshifa.rapidocusg

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class KubRulesEngineTest {

    private fun createBaseReport(): KubReportInput {
        return KubReportInput(
            patient = PatientInfo(name = "Test", ageYears = "30", sex = Sex.Male, bookingDateTime = LocalDateTime.now(), reportingDateTime = LocalDateTime.now()),
            findings = KubFindingsInput(
                rkPrintMode = OrganPrintMode.NORMAL,
                lkPrintMode = OrganPrintMode.NORMAL,
                bladderPrintMode = OrganPrintMode.NORMAL,
                prostatePrintMode = OrganPrintMode.NORMAL
            )
        )
    }

    @Test
    fun `normal kub generates normal impression`() {
        val input = createBaseReport()
        val result = KubRulesEngine.buildReport(input)

        val isNormal = result.impressionLines.any { it.contains("Normal KUB ultrasound.") }
        assertTrue("Expected Normal KUB ultrasound impression", isNormal)
    }

    @Test
    fun `stone present in KUB emits correct sentence and impression`() {
        val input = createBaseReport().copy(
            findings = KubFindingsInput(
                rkPrintMode = OrganPrintMode.ABNORMAL,
                stoneRightPresent = true,
                stoneRightMm = "6",
                stoneRightLocation = StoneLocation.RENAL_PELVIS,
                lkPrintMode = OrganPrintMode.ABNORMAL,
                stoneLeftPresent = true,
                stoneLeftMm = "8",
                stoneLeftLocation = StoneLocation.UPPER_CALYX
            )
        )
        val result = KubRulesEngine.buildReport(input)

        val rkFinding = result.findingsLines.any { it.contains("A 6 mm calculus is seen in the right renal pelvis.") }
        assertTrue("Expected valid right stone finding", rkFinding)

        val rkImpression = result.impressionLines.any { it.contains("Right renal calculus (6 mm) in renal pelvis.") }
        assertTrue("Expected valid right stone impression", rkImpression)
        
        val isNormal = result.impressionLines.any { it.contains("Normal KUB ultrasound.") }
        assertFalse("Expected abnormal impression", isNormal)
    }
}

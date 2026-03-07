package com.alshifa.rapidocusg

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class RulesEngineTest {

    private fun createBaseReport(): ReportInput {
        return ReportInput(
            patient = PatientInfo(name = "Test", ageYears = "30", sex = Sex.Male, bookingDateTime = LocalDateTime.now(), reportingDateTime = LocalDateTime.now()),
            findings = FindingsInput(
                rkPrintMode = OrganPrintMode.ABNORMAL,
                lkPrintMode = OrganPrintMode.ABNORMAL
            )
        )
    }

    @Test
    fun `stone present but missing size emits no stone sentence`() {
        val input = createBaseReport().copy(
            findings = FindingsInput(
                rkPrintMode = OrganPrintMode.ABNORMAL,
                stoneRightPresent = true,
                stoneRightMm = "",
                stoneRightLocation = StoneLocation.LOWER_CALYX
            )
        )
        val result = RulesEngine.buildReport(input)
        
        // no incomplete stone sentence should be emitted
        val findingsFound = result.findingsLines.any { it.contains("calculus is seen") || it.contains("renal calculus") }
        assertFalse("Missing size should not generate a stone sentence", findingsFound)
        
        // impression excludes incomplete stone data
        val impressionFound = result.impressionLines.any { it.contains("calculus is seen") || it.contains("renal calculus") }
        assertFalse("Missing size should not generate a stone impression", impressionFound)
    }

    @Test
    fun `stone present but missing location emits no stone sentence`() {
        val input = createBaseReport().copy(
            findings = FindingsInput(
                lkPrintMode = OrganPrintMode.ABNORMAL,
                stoneLeftPresent = true,
                stoneLeftMm = "5",
                stoneLeftLocation = null
            )
        )
        val result = RulesEngine.buildReport(input)

        // no incomplete stone sentence should be emitted
        val findingsFound = result.findingsLines.any { it.contains("calculus is seen") || it.contains("renal calculus") }
        assertFalse("Missing location should not generate a stone sentence", findingsFound)
        
        // impression excludes incomplete stone data
        val impressionFound = result.impressionLines.any { it.contains("calculus is seen") || it.contains("renal calculus") }
        assertFalse("Missing location should not generate a stone impression", impressionFound)
    }

    @Test
    fun `stone present with size and location is valid and emits sentence`() {
        val input = createBaseReport().copy(
            findings = FindingsInput(
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
        val result = RulesEngine.buildReport(input)

        // findings string check
        val rkFinding = result.findingsLines.any { it.contains("A 6 mm calculus is seen in the right renal pelvis.") }
        assertTrue("Expected valid right stone finding", rkFinding)

        val lkFinding = result.findingsLines.any { it.contains("A 8 mm calculus is seen in the left upper calyx (upper pole).") }
        assertTrue("Expected valid left stone finding", lkFinding)

        // impression string check
        val rkImpression = result.impressionLines.any { it.contains("Right renal calculus (6 mm) in renal pelvis.") }
        assertTrue("Expected valid right stone impression", rkImpression)
        
        val lkImpression = result.impressionLines.any { it.contains("Left renal calculus (8 mm) in upper calyx (upper pole).") }
        assertTrue("Expected valid left stone impression", lkImpression)
    }
}

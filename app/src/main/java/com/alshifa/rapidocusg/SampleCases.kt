package com.alshifa.rapidocusg

import java.time.LocalDateTime

object SampleCases {

    fun normal(now: LocalDateTime = LocalDateTime.now()): ReportInput = ReportInput(
        patient = PatientInfo(
            name = "Normal Case",
            ageYears = "30",
            sex = Sex.Male,
            bookingDateTime = now,
            reportingDateTime = now
        ),
        findings = FindingsInput()
    )

    fun surayya(now: LocalDateTime = LocalDateTime.now()): ReportInput = ReportInput(
        patient = PatientInfo(
            name = "Surayya",
            ageYears = "50",
            sex = Sex.Female,
            bookingDateTime = now,
            reportingDateTime = now
        ),
        // fatty liver grade 1 + bilateral mild hydronephrosis
        findings = FindingsInput(
            liverPrintMode = OrganPrintMode.ABNORMAL,
            fattyGrade = 1,
            lkPrintMode = OrganPrintMode.ABNORMAL,
            hydronephrosisLeft = Hydronephrosis.MILD,
            rkPrintMode = OrganPrintMode.ABNORMAL,
            hydronephrosisRight = Hydronephrosis.MILD
        )
    )

    fun fayyaz(now: LocalDateTime = LocalDateTime.now()): ReportInput = ReportInput(
        patient = PatientInfo(
            name = "Fayyaz",
            ageYears = "32",
            sex = Sex.Male,
            bookingDateTime = now,
            reportingDateTime = now
        ),
        // fatty liver grade 1 + left moderate hydro + 6 mm left renal pelvis stone + obs not seen
        findings = FindingsInput(
            liverPrintMode = OrganPrintMode.ABNORMAL,
            fattyGrade = 1,
            lkPrintMode = OrganPrintMode.ABNORMAL,
            hydronephrosisLeft = Hydronephrosis.MODERATE,
            stoneLeftMm = "6",
            obstruction = Obstruction.NOT_SEEN
        )
    )
}

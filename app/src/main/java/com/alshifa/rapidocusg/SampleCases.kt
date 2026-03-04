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
        findings = FindingsInput(
            fattyGrade = 1,
            hydronephrosisLeft = Hydronephrosis.MILD,
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
        findings = FindingsInput(
            fattyGrade = 1,
            hydronephrosisLeft = Hydronephrosis.MODERATE,
            stoneLeftMm = "6",
            obstruction = Obstruction.NOT_SEEN
        )
    )
}

package com.alshifa.rapidocusg

import java.time.LocalDateTime

enum class Sex { Male, Female }

enum class Hepatomegaly { NONE, MILD, MODERATE }

enum class Hydronephrosis { NONE, MILD, MODERATE, SEVERE }

enum class Obstruction { UNSET, NOT_SEEN, SUSPECTED }

enum class Ascites { NONE, MILD, MODERATE, GROSS }

data class PatientInfo(
    val name: String = "",
    val ageYears: String = "",
    val sex: Sex = Sex.Male,
    val bookingDateTime: LocalDateTime = LocalDateTime.now(),
    val reportingDateTime: LocalDateTime = LocalDateTime.now()
)

data class FindingsInput(
    val fattyGrade: Int = 0,
    val hepatomegaly: Hepatomegaly = Hepatomegaly.NONE,
    val cld: Boolean = false,
    val gallstones: Boolean = false,
    val hydronephrosisLeft: Hydronephrosis = Hydronephrosis.NONE,
    val hydronephrosisRight: Hydronephrosis = Hydronephrosis.NONE,
    val obstruction: Obstruction = Obstruction.UNSET,
    val stoneLeftMm: String = "",
    val stoneRightMm: String = "",
    val ascites: Ascites = Ascites.NONE
)

data class ReportInput(
    val patient: PatientInfo,
    val findings: FindingsInput
)

data class ReportBody(
    val findingsLines: List<String>,
    val impressionLines: List<String>,
    val isNormal: Boolean
)

package com.alshifa.rapidocusg

import java.time.LocalDateTime

enum class Sex { UNSET, Male, Female }

enum class OrganPrintMode { SKIP, NORMAL, ABNORMAL }

enum class Hepatomegaly { NONE, MILD, MODERATE }

enum class Hydronephrosis { NONE, MILD, MODERATE, SEVERE }

enum class Obstruction { UNSET, NOT_SEEN, SUSPECTED }

enum class Ascites { NONE, MILD, MODERATE, GROSS }

enum class CmdState { PRESERVED, REDUCED }

enum class StoneLocation(val displayName: String) {
    UPPER_CALYX("Upper calyx (upper pole)"),
    MID_CALYX("Mid calyx (mid pole)"),
    LOWER_CALYX("Lower calyx (lower pole)"),
    RENAL_PELVIS("Renal pelvis"),
    PUJ("Pelvi-ureteric junction (PUJ/UPJ)")
}

data class PatientInfo(
    /** Optional. If blank, it must not be printed on the report. */
    val patientId: String = "",
    val name: String = "",
    val ageYears: String = "",
    val sex: Sex = Sex.UNSET,
    val bookingDateTime: LocalDateTime = LocalDateTime.now(),
    val reportingDateTime: LocalDateTime = LocalDateTime.now()
)

data class FindingsInput(
    val liverPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val fattyGrade: Int = 0,
    val hepatomegaly: Hepatomegaly = Hepatomegaly.NONE,
    val cld: Boolean = false,
    val ascites: Ascites = Ascites.NONE,

    val gbPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val gallstones: Boolean = false,
    val gallstoneSizeMm: String = "",
    val cholecystectomy: Boolean = false,

    val cbdPrintMode: OrganPrintMode = OrganPrintMode.SKIP,

    val pancreasPrintMode: OrganPrintMode = OrganPrintMode.SKIP,

    val spleenPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val splenomegaly: Boolean = false,
    val spleenSizeCm: String = "",

    val rkPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val rkCmd: CmdState = CmdState.PRESERVED,
    val hydronephrosisRight: Hydronephrosis = Hydronephrosis.NONE,
    val stoneRightPresent: Boolean = false,
    val stoneRightMm: String = "",
    val stoneRightLocation: StoneLocation? = null,
    val renalCystRight: Boolean = false,
    val renalCystRightSizeMm: String = "",

    val lkPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val lkCmd: CmdState = CmdState.PRESERVED,
    val hydronephrosisLeft: Hydronephrosis = Hydronephrosis.NONE,
    val stoneLeftPresent: Boolean = false,
    val stoneLeftMm: String = "",
    val stoneLeftLocation: StoneLocation? = null,
    val renalCystLeft: Boolean = false,
    val renalCystLeftSizeMm: String = "",

    val obstruction: Obstruction = Obstruction.UNSET,
    val bladderPrintMode: OrganPrintMode = OrganPrintMode.NORMAL
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

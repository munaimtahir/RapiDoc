package com.alshifa.rapidocusg

enum class UterusStatus { NORMAL, BULKY }
enum class FreeFluid { NONE, MILD, MODERATE }

data class PelvisFindingsInput(
    val uterusPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val uterusStatus: UterusStatus = UterusStatus.NORMAL,
    val fibroid: Boolean = false,
    val fibroidSizeMm: String = "",

    val endoPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val endoThicknessMm: String = "",

    val roPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val cystRight: Boolean = false,
    val cystSizeRightMm: String = "",

    val loPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val cystLeft: Boolean = false,
    val cystSizeLeftMm: String = "",

    val adnexaPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val freeFluid: FreeFluid = FreeFluid.NONE,

    val bladderPrintMode: OrganPrintMode = OrganPrintMode.NORMAL
)

data class PelvisReportInput(
    val patient: PatientInfo,
    val findings: PelvisFindingsInput
)

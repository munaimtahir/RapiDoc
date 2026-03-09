package com.alshifa.rapidocusg

enum class BladderWallStatus { NORMAL, THICKENED }
enum class PostVoidResidual { NONE, SIGNIFICANT }

data class KubFindingsInput(
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

    val bladderPrintMode: OrganPrintMode = OrganPrintMode.NORMAL,
    val bladderWallStatus: BladderWallStatus = BladderWallStatus.NORMAL,
    val bladderStone: Boolean = false,
    val bladderStoneSizeMm: String = "",
    val postVoidResidual: PostVoidResidual = PostVoidResidual.NONE,

    val prostatePrintMode: OrganPrintMode = OrganPrintMode.SKIP,
    val prostateEnlarged: Boolean = false,
    val prostateVolCc: String = ""
)

data class KubReportInput(
    val patient: PatientInfo,
    val findings: KubFindingsInput
)

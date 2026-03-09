package com.alshifa.rapidocusg

enum class PregStatus { LIVE_IUP, IUP_NO_CARDIAC, EMPTY_SAC }
enum class FetalPresentation { CEPHALIC, BREECH, VARIABLE, EARLY }
enum class PlacentaLocation { ANTERIOR, POSTERIOR, FUNDAL, LOW_LYING, NOT_VISUALIZED }
enum class LiquorVolume { ADEQUATE, REDUCED, INCREASED }

data class ObstetricFindingsInput(
    val pregStatus: PregStatus = PregStatus.LIVE_IUP,
    val fhrPresent: Boolean = true,
    val fhrBpm: String = "",
    val gaWeeks: String = "",
    val gaDays: String = "",
    val presentation: FetalPresentation = FetalPresentation.CEPHALIC,
    val placenta: PlacentaLocation = PlacentaLocation.ANTERIOR,
    val liquor: LiquorVolume = LiquorVolume.ADEQUATE,
    val cervixClosed: Boolean = true
)

data class ObstetricReportInput(
    val patient: PatientInfo,
    val findings: ObstetricFindingsInput
)

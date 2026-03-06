package com.alshifa.rapidocusg.core.documentengine

import android.content.Context
import com.alshifa.rapidocusg.ReportInput
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

enum class DocumentType { USG_ABDOMEN, MEDICAL_LEAVE_CERT, MEDICAL_FITNESS_CERT }

data class PatientDemographics(
    val patientId: String? = null,
    val name: String,
    val age: String? = null,
    val gender: String? = null,
    val phone: String? = null
)

sealed class DocumentPayload {
    data class UsgAbdomenPayload(val reportInput: ReportInput) : DocumentPayload()
    
    data class LeaveCertificatePayload(
        val patient: PatientDemographics,
        val issueDateTime: LocalDateTime,
        val diagnosisOrReason: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationDays: Int,
        val notes: String = ""
    ) : DocumentPayload()

    data class FitnessCertificatePayload(
        val patient: PatientDemographics,
        val issueDateTime: LocalDateTime,
        val purposeText: String,
        val restrictionsText: String?,
        val remarks: String = ""
    ) : DocumentPayload()
}

data class BrandingConfig(val headerText: String, val logoPathOrUri: String?)
interface TimeProvider { fun now(): LocalDateTime }
object SystemTimeProvider : TimeProvider { override fun now(): LocalDateTime = LocalDateTime.now() }
data class TimingConfig(val booking: LocalDateTime, val reporting: LocalDateTime)

data class RenderedDocument(val pdfFile: File, val displayName: String)

interface DocumentRenderer<T : DocumentPayload> {
    val type: DocumentType
    fun validate(payload: T): List<String>
    fun render(context: Context, payload: T, branding: BrandingConfig, timing: TimingConfig): RenderedDocument
}

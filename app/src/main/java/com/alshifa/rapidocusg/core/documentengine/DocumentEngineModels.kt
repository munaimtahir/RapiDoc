package com.alshifa.rapidocusg.core.documentengine

import android.content.Context
import com.alshifa.rapidocusg.FindingsInput
import com.alshifa.rapidocusg.ReportInput
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

enum class DocumentType { USG_ABDOMEN, MEDICAL_CERTIFICATE, PRESCRIPTION, LAB_REQUEST_SLIP, RADIOLOGY_REQUEST_SLIP }
enum class PlanTier { FREE, PRO }

data class PatientDemographics(
    val patientId: String? = null,
    val name: String,
    val age: String? = null,
    val gender: String? = null,
    val phone: String? = null
)

sealed class DocumentPayload {
    data class UsgAbdomenPayload(val reportInput: ReportInput, val findingsInput: FindingsInput = reportInput.findings) : DocumentPayload()
    data class MedicalCertificatePayload(
        val patient: PatientDemographics,
        val type: String,
        val diagnosis: String,
        val fromDate: LocalDate,
        val toDate: LocalDate,
        val remarks: String = ""
    ) : DocumentPayload()

    data class PrescriptionPayload(
        val patient: PatientDemographics,
        val diagnosis: String = "",
        val medicines: List<MedicineRow>,
        val advice: String = "",
        val followUpDate: LocalDate? = null
    ) : DocumentPayload() {
        data class MedicineRow(
            val drugName: String,
            val dose: String,
            val frequency: String,
            val duration: String,
            val instructions: String = ""
        )
    }

    data class LabRequestPayload(
        val patient: PatientDemographics,
        val selectedLabs: List<String>,
        val other: String = ""
    ) : DocumentPayload()

    data class RadiologyRequestPayload(
        val patient: PatientDemographics,
        val modality: String,
        val studyName: String,
        val clinicalNotes: String,
        val urgent: Boolean
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

package com.alshifa.rapidocusg.core.documentengine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.alshifa.rapidocusg.PdfGenerator
import com.alshifa.rapidocusg.RulesEngine
import com.alshifa.rapidocusg.KubRulesEngine
import com.alshifa.rapidocusg.PelvisRulesEngine
import com.alshifa.rapidocusg.ObstetricRulesEngine
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

object UsgRenderer : DocumentRenderer<DocumentPayload.UsgAbdomenPayload> {
    override val type = DocumentType.USG_ABDOMEN
    override fun validate(payload: DocumentPayload.UsgAbdomenPayload): List<String> = buildList {
        if (payload.reportInput.patient.name.isBlank()) add("Patient name required")
        if (payload.reportInput.patient.ageYears.isBlank()) add("Age required")
    }

    override fun render(context: Context, payload: DocumentPayload.UsgAbdomenPayload, branding: BrandingConfig, timing: TimingConfig): RenderedDocument {
        val inputForPdf = payload.reportInput.copy(
            patient = payload.reportInput.patient.copy(
                bookingDateTime = timing.booking,
                reportingDateTime = timing.reporting
            )
        )
        val file = PdfGenerator.generatePdf(context, "Ultrasound Abdomen Report", inputForPdf.patient, RulesEngine.buildReport(inputForPdf), branding)
        return RenderedDocument(file, "USG Abdomen")
    }
}

object KubRenderer : DocumentRenderer<DocumentPayload.UsgKubPayload> {
    override val type = DocumentType.USG_KUB
    override fun validate(payload: DocumentPayload.UsgKubPayload): List<String> = buildList {
        if (payload.reportInput.patient.name.isBlank()) add("Patient name required")
        if (payload.reportInput.patient.ageYears.isBlank()) add("Age required")
    }

    override fun render(context: Context, payload: DocumentPayload.UsgKubPayload, branding: BrandingConfig, timing: TimingConfig): RenderedDocument {
        val inputForPdf = payload.reportInput.copy(
            patient = payload.reportInput.patient.copy(
                bookingDateTime = timing.booking,
                reportingDateTime = timing.reporting
            )
        )
        val file = PdfGenerator.generatePdf(context, "Ultrasound KUB / Renal Tract Report", inputForPdf.patient, KubRulesEngine.buildReport(inputForPdf), branding)
        return RenderedDocument(file, "USG KUB / Renal Tract")
    }
}

object PelvisRenderer : DocumentRenderer<DocumentPayload.UsgPelvisPayload> {
    override val type = DocumentType.USG_PELVIS
    override fun validate(payload: DocumentPayload.UsgPelvisPayload): List<String> = buildList {
        if (payload.reportInput.patient.name.isBlank()) add("Patient name required")
        if (payload.reportInput.patient.ageYears.isBlank()) add("Age required")
    }

    override fun render(context: Context, payload: DocumentPayload.UsgPelvisPayload, branding: BrandingConfig, timing: TimingConfig): RenderedDocument {
        val inputForPdf = payload.reportInput.copy(
            patient = payload.reportInput.patient.copy(
                bookingDateTime = timing.booking,
                reportingDateTime = timing.reporting
            )
        )
        val file = PdfGenerator.generatePdf(context, "Ultrasound Pelvis Report", inputForPdf.patient, PelvisRulesEngine.buildReport(inputForPdf), branding)
        return RenderedDocument(file, "USG Pelvis")
    }
}

object ObstetricRenderer : DocumentRenderer<DocumentPayload.UsgObstetricPayload> {
    override val type = DocumentType.USG_OBSTETRIC
    override fun validate(payload: DocumentPayload.UsgObstetricPayload): List<String> = buildList {
        if (payload.reportInput.patient.name.isBlank()) add("Patient name required")
        if (payload.reportInput.patient.ageYears.isBlank()) add("Age required")
        val weeks = payload.reportInput.findings.gaWeeks.toIntOrNull()
        if (weeks == null || weeks <= 0) add("Gestational Age (Weeks) required")
    }

    override fun render(context: Context, payload: DocumentPayload.UsgObstetricPayload, branding: BrandingConfig, timing: TimingConfig): RenderedDocument {
        val inputForPdf = payload.reportInput.copy(
            patient = payload.reportInput.patient.copy(
                bookingDateTime = timing.booking,
                reportingDateTime = timing.reporting
            )
        )
        val file = PdfGenerator.generatePdf(context, "Ultrasound Obstetric Report", inputForPdf.patient, ObstetricRulesEngine.buildReport(inputForPdf), branding)
        return RenderedDocument(file, "USG Obstetric")
    }
}


private object SimpleDocPdf {
    private val fileDateTime = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.US)
    private val colorGray = DeviceRgb(200, 200, 200)

    fun render(context: Context, title: String, branding: BrandingConfig, timing: TimingConfig, patient: PatientDemographics, bodyLines: List<String>): File {
        val outputDir = File(context.filesDir, "reports")
        outputDir.mkdirs()
        val safeName = patient.name.replace("[^A-Za-z0-9]+".toRegex(), "_")
        val file = File(outputDir, "${title.replace(' ','_')}_${safeName}_${timing.reporting.format(fileDateTime)}.pdf")

        FileOutputStream(file).use { fos ->
            val pdfDoc = PdfDocument(PdfWriter(fos))
            val document = Document(pdfDoc)
            document.setMargins(40f, 40f, 40f, 40f)

            // Header Area
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 3f)))
            headerTable.setWidth(UnitValue.createPercentValue(100f))

            val logoBmp = branding.logoPathOrUri?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
                ?: BitmapFactory.decodeResource(context.resources, com.alshifa.rapidocusg.R.drawable.polyclinic_logo)

            val logoCell = Cell().setBorder(Border.NO_BORDER)
            if (logoBmp != null) {
                val stream = ByteArrayOutputStream()
                logoBmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imgData = ImageDataFactory.create(stream.toByteArray())
                val image = Image(imgData).scaleToFit(80f, 50f)
                logoCell.add(image)
            }
            headerTable.addCell(logoCell)

            val titleCell = Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE)
            titleCell.add(Paragraph(branding.headerText).setBold().setFontSize(14f))
            titleCell.add(Paragraph(title).setBold().setFontSize(12f))
            headerTable.addCell(titleCell)

            document.add(headerTable)
            document.add(Paragraph("\n").setFontSize(10f))

            // Demographics Table
            val demoTable = Table(UnitValue.createPercentArray(floatArrayOf(1.5f, 3.5f)))
            demoTable.setWidth(UnitValue.createPercentValue(100f))

            val border = SolidBorder(colorGray, 1f)
            fun addRow(label: String, value: String) {
                demoTable.addCell(Cell().add(Paragraph(label).setBold().setFontSize(11f)).setBorder(border).setPadding(4f))
                demoTable.addCell(Cell().add(Paragraph(value).setFontSize(11f)).setBorder(border).setPadding(4f))
            }

            addRow("Patient Name", patient.name)
            patient.patientId?.trim()?.takeIf { it.isNotBlank() }?.let { addRow("Patient ID", it) }
            patient.age?.let { age -> addRow("Age/Gender", "$age / ${patient.gender.orEmpty()}") }
            patient.phone?.trim()?.takeIf { it.isNotBlank() }?.let { addRow("Phone", it) }
            addRow("Date/Time", timing.reporting.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.US)))

            document.add(demoTable)
            document.add(Paragraph("\n").setFontSize(10f))

            // Body Lines
            for (line in bodyLines) {
                document.add(Paragraph(line).setFontSize(11f).setMarginBottom(8f))
            }

            // Footer
            val footer = Paragraph("Electronically verified. Laboratory results should be interpreted by a physician in correlation with clinical and radiologic findings.")
                .setFontSize(9f)
                .setTextAlignment(TextAlignment.LEFT)
                .setFixedPosition(40f, 40f, 515f)
            document.add(footer)

            document.close()
        }

        // Validate
        if (!validatePdf(file)) {
            throw IllegalStateException("Generated PDF failed validation.")
        }

        return file
    }

    private fun validatePdf(file: File): Boolean {
        if (!file.exists() || file.length() == 0L) return false
        try {
            val raf = java.io.RandomAccessFile(file, "r")
            val length = raf.length()
            if (length < 6) return false
            raf.seek(length - 100)
            val buffer = ByteArray(100)
            raf.read(buffer)
            raf.close()
            if (!String(buffer).contains("%%EOF")) return false
        } catch (e: Exception) {
            return false
        }
        try {
            val doc = PdfDocument(PdfReader(file.absolutePath))
            if (doc.numberOfPages <= 0) return false
            doc.close()
        } catch (e: Exception) {
            return false
        }
        return true
    }
}

object LeaveCertificateRenderer : DocumentRenderer<DocumentPayload.LeaveCertificatePayload> {
    override val type = DocumentType.MEDICAL_LEAVE_CERT
    override fun validate(payload: DocumentPayload.LeaveCertificatePayload): List<String> = buildList {
        if (payload.patient.name.isBlank()) add("Patient name required")
        if (payload.patient.age.isNullOrBlank()) add("Age required")
        if (payload.diagnosisOrReason.isBlank()) add("Diagnosis/Reason required")
    }
    
    override fun render(context: Context, payload: DocumentPayload.LeaveCertificatePayload, branding: BrandingConfig, timing: TimingConfig): RenderedDocument {
        val dtFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        val lines = mutableListOf<String>()
        val startStr = payload.startDate.format(dtFormatter)
        val endStr = payload.endDate.format(dtFormatter)
        
        lines += "This is to certify that Mr/Ms ${payload.patient.name}, aged ${payload.patient.age} years, was examined and is advised rest for ${payload.durationDays} day(s) from $startStr to $endStr due to ${payload.diagnosisOrReason}."
        if (payload.notes.isNotBlank()) {
            lines += "Notes: ${payload.notes}"
        }
        lines += "This certificate is issued based on clinical examination. Verification may be required where applicable."
        
        val file = SimpleDocPdf.render(context, "Medical Leave Certificate", branding, timing, payload.patient, lines)
        return RenderedDocument(file, "Medical Leave Certificate")
    }
}

object FitnessCertificateRenderer : DocumentRenderer<DocumentPayload.FitnessCertificatePayload> {
    override val type = DocumentType.MEDICAL_FITNESS_CERT
    override fun validate(payload: DocumentPayload.FitnessCertificatePayload): List<String> = buildList {
        if (payload.patient.name.isBlank()) add("Patient name required")
        if (payload.patient.age.isNullOrBlank()) add("Age required")
        if (payload.purposeText.isBlank()) add("Purpose required")
    }
    
    override fun render(context: Context, payload: DocumentPayload.FitnessCertificatePayload, branding: BrandingConfig, timing: TimingConfig): RenderedDocument {
        val lines = mutableListOf<String>()
        
        lines += "This is to certify that Mr/Ms ${payload.patient.name}, aged ${payload.patient.age} years, was examined and is found medically fit for ${payload.purposeText}."
        if (!payload.restrictionsText.isNullOrBlank()) {
            lines += "Restrictions: ${payload.restrictionsText}"
        }
        if (payload.remarks.isNotBlank()) {
            lines += "Remarks: ${payload.remarks}"
        }
        lines += "This certificate is issued based on clinical examination. Verification may be required where applicable."
        
        val file = SimpleDocPdf.render(context, "Medical Fitness Certificate", branding, timing, payload.patient, lines)
        return RenderedDocument(file, "Medical Fitness Certificate")
    }
}

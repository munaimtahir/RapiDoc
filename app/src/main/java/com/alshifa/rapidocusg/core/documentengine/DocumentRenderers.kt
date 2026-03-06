package com.alshifa.rapidocusg.core.documentengine

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.alshifa.rapidocusg.PdfGenerator
import com.alshifa.rapidocusg.RulesEngine
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
        val file = PdfGenerator.generatePdf(context, inputForPdf, RulesEngine.buildReport(inputForPdf))
        return RenderedDocument(file, "USG Abdomen")
    }
}

private object SimpleDocPdf {
    private val fileDateTime = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.US)
    fun render(context: Context, title: String, branding: BrandingConfig, timing: TimingConfig, patient: PatientDemographics, bodyLines: List<String>): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val text = Paint().apply { color = Color.BLACK; textSize = 11f; isAntiAlias = true }
        val bold = Paint(text).apply { isFakeBoldText = true; textSize = 13f }

        drawHeader(context, canvas, branding, title, bold)
        var y = 100f
        val rows = buildList {
            add("Patient Name" to patient.name)
            patient.patientId?.trim()?.takeIf { it.isNotBlank() }?.let { add("Patient ID" to it) }
            patient.age?.let { age -> add("Age/Gender" to "$age / ${patient.gender.orEmpty()}") }
            patient.phone?.trim()?.takeIf { it.isNotBlank() }?.let { add("Phone" to it) }
            add("Date/Time" to timing.reporting.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.US)))
        }
        y = drawTable(canvas, y, rows, text)
        y += 20f
        bodyLines.forEach { line ->
            wrap(line, text, 500f).forEach { w -> canvas.drawText(w, 40f, y, text); y += 16f }
            y += 4f
        }
        doc.finishPage(page)
        val outputDir = File(context.filesDir, "reports")
        outputDir.mkdirs()
        val safeName = patient.name.replace("[^A-Za-z0-9]+".toRegex(), "_")
        val file = File(outputDir, "${title.replace(' ','_')}_${safeName}_${timing.reporting.format(fileDateTime)}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private fun drawHeader(context: Context, canvas: Canvas, branding: BrandingConfig, title: String, paint: Paint) {
        val logoBmp = branding.logoPathOrUri?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
            ?: BitmapFactory.decodeResource(context.resources, com.alshifa.rapidocusg.R.drawable.polyclinic_logo)
        logoBmp?.let { canvas.drawBitmap(it, null, RectF(40f, 20f, 120f, 70f), null) }
        canvas.drawText(branding.headerText, 135f, 45f, paint)
        canvas.drawText(title, 135f, 65f, paint)
    }

    private fun drawTable(canvas: Canvas, startY: Float, rows: List<Pair<String, String>>, paint: Paint): Float {
        var y = startY
        val line = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val left = 40f; val mid = 180f; val right = 555f; val h = 24f
        rows.forEach { (l, v) ->
            canvas.drawText(l, left + 4f, y + 16f, paint)
            canvas.drawText(v, mid + 4f, y + 16f, paint)
            canvas.drawLine(left, y + h, right, y + h, line)
            y += h
        }
        canvas.drawLine(left, startY, right, startY, line)
        canvas.drawLine(left, startY, left, y, line)
        canvas.drawLine(mid, startY, mid, y, line)
        canvas.drawLine(right, startY, right, y, line)
        return y
    }

    private fun wrap(t: String, p: Paint, w: Float): List<String> {
        val out = mutableListOf<String>()
        var cur = ""
        for (word in t.split(" ")) {
            val nxt = if (cur.isBlank()) word else "$cur $word"
            if (p.measureText(nxt) <= w) cur = nxt else { out += cur; cur = word }
        }
        if (cur.isNotBlank()) out += cur
        return out
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
        lines += ""
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
        lines += ""
        lines += "This certificate is issued based on clinical examination. Verification may be required where applicable."
        
        val file = SimpleDocPdf.render(context, "Medical Fitness Certificate", branding, timing, payload.patient, lines)
        return RenderedDocument(file, "Medical Fitness Certificate")
    }
}

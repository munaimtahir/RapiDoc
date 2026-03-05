package com.alshifa.rapidocusg

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.alshifa.rapidocusg.R
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

object PdfGenerator {

    private val displayDateTime = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.US)
    private val fileDateTime = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.US)

    fun generatePdf(context: Context, input: ReportInput, body: ReportBody): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val black = Paint().apply { color = Color.BLACK; textSize = 11f; isAntiAlias = true }
        val bold = Paint(black).apply { textSize = 13f; isFakeBoldText = true }
        val section = Paint(black).apply { textSize = 12f; isFakeBoldText = true }
        val tableLabel = Paint(black).apply { isFakeBoldText = true }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        var y = 40f

        drawHeader(context, canvas, bold)
        y = 90f

        val demoRows = buildList {
            add("Patient Name" to input.patient.name)
            val pid = input.patient.patientId.trim()
            if (pid.isNotEmpty()) add("Patient ID" to pid)
            add("Age/Gender" to "${input.patient.ageYears} / ${input.patient.sex.name}")
            add("Booking Date/Time" to input.patient.bookingDateTime.format(displayDateTime))
            add("Reporting Date/Time" to input.patient.reportingDateTime.format(displayDateTime))
        }

        y = drawDemographicsTable(canvas, y, demoRows, tableLabel, black, linePaint)
        y += 20f

        canvas.drawText("Findings", 40f, y, section)
        y += 18f
        y = drawParagraphLines(canvas, body.findingsLines, y, black)

        y += 12f
        canvas.drawText("Impression", 40f, y, section)
        y += 18f

        val impressionPaint = Paint(black).apply {
            color = if (body.isNormal) context.getColor(R.color.normal_green) else context.getColor(R.color.abnormal_red)
            isFakeBoldText = true
        }
        drawParagraphLines(canvas, body.impressionLines, y, impressionPaint)

        drawFooter(canvas, black)

        doc.finishPage(page)

        val outputDir = File(context.filesDir, "reports")
        outputDir.mkdirs()
        val safeName = input.patient.name.trim().ifBlank { "Patient" }.replace("[^A-Za-z0-9]+".toRegex(), "_")
        val fileName = "USG_${safeName}_${input.patient.reportingDateTime.format(fileDateTime)}.pdf"
        val outFile = File(outputDir, fileName)

        FileOutputStream(outFile).use { out -> doc.writeTo(out) }
        doc.close()
        return outFile
    }

    private fun drawHeader(context: Context, canvas: Canvas, titlePaint: Paint) {
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.polyclinic_logo)
        if (logo != null) {
            val dstLeft = 40f
            val dstTop = 20f
            val dstRight = 120f
            val dstBottom = 70f
            canvas.drawBitmap(logo, null, android.graphics.RectF(dstLeft, dstTop, dstRight, dstBottom), null)
        }
        canvas.drawText("AlShifa PolyClinic", 135f, 45f, titlePaint)
        canvas.drawText("Ultrasound Abdomen Report", 135f, 65f, titlePaint)
    }

    private fun drawDemographicsTable(
        canvas: Canvas,
        startY: Float,
        rows: List<Pair<String, String>>,
        labelPaint: Paint,
        valuePaint: Paint,
        linePaint: Paint
    ): Float {
        var y = startY
        val left = 40f
        val middle = 180f
        val right = 555f
        val rowHeight = 24f

        rows.forEach { (label, value) ->
            canvas.drawText(label, left + 4f, y + 16f, labelPaint)
            canvas.drawText(value, middle + 4f, y + 16f, valuePaint)
            canvas.drawLine(left, y + rowHeight, right, y + rowHeight, linePaint)
            y += rowHeight
        }

        canvas.drawLine(left, startY, left, y, linePaint)
        canvas.drawLine(middle, startY, middle, y, linePaint)
        canvas.drawLine(right, startY, right, y, linePaint)
        canvas.drawLine(left, startY, right, startY, linePaint)

        return y
    }

    private fun drawParagraphLines(canvas: Canvas, lines: List<String>, startY: Float, paint: Paint): Float {
        var y = startY
        val left = 50f
        val maxWidth = 500f
        lines.forEach { sentence ->
            val wrapped = wrapText(sentence, paint, maxWidth)
            wrapped.forEach { line ->
                canvas.drawText("• $line", left, y, paint)
                y += 16f
            }
            y += 4f
        }
        return y
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val next = if (current.isBlank()) word else "$current $word"
            if (paint.measureText(next) <= maxWidth) {
                current = next
            } else {
                if (current.isNotBlank()) lines += current
                current = word
            }
        }
        if (current.isNotBlank()) lines += current
        return lines
    }

    private fun drawFooter(canvas: Canvas, paint: Paint) {
        paint.textSize = 9f
        val footer = "Electronically verified. Laboratory results should be interpreted by a physician in correlation with clinical and radiologic findings."
        val wrapped = wrapText(footer, paint, 510f)
        var y = 790f
        wrapped.forEach { line ->
            canvas.drawText(line, 40f, y, paint)
            y += 12f
        }
    }
}

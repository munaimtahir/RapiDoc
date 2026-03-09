package com.alshifa.rapidocusg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.alshifa.rapidocusg.core.documentengine.BrandingConfig
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

object PdfGenerator {

    private val displayDateTime = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.US)
    private val fileDateTime = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.US)

    // Impression Colors
    private val colorNormal = DeviceRgb(46, 125, 50)  // Green
    private val colorAbnormal = DeviceRgb(211, 47, 47) // Red
    private val colorGray = DeviceRgb(200, 200, 200)

    fun generatePdf(context: Context, title: String, patient: PatientInfo, body: ReportBody, branding: BrandingConfig): File {
        val outputDir = File(context.filesDir, "reports")
        outputDir.mkdirs()
        val safeName = patient.name.trim().ifBlank { "Patient" }.replace("[^A-Za-z0-9]+".toRegex(), "_")
        val fileName = "${title.replace(' ','_')}_${safeName}_${patient.reportingDateTime.format(fileDateTime)}.pdf"
        val outFile = File(outputDir, fileName)

        // Generate the PDF
        FileOutputStream(outFile).use { fos ->
            val writer = PdfWriter(fos)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            // Margins
            document.setMargins(40f, 40f, 40f, 40f)

            // Header (Logo + Title)
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 3f)))
            headerTable.setWidth(UnitValue.createPercentValue(100f))

            val logoBmp = branding.logoPathOrUri?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.polyclinic_logo)
            
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
                val c1 = Cell().add(Paragraph(label).setBold().setFontSize(11f)).setBorder(border).setPadding(4f)
                val c2 = Cell().add(Paragraph(value).setFontSize(11f)).setBorder(border).setPadding(4f)
                demoTable.addCell(c1)
                demoTable.addCell(c2)
            }

            addRow("Patient Name", patient.name)
            val pid = patient.patientId.trim()
            if (pid.isNotEmpty()) addRow("Patient ID", pid)
            addRow("Age/Gender", "${patient.ageYears} / ${patient.sex.name}")
            addRow("Booking Date/Time", patient.bookingDateTime.format(displayDateTime))
            addRow("Reporting Date/Time", patient.reportingDateTime.format(displayDateTime))

            document.add(demoTable)
            document.add(Paragraph("\n").setFontSize(10f))

            // Findings
            document.add(Paragraph("Findings").setBold().setFontSize(12f))
            for (line in body.findingsLines) {
                val p = Paragraph("•  $line").setFontSize(11f).setMarginBottom(4f)
                document.add(p)
            }

            document.add(Paragraph("\n").setFontSize(10f))

            // Impression
            document.add(Paragraph("Impression").setBold().setFontSize(12f))
            val impColor = if (body.isNormal) colorNormal else colorAbnormal
            for (line in body.impressionLines) {
                val p = Paragraph("•  $line").setFontSize(11f).setBold().setFontColor(impColor).setMarginBottom(4f)
                document.add(p)
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
        if (!validatePdf(outFile)) {
            throw IllegalStateException("Generated PDF failed validation.")
        }

        return outFile
    }

    private fun validatePdf(file: File): Boolean {
        if (!file.exists() || file.length() == 0L) {
            Log.e("PdfGenerator", "Validation failed: File does not exist or size is 0.")
            return false
        }
        
        // Check for EOF marker (%%EOF) loosely
        try {
            val raf = java.io.RandomAccessFile(file, "r")
            val length = raf.length()
            if (length < 6) return false
            raf.seek(length - 100)
            val buffer = ByteArray(100)
            raf.read(buffer)
            raf.close()
            val tail = String(buffer)
            if (!tail.contains("%%EOF")) {
                Log.e("PdfGenerator", "Validation failed: %%EOF marker not found.")
                return false
            }
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Validation failed: Could not read file tail.", e)
            return false
        }
        
        // Parse with iText PdfReader
        try {
            val reader = PdfReader(file.absolutePath)
            val doc = PdfDocument(reader)
            if (doc.numberOfPages <= 0) {
                Log.e("PdfGenerator", "Validation failed: No pages in PDF.")
                return false
            }
            doc.close()
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Validation failed: Cannot be parsed by PdfReader.", e)
            return false
        }

        return true
    }
}

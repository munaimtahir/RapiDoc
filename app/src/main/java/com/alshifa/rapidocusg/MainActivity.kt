package com.alshifa.rapidocusg

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    RapiDocApp()
                }
            }
        }
    }
}

@Composable
private fun RapiDocApp() {
    val context = LocalContext.current
    var reportInput by remember { mutableStateOf(SampleCases.normal()) }
    var forceNormal by remember { mutableStateOf(false) }
    var currentPdfFile by remember { mutableStateOf<File?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    val reportBody = remember(reportInput) { RulesEngine.buildReport(reportInput) }

    Scaffold { padding ->
        if (!showPreview) {
            QuickEntryScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                reportInput = reportInput,
                forceNormal = forceNormal,
                onInputChange = {
                    reportInput = it
                    forceNormal = false
                },
                onForceNormal = { enabled ->
                    forceNormal = enabled
                    if (enabled) {
                        reportInput = reportInput.copy(findings = FindingsInput())
                    }
                },
                onLoadNormal = {
                    reportInput = SampleCases.normal()
                    forceNormal = true
                    currentPdfFile = null
                },
                onLoadSurayya = {
                    reportInput = SampleCases.surayya()
                    forceNormal = false
                    currentPdfFile = null
                },
                onLoadFayyaz = {
                    reportInput = SampleCases.fayyaz()
                    forceNormal = false
                    currentPdfFile = null
                },
                onGoPreview = {
                    if (reportInput.patient.name.isBlank() || reportInput.patient.ageYears.isBlank()) {
                        Toast.makeText(context, "Patient name and age are required.", Toast.LENGTH_SHORT).show()
                    } else {
                        showPreview = true
                    }
                }
            )
        } else {
            PreviewScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                reportInput = reportInput,
                reportBody = reportBody,
                currentPdfFile = currentPdfFile,
                onBack = { showPreview = false },
                onGenerate = {
                    val now = LocalDateTime.now()
                    val inputForPdf = reportInput.copy(
                        patient = reportInput.patient.copy(
                            bookingDateTime = reportInput.patient.bookingDateTime,
                            reportingDateTime = now
                        )
                    )
                    val file = PdfGenerator.generatePdf(context, inputForPdf, RulesEngine.buildReport(inputForPdf))
                    currentPdfFile = file
                    Toast.makeText(context, "PDF generated: ${file.name}", Toast.LENGTH_SHORT).show()
                },
                onPrint = {
                    val file = currentPdfFile
                    if (file == null) {
                        Toast.makeText(context, "Generate PDF first.", Toast.LENGTH_SHORT).show()
                    } else {
                        PdfActions.print(context, file)
                    }
                },
                onShare = {
                    val file = currentPdfFile
                    if (file == null) {
                        Toast.makeText(context, "Generate PDF first.", Toast.LENGTH_SHORT).show()
                    } else {
                        PdfActions.share(context, file)
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickEntryScreen(
    modifier: Modifier,
    reportInput: ReportInput,
    forceNormal: Boolean,
    onInputChange: (ReportInput) -> Unit,
    onForceNormal: (Boolean) -> Unit,
    onLoadNormal: () -> Unit,
    onLoadSurayya: () -> Unit,
    onLoadFayyaz: () -> Unit,
    onGoPreview: () -> Unit
) {
    val findings = reportInput.findings

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Quick Entry", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Normal Toggle")
            Spacer(Modifier.width(8.dp))
            Switch(checked = forceNormal, onCheckedChange = onForceNormal)
        }

        OutlinedTextField(
            value = reportInput.patient.name,
            onValueChange = {
                onInputChange(reportInput.copy(patient = reportInput.patient.copy(name = it)))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Patient Name*") }
        )

        OutlinedTextField(
            value = reportInput.patient.ageYears,
            onValueChange = {
                onInputChange(reportInput.copy(patient = reportInput.patient.copy(ageYears = it.filter(Char::isDigit))))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Age*") }
        )

        EnumSelector(
            label = "Gender",
            options = Sex.entries,
            selected = reportInput.patient.sex,
            enabled = true,
            onSelect = { onInputChange(reportInput.copy(patient = reportInput.patient.copy(sex = it))) }
        )

        EnumSelector(
            label = "Fatty Liver Grade",
            options = listOf(0, 1, 2, 3),
            selected = findings.fattyGrade,
            enabled = !forceNormal,
            itemLabel = { if (it == 0) "None" else "Grade $it" },
            onSelect = { onInputChange(reportInput.copy(findings = findings.copy(fattyGrade = it))) }
        )

        EnumSelector(
            label = "Hepatomegaly",
            options = Hepatomegaly.entries,
            selected = findings.hepatomegaly,
            enabled = !forceNormal,
            itemLabel = { it.name.replace("_", " ") },
            onSelect = { onInputChange(reportInput.copy(findings = findings.copy(hepatomegaly = it))) }
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("CLD")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = findings.cld,
                enabled = !forceNormal,
                onCheckedChange = { onInputChange(reportInput.copy(findings = findings.copy(cld = it))) }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Gallstones")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = findings.gallstones,
                enabled = !forceNormal,
                onCheckedChange = { onInputChange(reportInput.copy(findings = findings.copy(gallstones = it))) }
            )
        }

        EnumSelector(
            label = "Hydronephrosis Left",
            options = Hydronephrosis.entries,
            selected = findings.hydronephrosisLeft,
            enabled = !forceNormal,
            itemLabel = { it.name.replace("_", " ") },
            onSelect = { onInputChange(reportInput.copy(findings = findings.copy(hydronephrosisLeft = it))) }
        )

        EnumSelector(
            label = "Hydronephrosis Right",
            options = Hydronephrosis.entries,
            selected = findings.hydronephrosisRight,
            enabled = !forceNormal,
            itemLabel = { it.name.replace("_", " ") },
            onSelect = { onInputChange(reportInput.copy(findings = findings.copy(hydronephrosisRight = it))) }
        )

        EnumSelector(
            label = "Obstruction",
            options = Obstruction.entries,
            selected = findings.obstruction,
            enabled = !forceNormal,
            itemLabel = { it.name.replace("_", " ") },
            onSelect = { onInputChange(reportInput.copy(findings = findings.copy(obstruction = it))) }
        )

        OutlinedTextField(
            value = findings.stoneLeftMm,
            onValueChange = {
                onInputChange(reportInput.copy(findings = findings.copy(stoneLeftMm = it.filter(Char::isDigit))))
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Left Renal Stone (mm)") },
            enabled = !forceNormal
        )

        OutlinedTextField(
            value = findings.stoneRightMm,
            onValueChange = {
                onInputChange(reportInput.copy(findings = findings.copy(stoneRightMm = it.filter(Char::isDigit))))
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Right Renal Stone (mm)") },
            enabled = !forceNormal
        )

        EnumSelector(
            label = "Ascites",
            options = Ascites.entries,
            selected = findings.ascites,
            enabled = !forceNormal,
            itemLabel = { it.name.replace("_", " ") },
            onSelect = { onInputChange(reportInput.copy(findings = findings.copy(ascites = it))) }
        )

        Spacer(Modifier.height(4.dp))
        Text("Load Sample Case", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onLoadNormal) { Text("Normal") }
            Button(onClick = onLoadSurayya) { Text("Surayya 50F") }
            Button(onClick = onLoadFayyaz) { Text("Fayyaz 32M") }
        }

        Spacer(Modifier.height(6.dp))
        Button(onClick = onGoPreview, modifier = Modifier.fillMaxWidth()) {
            Text("Go To Preview")
        }
    }
}

@Composable
private fun PreviewScreen(
    modifier: Modifier,
    reportInput: ReportInput,
    reportBody: ReportBody,
    currentPdfFile: File?,
    onBack: () -> Unit,
    onGenerate: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Preview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Patient: ${reportInput.patient.name}")
        Text("Age/Gender: ${reportInput.patient.ageYears} / ${reportInput.patient.sex.name}")

        Text("Impression", fontWeight = FontWeight.Bold)
        val impressionColor = if (reportBody.isNormal) Color(0xFF2E7D32) else Color(0xFFC62828)
        reportBody.impressionLines.forEach {
            Text("• $it", color = impressionColor)
        }

        Text("PDF Preview", fontWeight = FontWeight.Bold)
        PdfPreviewBox(file = currentPdfFile)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onGenerate) { Text("Generate PDF") }
            Button(onClick = onPrint) { Text("Print") }
            Button(onClick = onShare) { Text("Share") }
        }
        TextButton(onClick = onBack) { Text("Back to Quick Entry") }
    }
}

@Composable
private fun PdfPreviewBox(file: File?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(Color(0xFFF3F3F3)),
        contentAlignment = Alignment.Center
    ) {
        if (file == null || !file.exists()) {
            Text("Generate PDF to preview")
            return
        }

        val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = file.absolutePath) {
            value = withContext(Dispatchers.IO) { renderPdfFirstPage(file) }
        }

        val bitmap = bitmapState.value
        if (bitmap == null) {
            Text("Unable to render preview")
        } else {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "PDF first page",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

private fun renderPdfFirstPage(file: File): Bitmap? {
    return try {
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        val page = renderer.openPage(0)
        val width = page.width * 2
        val height = page.height * 2
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        descriptor.close()
        bitmap
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun <T> EnumSelector(
    label: String,
    options: List<T>,
    selected: T,
    enabled: Boolean,
    itemLabel: (T) -> String = { it.toString() },
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label)
        Box {
            OutlinedTextField(
                value = itemLabel(selected),
                onValueChange = {},
                enabled = enabled,
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = { if (enabled) expanded = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("Select")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(itemLabel(option)) },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        }
                    )
                }
            }
        }
    }
}

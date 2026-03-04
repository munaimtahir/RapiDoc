package com.alshifa.rapidocusg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.ButtonDefaults
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
import com.google.gson.Gson

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
    val prefs = remember { context.getSharedPreferences("RapiDocPrefs", Context.MODE_PRIVATE) }
    val gson = remember { Gson() }

    val initialFindings = remember {
        val savedJson = prefs.getString("pref_findings", null)
        if (savedJson != null) {
            try {
                gson.fromJson(savedJson, FindingsInput::class.java)
            } catch (e: Exception) {
                FindingsInput()
            }
        } else {
            FindingsInput()
        }
    }

    var reportInput by remember { mutableStateOf(ReportInput(patient = PatientInfo(), findings = initialFindings)) }
    var forceNormal by remember { mutableStateOf(false) }
    var currentPdfFile by remember { mutableStateOf<File?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    LaunchedEffect(reportInput.findings) {
        prefs.edit().putString("pref_findings", gson.toJson(reportInput.findings)).apply()
    }

    val reportBody = remember(reportInput) { RulesEngine.buildReport(reportInput) }

    Scaffold { padding ->
        BackHandler(enabled = true) {
            if (showPreview) {
                showPreview = false
            } else {
                (context as? android.app.Activity)?.finish()
            }
        }
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

                onGoPreview = {
                    showPreview = true
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
```kotlin
                            bookingDateTime = now.minusMinutes(20),
                            reportingDateTime = now
```
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
                onNewReport = {
                    reportInput = reportInput.copy(patient = PatientInfo())
                    forceNormal = false
                    currentPdfFile = null
                    showPreview = false
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
            Text("Normal Toggle (resets input)")
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
            value = reportInput.patient.patientId,
            onValueChange = {
                onInputChange(reportInput.copy(patient = reportInput.patient.copy(patientId = it)))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Patient ID (optional)") }
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
            label = "Gender*",
            options = Sex.entries,
            selected = reportInput.patient.sex,
            enabled = true,
            onSelect = { onInputChange(reportInput.copy(patient = reportInput.patient.copy(sex = it))) }
        )

        // Liver
        OrganSection("Liver", findings.liverPrintMode, forceNormal,
            onModeChange = { onInputChange(reportInput.copy(findings = findings.copy(liverPrintMode = it))) }) {
            EnumSelector(label = "Fatty Liver Grade", options = listOf(0, 1, 2, 3), selected = findings.fattyGrade, enabled = true,
                itemLabel = { if (it == 0) "None" else "Grade $it" },
                onSelect = { onInputChange(reportInput.copy(findings = findings.copy(fattyGrade = it))) })
            EnumSelector(label = "Hepatomegaly", options = Hepatomegaly.entries, selected = findings.hepatomegaly, enabled = true,
                itemLabel = { it.name.replace("_", " ") },
                onSelect = { onInputChange(reportInput.copy(findings = findings.copy(hepatomegaly = it))) })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CLD")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.cld, onCheckedChange = { onInputChange(reportInput.copy(findings = findings.copy(cld = it))) })
            }
            EnumSelector(label = "Ascites", options = Ascites.entries, selected = findings.ascites, enabled = true,
                itemLabel = { it.name.replace("_", " ") },
                onSelect = { onInputChange(reportInput.copy(findings = findings.copy(ascites = it))) })
        }

        // Gallbladder
        OrganSection("Gallbladder", findings.gbPrintMode, forceNormal,
            onModeChange = { onInputChange(reportInput.copy(findings = findings.copy(gbPrintMode = it))) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gallstones")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.gallstones, onCheckedChange = { onInputChange(reportInput.copy(findings = findings.copy(gallstones = it))) })
            }
            if (findings.gallstones) {
                OutlinedTextField(value = findings.gallstoneSizeMm, onValueChange = { onInputChange(reportInput.copy(findings = findings.copy(gallstoneSizeMm = it.filter(Char::isDigit)))) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Stone size (mm)") })
            }
        }

        // CBD
        OrganSection("CBD", findings.cbdPrintMode, forceNormal,
            onModeChange = { onInputChange(reportInput.copy(findings = findings.copy(cbdPrintMode = it))) }) {
        }

        // Pancreas
        OrganSection("Pancreas", findings.pancreasPrintMode, forceNormal,
            onModeChange = { onInputChange(reportInput.copy(findings = findings.copy(pancreasPrintMode = it))) }) {
        }

        // Spleen
        OrganSection("Spleen", findings.spleenPrintMode, forceNormal,
            onModeChange = { onInputChange(reportInput.copy(findings = findings.copy(spleenPrintMode = it))) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Splenomegaly")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.splenomegaly, onCheckedChange = { onInputChange(reportInput.copy(findings = findings.copy(splenomegaly = it))) })
            }
            if (findings.splenomegaly) {
                OutlinedTextField(value = findings.spleenSizeCm, onValueChange = { onInputChange(reportInput.copy(findings = findings.copy(spleenSizeCm = it))) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Spleen size (cm)") })
            }
        }

        // Right Kidney
        OrganSection("Right Kidney", findings.rkPrintMode, forceNormal,
            onModeChange = { onInputChange(reportInput.copy(findings = findings.copy(rkPrintMode = it))) }) {
            EnumSelector(label = "Right CMD", options = CmdState.entries, selected = findings.rkCmd, enabled = true,
                onSelect = { onInputChange(reportInput.copy(findings = findings.copy(rkCmd = it))) })
            EnumSelector(label = "Hydronephrosis", options = Hydronephrosis.entries, selected = findings.hydronephrosisRight, enabled = true,
                onSelect = { onInputChange(reportInput.copy(findings = findings.copy(hydronephrosisRight = it))) })
            OutlinedTextField(value = findings.stoneRightMm, onValueChange = { onInputChange(reportInput.copy(findings = findings.copy(stoneRightMm = it.filter(Char::isDigit)))) },
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Right Renal Stone (mm)") })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Renal Cyst Right")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.renalCystRight, onCheckedChange = { onInputChange(reportInput.copy(findings = findings.copy(renalCystRight = it))) })
            }
            if (findings.renalCystRight) {
                OutlinedTextField(value = findings.renalCystRightSizeMm, onValueChange = { onInputChange(reportInput.copy(findings = findings.copy(renalCystRightSizeMm = it.filter(Char::isDigit)))) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Cyst Size (mm)") })
            }
        }

        // Left Kidney
        OrganSection("Left Kidney", findings.lkPrintMode, forceNormal,
            onModeChange = { onInputChange(reportInput.copy(findings = findings.copy(lkPrintMode = it))) }) {
            EnumSelector(label = "Left CMD", options = CmdState.entries, selected = findings.lkCmd, enabled = true,
                onSelect = { onInputChange(reportInput.copy(findings = findings.copy(lkCmd = it))) })
            EnumSelector(label = "Hydronephrosis", options = Hydronephrosis.entries, selected = findings.hydronephrosisLeft, enabled = true,
                onSelect = { onInputChange(reportInput.copy(findings = findings.copy(hydronephrosisLeft = it))) })
            OutlinedTextField(value = findings.stoneLeftMm, onValueChange = { onInputChange(reportInput.copy(findings = findings.copy(stoneLeftMm = it.filter(Char::isDigit)))) },
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Left Renal Stone (mm)") })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Renal Cyst Left")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.renalCystLeft, onCheckedChange = { onInputChange(reportInput.copy(findings = findings.copy(renalCystLeft = it))) })
            }
            if (findings.renalCystLeft) {
                OutlinedTextField(value = findings.renalCystLeftSizeMm, onValueChange = { onInputChange(reportInput.copy(findings = findings.copy(renalCystLeftSizeMm = it.filter(Char::isDigit)))) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Cyst Size (mm)") })
            }
        }

        EnumSelector(
            label = "Obstruction",
            options = Obstruction.entries,
            selected = findings.obstruction,
            enabled = !forceNormal,
            itemLabel = { it.name.replace("_", " ") },
            onSelect = { onInputChange(reportInput.copy(findings = findings.copy(obstruction = it))) }
        )

        // Urinary Bladder
        OrganSection("Urinary Bladder", findings.bladderPrintMode, forceNormal,
            onModeChange = { onInputChange(reportInput.copy(findings = findings.copy(bladderPrintMode = it))) }) {
        }

        Spacer(Modifier.height(4.dp))
        Text("Load Sample Case", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onLoadNormal) { Text("Normal") }
        }

        Spacer(Modifier.height(6.dp))
        Button(onClick = onGoPreview, modifier = Modifier.fillMaxWidth()) {
            Text("Go To Preview")
        }
    }
}

@Composable
private fun OrganSection(
    title: String,
    mode: OrganPrintMode,
    forceNormal: Boolean,
    onModeChange: (OrganPrintMode) -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF9F9F9)).padding(8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        EnumSelector(
            label = "Print Mode",
            options = OrganPrintMode.entries,
            selected = mode,
            enabled = !forceNormal,
            itemLabel = { it.name },
            onSelect = onModeChange
        )
        if (mode == OrganPrintMode.ABNORMAL && !forceNormal) {
            Spacer(Modifier.height(8.dp))
            content()
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
    onShare: () -> Unit,
    onNewReport: () -> Unit
) {
    var showError by remember { mutableStateOf(false) }
    val isValid = reportInput.patient.name.isNotBlank() && reportInput.patient.ageYears.isNotBlank() && reportInput.patient.sex != Sex.UNSET
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Preview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Patient: ${reportInput.patient.name}")
        if (reportInput.patient.patientId.trim().isNotEmpty()) {
            Text("Patient ID: ${reportInput.patient.patientId}")
        }
        Text("Age/Gender: ${reportInput.patient.ageYears} / ${reportInput.patient.sex.name}")

        Text("Impression", fontWeight = FontWeight.Bold)
        val impressionColor = if (reportBody.isNormal) Color(0xFF2E7D32) else Color(0xFFC62828)
        reportBody.impressionLines.forEach {
            Text("• $it", color = impressionColor)
        }

        Text("PDF Preview", fontWeight = FontWeight.Bold)
        PdfPreviewBox(file = currentPdfFile)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (!isValid) {
                        showError = true
                    } else {
                        showError = false
                        onGenerate()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isValid) MaterialTheme.colorScheme.primary else Color.Gray,
                    contentColor = if (isValid) MaterialTheme.colorScheme.onPrimary else Color.LightGray
                )
            ) { Text("Generate PDF") }
            Button(onClick = onPrint) { Text("Print") }
            Button(onClick = onShare) { Text("Share") }
        }

        if (showError && !isValid) {
            Text(
                text = "Required fields (Name, Age, Gender) must be valid to generate.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack) { Text("Back to Quick Entry") }
            Spacer(Modifier.weight(1f))
            Button(onClick = onNewReport) { Text("New Report") }
        }
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

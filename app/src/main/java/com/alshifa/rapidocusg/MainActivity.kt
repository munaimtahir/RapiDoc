package com.alshifa.rapidocusg

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alshifa.rapidocusg.core.documentengine.AppSettings
import com.alshifa.rapidocusg.core.documentengine.BrandingConfig
import com.alshifa.rapidocusg.core.documentengine.DocumentPayload
import com.alshifa.rapidocusg.core.documentengine.DocumentRegistry
import com.alshifa.rapidocusg.core.documentengine.DocumentType
import com.alshifa.rapidocusg.core.documentengine.LabRequestRenderer
import com.alshifa.rapidocusg.core.documentengine.MedicalCertificateRenderer
import com.alshifa.rapidocusg.core.documentengine.PatientDemographics
import com.alshifa.rapidocusg.core.documentengine.PlanTier
import com.alshifa.rapidocusg.core.documentengine.PrescriptionRenderer
import com.alshifa.rapidocusg.core.documentengine.RadiologyRequestRenderer
import com.alshifa.rapidocusg.core.documentengine.RenderedDocument
import com.alshifa.rapidocusg.core.documentengine.SettingsStore
import com.alshifa.rapidocusg.core.documentengine.SystemTimeProvider
import com.alshifa.rapidocusg.core.documentengine.TimingConfig
import com.alshifa.rapidocusg.core.documentengine.UsgRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
    val navController = rememberNavController()
    val settingsStore = remember { SettingsStore(context) }
    val settings by settingsStore.settings.collectAsState(initial = AppSettings())
    val scope = rememberCoroutineScope()
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
    var currentDocument by remember { mutableStateOf<RenderedDocument?>(null) }
    var currentDocType by remember { mutableStateOf(DocumentType.USG_ABDOMEN) }

    LaunchedEffect(reportInput.findings) {
        prefs.edit().putString("pref_findings", gson.toJson(reportInput.findings)).apply()
    }

    val reportBody = remember(reportInput) { RulesEngine.buildReport(reportInput) }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            Scaffold { padding ->
                Column(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("RapiDoc", style = MaterialTheme.typography.headlineSmall)
                    DocumentRegistry.docs.forEach { meta ->
                        val locked = !DocumentRegistry.allowedFor(settings.planTier, meta.type)
                        Button(onClick = {
                            if (locked) navController.navigate("upsell") else navController.navigate(meta.formRoute)
                        }, enabled = true, modifier = Modifier.fillMaxWidth()) {
                            Text(meta.displayName + if (locked) " (PRO)" else "")
                        }
                    }
                    TextButton(onClick = { navController.navigate("settings") }) { Text("Settings") }
                }
            }
        }
        composable("settings") {
            SettingsScreen(settings = settings, onBack = { navController.popBackStack() }, onUpdateHeader = {
                scope.launch { settingsStore.updateHeader(it) }
            }, onUpdatePlan = {
                scope.launch { settingsStore.updatePlan(it) }
            }, onUpdateLogo = { uri ->
                scope.launch {
                    val path = uri?.let { copyLogoToInternal(context, it) }
                    settingsStore.updateLogo(path)
                }
            }, onReset = {
                scope.launch {
                    settings.logoPath?.let { runCatching { File(it).delete() } }
                    settingsStore.resetFactory()
                }
            })
        }
        composable("upsell") {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO Feature", style = MaterialTheme.typography.headlineSmall)
                Text("Upgrade to PRO to unlock all document types and personalization settings.")
                TextButton(onClick = { navController.popBackStack() }) { Text("Back") }
            }
        }
        composable("doc/usg_abdomen/form") {
            QuickEntryScreen(
                modifier = Modifier.fillMaxSize(),
                reportInput = reportInput,
                forceNormal = forceNormal,
                onInputChange = { reportInput = it; forceNormal = false },
                onForceNormal = { enabled -> forceNormal = enabled; if (enabled) reportInput = reportInput.copy(findings = FindingsInput()) },
                onLoadNormal = {},
                onGoPreview = { navController.navigate("doc/usg_abdomen/preview") }
            )
        }
        composable("doc/usg_abdomen/preview") {
            PreviewScreen(
                modifier = Modifier.fillMaxSize(),
                reportInput = reportInput,
                reportBody = reportBody,
                currentPdfFile = currentDocument?.pdfFile,
                onBack = { navController.popBackStack() },
                onGenerate = {
                    val now = SystemTimeProvider.now()
                    val rendered = UsgRenderer.render(context, DocumentPayload.UsgAbdomenPayload(reportInput), BrandingConfig(settings.headerText, settings.logoPath), TimingConfig(now.minusMinutes(20), now))
                    currentDocType = DocumentType.USG_ABDOMEN
                    currentDocument = rendered
                    Toast.makeText(context, "PDF generated: ${rendered.pdfFile.name}", Toast.LENGTH_SHORT).show()
                },
                onPrint = { currentDocument?.let { PdfActions.print(context, it.pdfFile) } ?: Toast.makeText(context, "Generate PDF first.", Toast.LENGTH_SHORT).show() },
                onNewReport = { reportInput = ReportInput(PatientInfo(), FindingsInput()); forceNormal = false; currentDocument = null; navController.navigate("home") },
                onShare = { currentDocument?.let { PdfActions.share(context, it.pdfFile) } ?: Toast.makeText(context, "Generate PDF first.", Toast.LENGTH_SHORT).show() }
            )
        }
        composable("doc/{docType}/form", arguments = listOf(navArgument("docType") { type = NavType.StringType })) { backStack ->
            val docType = backStack.arguments?.getString("docType")
            if (docType == "usg_abdomen") return@composable
            GenericDocForm(
                docType = docType.orEmpty(),
                onBack = { navController.popBackStack() },
                onGenerated = { rendered, type -> currentDocument = rendered; currentDocType = type; navController.navigate("doc/${docType}/preview") },
                settings = settings
            )
        }
        composable("doc/{docType}/preview", arguments = listOf(navArgument("docType") { type = NavType.StringType })) { backStack ->
            val docType = backStack.arguments?.getString("docType").orEmpty()
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Preview", style = MaterialTheme.typography.headlineSmall)
                Text("${currentDocument?.displayName ?: "Document"}")
                PdfPreviewBox(file = currentDocument?.pdfFile)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { currentDocument?.let { PdfActions.print(context, it.pdfFile) } }) { Text("Print") }
                    Button(onClick = { currentDocument?.let { PdfActions.share(context, it.pdfFile) } }) { Text("Share") }
                }
                TextButton(onClick = { navController.navigate("doc/$docType/form") }) { Text("Back to Form") }
                TextButton(onClick = { navController.navigate("home") }) { Text("Home") }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onUpdateHeader: (String) -> Unit,
    onUpdatePlan: (PlanTier) -> Unit,
    onUpdateLogo: (Uri?) -> Unit,
    onReset: () -> Unit
) {
    var header by remember(settings.headerText) { mutableStateOf(settings.headerText) }
    var showReset by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> onUpdateLogo(uri) }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = header, onValueChange = { header = it }, label = { Text("Header text") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onUpdateHeader(header) }) { Text("Save Header") }
        if (settings.planTier == PlanTier.PRO) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { picker.launch("image/*") }) { Text("Upload Logo") }
                Button(onClick = { onUpdateLogo(null) }) { Text("Clear Logo") }
            }
        } else {
            Text("Logo customization is PRO only")
        }
        settings.logoPath?.let {
            BitmapFactory.decodeFile(it)?.let { bmp -> Image(bitmap = bmp.asImageBitmap(), contentDescription = "logo", modifier = Modifier.size(100.dp)) }
        }
        Text("Plan Tier")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onUpdatePlan(PlanTier.FREE) }) { Text("FREE") }
            Button(onClick = { onUpdatePlan(PlanTier.PRO) }) { Text("PRO") }
        }
        Button(onClick = { showReset = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Factory Reset") }
        if (showReset) {
            Text("Confirm reset?")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onReset(); showReset = false }) { Text("Yes") }
                Button(onClick = { showReset = false }) { Text("No") }
            }
        }
        TextButton(onClick = onBack) { Text("Back") }
    }
}

@Composable
private fun GenericDocForm(
    docType: String,
    settings: AppSettings,
    onBack: () -> Unit,
    onGenerated: (RenderedDocument, DocumentType) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var line1 by remember { mutableStateOf("") }
    var line2 by remember { mutableStateOf("") }
    var line3 by remember { mutableStateOf("") }
    var urgent by remember { mutableStateOf(false) }
    val now = SystemTimeProvider.now()
    val timing = TimingConfig(now.minusMinutes(20), now)
    val patient = PatientDemographics(name = name, age = age, gender = gender, phone = phone)

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(docType.replace('_', ' ').uppercase(), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(name, { name = it }, label = { Text("Patient Name*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(age, { age = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(gender, { gender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(line1, { line1 = it }, label = { Text("Field 1") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(line2, { line2 = it }, label = { Text("Field 2") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(line3, { line3 = it }, label = { Text("Field 3") }, modifier = Modifier.fillMaxWidth())
        if (docType == "radiology_request_slip") Row(verticalAlignment = Alignment.CenterVertically) { Text("Urgent"); Switch(urgent, { urgent = it }) }
        Button(onClick = {
            val branding = BrandingConfig(settings.headerText, settings.logoPath)
            val rendered = when (docType) {
                "medical_certificate" -> MedicalCertificateRenderer.render(context, DocumentPayload.MedicalCertificatePayload(patient, line1.ifBlank { "Leave" }, line2, java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(3), line3), branding, timing) to DocumentType.MEDICAL_CERTIFICATE
                "prescription" -> PrescriptionRenderer.render(context, DocumentPayload.PrescriptionPayload(patient, line1, listOf(DocumentPayload.PrescriptionPayload.MedicineRow(line2, line3, "", ""))), branding, timing) to DocumentType.PRESCRIPTION
                "lab_request_slip" -> LabRequestRenderer.render(context, DocumentPayload.LabRequestPayload(patient, listOfNotNull(line1.takeIf { it.isNotBlank() }, line2.takeIf { it.isNotBlank() }), line3), branding, timing) to DocumentType.LAB_REQUEST_SLIP
                else -> RadiologyRequestRenderer.render(context, DocumentPayload.RadiologyRequestPayload(patient, line1, line2, line3, urgent), branding, timing) to DocumentType.RADIOLOGY_REQUEST_SLIP
            }
            onGenerated(rendered.first, rendered.second)
        }, enabled = name.isNotBlank()) { Text("Generate PDF") }
        TextButton(onClick = onBack) { Text("Back") }
    }
}

private fun copyLogoToInternal(context: Context, uri: Uri): String {
    val outFile = File(context.filesDir, "branding_logo.png")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(outFile).use { output -> input.copyTo(output) }
    }
    return outFile.absolutePath
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

package com.alshifa.rapidocusg

import android.content.Context
import android.net.Uri
import android.content.pm.ApplicationInfo
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import com.alshifa.rapidocusg.core.documentengine.LeaveCertificateRenderer
import com.alshifa.rapidocusg.core.documentengine.FitnessCertificateRenderer
import com.alshifa.rapidocusg.ui.screens.LeaveCertificateFormScreen
import com.alshifa.rapidocusg.ui.screens.FitnessCertificateFormScreen
import com.alshifa.rapidocusg.ui.screens.ParserDictionaryScreen
import com.alshifa.rapidocusg.core.parser.ChatParser
import com.alshifa.rapidocusg.core.parser.ParseResult
import com.alshifa.rapidocusg.core.parser.SystemKeywords
import com.alshifa.rapidocusg.core.parser.ParseConfidence
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
import java.io.IOException
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

    var parseResult by remember { mutableStateOf<ParseResult?>(null) }
    var rawQuickEntry by remember { mutableStateOf("") }
    var showDocPicker by remember { mutableStateOf(false) }
    var initialFields by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }

    LaunchedEffect(reportInput.findings) {
        prefs.edit().putString("pref_findings", gson.toJson(reportInput.findings)).apply()
    }

    val reportBody = remember(reportInput) { RulesEngine.buildReport(reportInput) }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            Scaffold { padding ->
                Column(Modifier.padding(padding).fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("RapiDoc", style = MaterialTheme.typography.headlineSmall)
                    
                    Text("Quick Entry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = rawQuickEntry,
                            onValueChange = { rawQuickEntry = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Command") }
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val mapType = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                            val mappings: Map<String, String> = if (settings.parserSynonymsJson.isBlank() || settings.parserSynonymsJson == "{}") {
                                emptyMap()
                            } else {
                                runCatching { gson.fromJson<Map<String, String>>(settings.parserSynonymsJson, mapType) }.getOrDefault(emptyMap())
                            }
                            val res = ChatParser.parse(rawQuickEntry, mappings)
                            parseResult = res
                            initialFields = res.filledFields
                            if (res.confidence == ParseConfidence.LOW || res.confidence == ParseConfidence.NONE || res.detectedDocType == null) {
                                showDocPicker = true
                            } else {
                                // Navigate mapped fields directly
                                if (res.detectedDocType == DocumentType.USG_ABDOMEN) {
                                    reportInput = reportInput.copy(
                                        patient = reportInput.patient.copy(
                                            name = (res.filledFields[SystemKeywords.KEY_NAME] as? String) ?: reportInput.patient.name,
                                            ageYears = (res.filledFields[SystemKeywords.KEY_AGE] as? String) ?: reportInput.patient.ageYears,
                                            sex = when ((res.filledFields[SystemKeywords.KEY_SEX] as? String)?.lowercase()) {
                                                "male", "m" -> Sex.Male
                                                "female", "f" -> Sex.Female
                                                else -> reportInput.patient.sex
                                            },
                                        )
                                    )
                                    navController.navigate("doc/usg_abdomen/form")
                                } else {
                                    val route = DocumentRegistry.byType(res.detectedDocType).formRoute
                                    navController.navigate(route)
                                }
                            }
                        }) { Text("Parse") }
                    }

                    if (parseResult != null) {
                        Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFE3F2FD)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Parsed Summary", fontWeight = FontWeight.Bold)
                            val res = parseResult!!
                            Text("Detected: ${res.detectedDocType?.name ?: "Unknown"} [${res.confidence.name}]", color = if (res.confidence == ParseConfidence.HIGH) Color(0xFF2E7D32) else Color(0xFFC62828))
                            Text("Filled: ${res.filledFields}")
                            if (res.missingRequired.isNotEmpty()) Text("Missing: ${res.missingRequired}", color = Color.Red)
                            if (res.unknownTokens.isNotEmpty()) Text("Unknown: ${res.unknownTokens}", color = Color(0xFFE65100))
                        }
                    }

                    if (showDocPicker) {
                        AlertDialog(
                            onDismissRequest = { showDocPicker = false },
                            title = { Text("Choose Document") },
                            text = { Text("Could not determine document type. Please select:") },
                            confirmButton = {},
                            dismissButton = {
                                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DocumentRegistry.docs.forEach { m ->
                                        Button(onClick = {
                                            showDocPicker = false
                                            if (m.type == DocumentType.USG_ABDOMEN) {
                                                reportInput = reportInput.copy(
                                                    patient = reportInput.patient.copy(
                                                        name = (initialFields[SystemKeywords.KEY_NAME] as? String) ?: reportInput.patient.name,
                                                        ageYears = (initialFields[SystemKeywords.KEY_AGE] as? String) ?: reportInput.patient.ageYears,
                                                        sex = when ((initialFields[SystemKeywords.KEY_SEX] as? String)?.lowercase()) {
                                                            "male", "m" -> Sex.Male
                                                            "female", "f" -> Sex.Female
                                                            else -> reportInput.patient.sex
                                                        },
                                                    )
                                                )
                                                navController.navigate(m.formRoute)
                                            } else {
                                                navController.navigate(m.formRoute)
                                            }
                                        }, modifier = Modifier.fillMaxWidth()) { Text(m.displayName) }
                                    }
                                    TextButton(onClick = { showDocPicker = false }) { Text("Cancel") }
                                }
                            }
                        )
                    }

                    HorizontalDivider()
                    DocumentRegistry.docs.forEach { meta ->
                        Button(onClick = {
                            navController.navigate(meta.formRoute)
                        }, enabled = true, modifier = Modifier.fillMaxWidth()) {
                            Text(meta.displayName)
                        }
                    }
                    TextButton(onClick = { navController.navigate("settings") }) { Text("Settings") }
                    TextButton(onClick = { navController.navigate("dictionary") }) { Text("Parser Dictionary") }
                }
            }
        }
        composable("dictionary") {
            ParserDictionaryScreen(
                settings = settings,
                settingsStore = settingsStore,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(settings = settings, onBack = { navController.popBackStack() }, onUpdateHeader = {
                scope.launch { settingsStore.updateHeader(it) }
            }, onUpdateLogo = { uri ->
                scope.launch {
                    if (uri == null) {
                        settingsStore.updateLogo(null)
                    } else {
                        val path = copyLogoToInternal(context, uri)
                        if (path != null) {
                            settingsStore.updateLogo(path)
                        }
                    }
                }
            }, onReset = {
                scope.launch {
                    settings.logoPath?.let { runCatching { File(it).delete() } }
                    settingsStore.resetFactory()
                }
            })
        }

        composable("doc/usg_abdomen/form") {
            QuickEntryScreen(
                modifier = Modifier.fillMaxSize(),
                reportInput = reportInput,
                forceNormal = forceNormal,
                onInputChange = { reportInput = it; forceNormal = false },
                onForceNormal = { enabled -> forceNormal = enabled; if (enabled) reportInput = reportInput.copy(findings = FindingsInput()) },
                onLoadNormal = { reportInput = reportInput.copy(findings = FindingsInput()); forceNormal = false },
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
                onNewReport = {
                    reportInput = ReportInput(PatientInfo(), FindingsInput())
                    forceNormal = false
                    currentDocument = null
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onShare = { currentDocument?.let { PdfActions.share(context, it.pdfFile) } ?: Toast.makeText(context, "Generate PDF first.", Toast.LENGTH_SHORT).show() }
            )
        }
        composable("doc/medical_leave_cert/form") {
            LeaveCertificateFormScreen(
                settings = settings,
                initialFields = initialFields,
                onBack = { navController.popBackStack() },
                onGenerated = { rendered, type -> currentDocument = rendered; currentDocType = type; navController.navigate("doc/medical_leave_cert/preview") }
            )
        }
        composable("doc/medical_fitness_cert/form") {
            FitnessCertificateFormScreen(
                settings = settings,
                initialFields = initialFields,
                onBack = { navController.popBackStack() },
                onGenerated = { rendered, type -> currentDocument = rendered; currentDocType = type; navController.navigate("doc/medical_fitness_cert/preview") }
            )
        }
        composable("doc/{docType}/preview", arguments = listOf(navArgument("docType") { type = NavType.StringType })) { backStack ->
            val docTypeArg = backStack.arguments?.getString("docType").orEmpty()
            if (docTypeArg.isBlank()) {
                LaunchedEffect(Unit) {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                }
                return@composable
            }
            if (docTypeArg == "usg_abdomen") {
                LaunchedEffect(docTypeArg) {
                    navController.navigate("doc/usg_abdomen/preview") {
                        popUpTo("doc/{docType}/preview") { inclusive = true }
                        launchSingleTop = true
                    }
                }
                return@composable
            }
            val docTypeEnum = docTypeFromRouteArg(docTypeArg)
            if (docTypeEnum == null) {
                LaunchedEffect(docTypeArg) { navController.navigate("home") }
                return@composable
            }
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Preview", style = MaterialTheme.typography.headlineSmall)
                Text("${currentDocument?.displayName ?: "Document"}")
                PdfPreviewBox(file = currentDocument?.pdfFile)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { currentDocument?.let { PdfActions.print(context, it.pdfFile) } }) { Text("Print") }
                    Button(onClick = { currentDocument?.let { PdfActions.share(context, it.pdfFile) } }) { Text("Share") }
                }
                TextButton(onClick = { navController.navigate("doc/$docTypeArg/form") }) { Text("Back to Form") }
                TextButton(onClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                }) { Text("Home") }
            }
        }
    }
}

private fun docTypeFromRouteArg(docTypeArg: String): DocumentType? = when (docTypeArg) {
    "usg_abdomen" -> DocumentType.USG_ABDOMEN
    "medical_leave_cert" -> DocumentType.MEDICAL_LEAVE_CERT
    "medical_fitness_cert" -> DocumentType.MEDICAL_FITNESS_CERT
    else -> null
}

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onUpdateHeader: (String) -> Unit,
    onUpdateLogo: (Uri?) -> Unit,
    onReset: () -> Unit
) {
    var header by remember(settings.headerText) { mutableStateOf(settings.headerText) }
    var showReset by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isDebugBuild = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> onUpdateLogo(uri) }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = header, onValueChange = { header = it }, label = { Text("Header text") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onUpdateHeader(header) }) { Text("Save Header") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { picker.launch("image/*") }) { Text("Upload Logo") }
            Button(onClick = { onUpdateLogo(null) }) { Text("Clear Logo") }
        }
        settings.logoPath?.let {
            BitmapFactory.decodeFile(it)?.let { bmp -> Image(bitmap = bmp.asImageBitmap(), contentDescription = "logo", modifier = Modifier.size(100.dp)) }
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



private fun copyLogoToInternal(context: Context, uri: Uri): String? {
    val input = context.contentResolver.openInputStream(uri) ?: return null
    val outFile = File(context.filesDir, "branding_logo.png")
    return try {
        input.use { stream ->
            FileOutputStream(outFile).use { output -> stream.copyTo(output) }
        }
        outFile.absolutePath
    } catch (_: IOException) {
        runCatching { outFile.delete() }
        null
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
    val context = LocalContext.current
    val isDebugBuild = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

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
            OutlinedTextField(value = findings.stoneRightMm, onValueChange = { 
                val newSize = it.filter(Char::isDigit)
                val newLoc = if (newSize.isEmpty() || (newSize.toIntOrNull() ?: 0) <= 0) null else findings.stoneRightLocation ?: StoneLocation.RENAL_PELVIS
                onInputChange(reportInput.copy(findings = findings.copy(stoneRightMm = newSize, stoneRightLocation = newLoc)))
            },
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Right Renal Stone (mm)") })
            if (findings.stoneRightMm.isNotEmpty() && (findings.stoneRightMm.toIntOrNull() ?: 0) > 0) {
                EnumSelector(
                    label = "Right stone location",
                    options = StoneLocation.entries,
                    selected = findings.stoneRightLocation ?: StoneLocation.RENAL_PELVIS,
                    enabled = true,
                    itemLabel = { it.displayName },
                    onSelect = { onInputChange(reportInput.copy(findings = findings.copy(stoneRightLocation = it))) }
                )
            }
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
            OutlinedTextField(value = findings.stoneLeftMm, onValueChange = { 
                val newSize = it.filter(Char::isDigit)
                val newLoc = if (newSize.isEmpty() || (newSize.toIntOrNull() ?: 0) <= 0) null else findings.stoneLeftLocation ?: StoneLocation.RENAL_PELVIS
                onInputChange(reportInput.copy(findings = findings.copy(stoneLeftMm = newSize, stoneLeftLocation = newLoc)))
            },
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Left Renal Stone (mm)") })
            if (findings.stoneLeftMm.isNotEmpty() && (findings.stoneLeftMm.toIntOrNull() ?: 0) > 0) {
                EnumSelector(
                    label = "Left stone location",
                    options = StoneLocation.entries,
                    selected = findings.stoneLeftLocation ?: StoneLocation.RENAL_PELVIS,
                    enabled = true,
                    itemLabel = { it.displayName },
                    onSelect = { onInputChange(reportInput.copy(findings = findings.copy(stoneLeftLocation = it))) }
                )
            }
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

        if (isDebugBuild) {
            Spacer(Modifier.height(4.dp))
            Text("Load Sample Case", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLoadNormal) { Text("Normal") }
            }
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

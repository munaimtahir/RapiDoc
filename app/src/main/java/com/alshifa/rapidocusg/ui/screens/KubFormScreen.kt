package com.alshifa.rapidocusg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.alshifa.rapidocusg.*
import com.alshifa.rapidocusg.core.documentengine.*
import com.alshifa.rapidocusg.core.parser.SystemKeywords

@Composable
fun KubFormScreen(
    settings: AppSettings,
    initialFields: Map<String, Any> = emptyMap(),
    onBack: () -> Unit,
    onGenerated: (RenderedDocument, DocumentType) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf((initialFields[SystemKeywords.KEY_NAME] as? String) ?: "") }
    var age by remember { mutableStateOf((initialFields[SystemKeywords.KEY_AGE] as? String) ?: "") }
    var gender by remember { mutableStateOf((initialFields[SystemKeywords.KEY_SEX] as? String)?.lowercase()?.let {
        if (it == "f" || it == "female") Sex.Female else Sex.Male
    } ?: Sex.Male) }
    var patientId by remember { mutableStateOf((initialFields[SystemKeywords.KEY_ID] as? String) ?: "") }

    var findings by remember { mutableStateOf(KubFindingsInput()) }
    var forceNormal by remember { mutableStateOf(false) }

    val isValidName = name.trim().length >= 2
    val isValidAge = age.toIntOrNull() in 0..120
    val isValid = isValidName && isValidAge

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("USG KUB / Renal Tract", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Patient Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Normal Toggle (resets input)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = forceNormal, onCheckedChange = { 
                        forceNormal = it
                        if (it) findings = KubFindingsInput()
                    })
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.trimStart() },
                    isError = !isValidName && name.isNotEmpty(),
                    label = { Text("Patient Name*") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = patientId,
                    onValueChange = { patientId = it.trim() },
                    label = { Text("Patient ID (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter(Char::isDigit) },
                    isError = !isValidAge && age.isNotEmpty(),
                    label = { Text("Age (Years)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                EnumSelector(
                    label = "Gender*",
                    options = Sex.entries,
                    selected = gender,
                    enabled = true,
                    onSelect = { gender = it }
                )
            }
        }

        OrganSection("Right Kidney", findings.rkPrintMode, forceNormal,
            onModeChange = { findings = findings.copy(rkPrintMode = it) }) {
            EnumSelector(label = "Right CMD", options = CmdState.entries, selected = findings.rkCmd, enabled = true,
                onSelect = { findings = findings.copy(rkCmd = it) })
            EnumSelector(label = "Hydronephrosis", options = Hydronephrosis.entries, selected = findings.hydronephrosisRight, enabled = true,
                onSelect = { findings = findings.copy(hydronephrosisRight = it) })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Right Renal Stone")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.stoneRightPresent, onCheckedChange = { findings = findings.copy(stoneRightPresent = it) })
            }
            if (findings.stoneRightPresent) {
                OutlinedTextField(value = findings.stoneRightMm, onValueChange = { findings = findings.copy(stoneRightMm = it.filter(Char::isDigit)) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Stone Size (mm)*") })
                EnumSelector(
                    label = "Right stone location",
                    options = StoneLocation.entries,
                    selected = findings.stoneRightLocation ?: StoneLocation.RENAL_PELVIS,
                    enabled = true,
                    itemLabel = { it.displayName },
                    onSelect = { findings = findings.copy(stoneRightLocation = it) }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Renal Cyst Right")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.renalCystRight, onCheckedChange = { findings = findings.copy(renalCystRight = it) })
            }
            if (findings.renalCystRight) {
                OutlinedTextField(value = findings.renalCystRightSizeMm, onValueChange = { findings = findings.copy(renalCystRightSizeMm = it.filter(Char::isDigit)) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Cyst Size (mm)") })
            }
        }

        OrganSection("Left Kidney", findings.lkPrintMode, forceNormal,
            onModeChange = { findings = findings.copy(lkPrintMode = it) }) {
            EnumSelector(label = "Left CMD", options = CmdState.entries, selected = findings.lkCmd, enabled = true,
                onSelect = { findings = findings.copy(lkCmd = it) })
            EnumSelector(label = "Hydronephrosis", options = Hydronephrosis.entries, selected = findings.hydronephrosisLeft, enabled = true,
                onSelect = { findings = findings.copy(hydronephrosisLeft = it) })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Left Renal Stone")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.stoneLeftPresent, onCheckedChange = { findings = findings.copy(stoneLeftPresent = it) })
            }
            if (findings.stoneLeftPresent) {
                OutlinedTextField(value = findings.stoneLeftMm, onValueChange = { findings = findings.copy(stoneLeftMm = it.filter(Char::isDigit)) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Stone Size (mm)*") })
                EnumSelector(
                    label = "Left stone location",
                    options = StoneLocation.entries,
                    selected = findings.stoneLeftLocation ?: StoneLocation.RENAL_PELVIS,
                    enabled = true,
                    itemLabel = { it.displayName },
                    onSelect = { findings = findings.copy(stoneLeftLocation = it) }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Renal Cyst Left")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.renalCystLeft, onCheckedChange = { findings = findings.copy(renalCystLeft = it) })
            }
            if (findings.renalCystLeft) {
                OutlinedTextField(value = findings.renalCystLeftSizeMm, onValueChange = { findings = findings.copy(renalCystLeftSizeMm = it.filter(Char::isDigit)) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Cyst Size (mm)") })
            }
        }

        EnumSelector(
            label = "Obstruction",
            options = Obstruction.entries,
            selected = findings.obstruction,
            enabled = !forceNormal,
            itemLabel = { it.name.replace("_", " ") },
            onSelect = { findings = findings.copy(obstruction = it) }
        )

        OrganSection("Urinary Bladder", findings.bladderPrintMode, forceNormal,
            onModeChange = { findings = findings.copy(bladderPrintMode = it) }) {
            EnumSelector(label = "Bladder Wall Status", options = BladderWallStatus.entries, selected = findings.bladderWallStatus, enabled = true,
                onSelect = { findings = findings.copy(bladderWallStatus = it) })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bladder Stone")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.bladderStone, onCheckedChange = { findings = findings.copy(bladderStone = it) })
            }
            if (findings.bladderStone) {
                OutlinedTextField(value = findings.bladderStoneSizeMm, onValueChange = { findings = findings.copy(bladderStoneSizeMm = it.filter(Char::isDigit)) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Stone Size (mm)") })
            }
            EnumSelector(label = "Post Void Residual", options = PostVoidResidual.entries, selected = findings.postVoidResidual, enabled = true,
                itemLabel = { it.name.replace("_", " ") },
                onSelect = { findings = findings.copy(postVoidResidual = it) })
        }

        if (gender == Sex.Male) {
            OrganSection("Prostate", findings.prostatePrintMode, forceNormal,
                onModeChange = { findings = findings.copy(prostatePrintMode = it) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Prostate Enlarged")
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = findings.prostateEnlarged, onCheckedChange = { findings = findings.copy(prostateEnlarged = it) })
                }
                if (findings.prostateEnlarged) {
                    OutlinedTextField(value = findings.prostateVolCc, onValueChange = { findings = findings.copy(prostateVolCc = it.filter(Char::isDigit)) },
                        modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Volume (cc)") })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val now = SystemTimeProvider.now()
                val patient = PatientInfo(
                    name = name,
                    ageYears = age,
                    sex = gender,
                    patientId = patientId.ifBlank { "" },
                    bookingDateTime = now.minusMinutes(20),
                    reportingDateTime = now
                )
                val payload = DocumentPayload.UsgKubPayload(KubReportInput(patient, findings))
                val rendered = KubRenderer.render(context, payload, BrandingConfig(settings.headerText, settings.logoPath), TimingConfig(now.minusMinutes(20), now))
                onGenerated(rendered, DocumentType.USG_KUB)
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Generate PDF Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

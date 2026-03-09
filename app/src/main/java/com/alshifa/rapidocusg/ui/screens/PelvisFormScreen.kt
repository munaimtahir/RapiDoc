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
fun PelvisFormScreen(
    settings: AppSettings,
    initialFields: Map<String, Any> = emptyMap(),
    onBack: () -> Unit,
    onGenerated: (RenderedDocument, DocumentType) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf((initialFields[SystemKeywords.KEY_NAME] as? String) ?: "") }
    var age by remember { mutableStateOf((initialFields[SystemKeywords.KEY_AGE] as? String) ?: "") }
    var patientId by remember { mutableStateOf((initialFields[SystemKeywords.KEY_ID] as? String) ?: "") }

    var findings by remember { mutableStateOf(PelvisFindingsInput()) }
    var forceNormal by remember { mutableStateOf(false) }

    val isValidName = name.trim().length >= 2
    val isValidAge = age.toIntOrNull() in 0..120
    val isValid = isValidName && isValidAge

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("USG Pelvis (Female)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Patient Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Normal Toggle (resets input)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = forceNormal, onCheckedChange = { 
                        forceNormal = it
                        if (it) findings = PelvisFindingsInput()
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
            }
        }

        OrganSection("Uterus", findings.uterusPrintMode, forceNormal, onModeChange = { findings = findings.copy(uterusPrintMode = it) }) {
            EnumSelector(label = "Uterus Status", options = UterusStatus.entries, selected = findings.uterusStatus, enabled = true,
                onSelect = { findings = findings.copy(uterusStatus = it) })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Fibroid")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.fibroid, onCheckedChange = { findings = findings.copy(fibroid = it) })
            }
            if (findings.fibroid) {
                OutlinedTextField(value = findings.fibroidSizeMm, onValueChange = { findings = findings.copy(fibroidSizeMm = it.filter(Char::isDigit)) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Fibroid Size (mm)") })
            }
        }

        OrganSection("Endometrium", findings.endoPrintMode, forceNormal, onModeChange = { findings = findings.copy(endoPrintMode = it) }) {
            OutlinedTextField(value = findings.endoThicknessMm, onValueChange = { findings = findings.copy(endoThicknessMm = it.filter(Char::isDigit)) },
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Endometrial Thickness (mm)") })
        }

        OrganSection("Right Ovary", findings.roPrintMode, forceNormal, onModeChange = { findings = findings.copy(roPrintMode = it) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ovarian Cyst")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.cystRight, onCheckedChange = { findings = findings.copy(cystRight = it) })
            }
            if (findings.cystRight) {
                OutlinedTextField(value = findings.cystSizeRightMm, onValueChange = { findings = findings.copy(cystSizeRightMm = it.filter(Char::isDigit)) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Cyst Size (mm)") })
            }
        }

        OrganSection("Left Ovary", findings.loPrintMode, forceNormal, onModeChange = { findings = findings.copy(loPrintMode = it) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ovarian Cyst")
                Spacer(Modifier.width(8.dp))
                Switch(checked = findings.cystLeft, onCheckedChange = { findings = findings.copy(cystLeft = it) })
            }
            if (findings.cystLeft) {
                OutlinedTextField(value = findings.cystSizeLeftMm, onValueChange = { findings = findings.copy(cystSizeLeftMm = it.filter(Char::isDigit)) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Cyst Size (mm)") })
            }
        }

        OrganSection("Adnexa / Pouch of Douglas", findings.adnexaPrintMode, forceNormal, onModeChange = { findings = findings.copy(adnexaPrintMode = it) }) {
            EnumSelector(label = "Free Fluid", options = FreeFluid.entries, selected = findings.freeFluid, enabled = true,
                itemLabel = { it.name.replace("_", " ") },
                onSelect = { findings = findings.copy(freeFluid = it) })
        }

        OrganSection("Urinary Bladder", findings.bladderPrintMode, forceNormal, onModeChange = { findings = findings.copy(bladderPrintMode = it) }) {
            // No specific findings for Bladder in Pelvis other than abnormal
        }


        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val now = SystemTimeProvider.now()
                val patient = PatientInfo(
                    name = name,
                    ageYears = age,
                    sex = Sex.Female,
                    patientId = patientId.ifBlank { "" },
                    bookingDateTime = now.minusMinutes(20),
                    reportingDateTime = now
                )
                val payload = DocumentPayload.UsgPelvisPayload(PelvisReportInput(patient, findings))
                val rendered = PelvisRenderer.render(context, payload, BrandingConfig(settings.headerText, settings.logoPath), TimingConfig(now.minusMinutes(20), now))
                onGenerated(rendered, DocumentType.USG_PELVIS)
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

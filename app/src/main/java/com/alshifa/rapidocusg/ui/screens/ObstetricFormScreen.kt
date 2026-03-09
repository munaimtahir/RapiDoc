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
fun ObstetricFormScreen(
    settings: AppSettings,
    initialFields: Map<String, Any> = emptyMap(),
    onBack: () -> Unit,
    onGenerated: (RenderedDocument, DocumentType) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf((initialFields[SystemKeywords.KEY_NAME] as? String) ?: "") }
    var age by remember { mutableStateOf((initialFields[SystemKeywords.KEY_AGE] as? String) ?: "") }
    var patientId by remember { mutableStateOf((initialFields[SystemKeywords.KEY_ID] as? String) ?: "") }

    var findings by remember { mutableStateOf(ObstetricFindingsInput()) }

    val isValidName = name.trim().length >= 2
    val isValidAge = age.toIntOrNull() in 0..120
    val isValidGaWeeks = (findings.gaWeeks.toIntOrNull() ?: 0) > 0
    val isValid = isValidName && isValidAge && isValidGaWeeks

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("USG Obstetric", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Patient Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                
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

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Fetal Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                
                EnumSelector(label = "Pregnancy Status", options = PregStatus.entries, selected = findings.pregStatus, enabled = true,
                    itemLabel = { it.name.replace("_", " ") },
                    onSelect = { findings = findings.copy(pregStatus = it) })

                if (findings.pregStatus == PregStatus.LIVE_IUP) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Fetal Heart Rate Present")
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = findings.fhrPresent, onCheckedChange = { findings = findings.copy(fhrPresent = it) })
                    }
                    if (findings.fhrPresent) {
                        OutlinedTextField(value = findings.fhrBpm, onValueChange = { findings = findings.copy(fhrBpm = it.filter(Char::isDigit)) },
                            modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("FHR (bpm)") })
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = findings.gaWeeks, onValueChange = { findings = findings.copy(gaWeeks = it.filter(Char::isDigit)) },
                        modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("GA Weeks*") }, isError = !isValidGaWeeks && findings.gaWeeks.isNotEmpty())
                    OutlinedTextField(value = findings.gaDays, onValueChange = { findings = findings.copy(gaDays = it.filter(Char::isDigit)) },
                        modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("GA Days") })
                }
            }
        }

        if (findings.pregStatus != PregStatus.EMPTY_SAC) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Obstetric Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    
                    EnumSelector(label = "Presentation", options = FetalPresentation.entries, selected = findings.presentation, enabled = true,
                        itemLabel = { it.name.replace("_", " ") },
                        onSelect = { findings = findings.copy(presentation = it) })

                    EnumSelector(label = "Placenta", options = PlacentaLocation.entries, selected = findings.placenta, enabled = true,
                        itemLabel = { it.name.replace("_", " ") },
                        onSelect = { findings = findings.copy(placenta = it) })

                    EnumSelector(label = "Liquor Volume", options = LiquorVolume.entries, selected = findings.liquor, enabled = true,
                        itemLabel = { it.name.replace("_", " ") },
                        onSelect = { findings = findings.copy(liquor = it) })

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Cervix Closed / Normal")
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = findings.cervixClosed, onCheckedChange = { findings = findings.copy(cervixClosed = it) })
                    }
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
                    sex = Sex.Female,
                    patientId = patientId.ifBlank { "" },
                    bookingDateTime = now.minusMinutes(20),
                    reportingDateTime = now
                )
                val payload = DocumentPayload.UsgObstetricPayload(ObstetricReportInput(patient, findings))
                val rendered = ObstetricRenderer.render(context, payload, BrandingConfig(settings.headerText, settings.logoPath), TimingConfig(now.minusMinutes(20), now))
                onGenerated(rendered, DocumentType.USG_OBSTETRIC)
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

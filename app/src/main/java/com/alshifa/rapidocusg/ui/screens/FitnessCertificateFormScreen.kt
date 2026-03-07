package com.alshifa.rapidocusg.ui.screens

import android.widget.Toast
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
import com.alshifa.rapidocusg.core.documentengine.*
import com.alshifa.rapidocusg.core.parser.SystemKeywords
import java.time.LocalDateTime

@Composable
fun FitnessCertificateFormScreen(
    settings: AppSettings,
    initialFields: Map<String, Any> = emptyMap(),
    onBack: () -> Unit,
    onGenerated: (RenderedDocument, DocumentType) -> Unit
) {
    val context = LocalContext.current
    var name by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_NAME] as? String) ?: "") }
    var age by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_AGE] as? String) ?: "") }
    var gender by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_SEX] as? String) ?: "") }
    var patientId by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_ID] as? String) ?: "") }
    
    val purposes = listOf("FIT_FOR_DUTY", "FIT_FOR_SCHOOL", "FIT_FOR_TRAVEL", "FIT_FOR_ADMISSION", "OTHER")
    var selectedPurpose by remember { mutableStateOf(purposes[0]) }
    var otherPurposeText by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_PURPOSE] as? String) ?: "") }
    
    LaunchedEffect(initialFields) {
        if (initialFields.containsKey(SystemKeywords.KEY_PURPOSE)) selectedPurpose = "OTHER"
    }

    val restrictions = listOf("NONE", "LIGHT_DUTY", "AVOID_HEAVY_LIFTING", "OTHER")
    var selectedRestriction by remember { mutableStateOf(restrictions[0]) }
    var otherRestrictionsText by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_RESTRICTIONS] as? String) ?: "") }

    LaunchedEffect(initialFields) {
        if (initialFields.containsKey(SystemKeywords.KEY_RESTRICTIONS)) selectedRestriction = "OTHER"
    }

    var remarks by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_REMARKS] as? String) ?: "") }

    val resolvedPurpose = if (selectedPurpose == "OTHER") otherPurposeText else selectedPurpose.replace("_", " ").lowercase()
    val resolvedRestriction = if (selectedRestriction == "OTHER") otherRestrictionsText else selectedRestriction.replace("_", " ").lowercase()

    val isValidName = name.trim().length >= 2
    val isValidAge = age.toIntOrNull() in 0..120
    val isValidSex = gender.equals("Male", true) || gender.equals("Female", true) || gender.equals("m", true) || gender.equals("f", true)

    val isValid = isValidName && isValidAge && isValidSex &&
            (selectedPurpose != "OTHER" || otherPurposeText.isNotBlank()) &&
            (selectedRestriction != "OTHER" || otherRestrictionsText.isNotBlank()) &&
            remarks.length <= 120 && otherPurposeText.length <= 60 && otherRestrictionsText.length <= 60

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Medical Fitness Certificate", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.trimStart() },
            isError = !isValidName && name.isNotEmpty(),
            label = { Text("Patient Name*") },
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
        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it.trim() },
            isError = !isValidSex && gender.isNotEmpty(),
            label = { Text("Gender (Male/Female)*") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = patientId,
            onValueChange = { patientId = it.trim() },
            label = { Text("Patient ID (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(8.dp))
        Text("Fitness Details", fontWeight = FontWeight.SemiBold)
        
        Text("Purpose:*")
        purposes.forEach { p ->
            Row {
                RadioButton(selected = selectedPurpose == p, onClick = { selectedPurpose = p })
                Text(p.replace("_", " "))
            }
        }
        if (selectedPurpose == "OTHER") {
            OutlinedTextField(otherPurposeText, { if(it.length <= 60) otherPurposeText = it }, label = { Text("Specify Purpose*") }, modifier = Modifier.fillMaxWidth())
        }

        Text("Restrictions:*")
        restrictions.forEach { r ->
            Row {
                RadioButton(selected = selectedRestriction == r, onClick = { selectedRestriction = r })
                Text(r.replace("_", " "))
            }
        }
        if (selectedRestriction == "OTHER") {
            OutlinedTextField(otherRestrictionsText, { if(it.length <= 60) otherRestrictionsText = it }, label = { Text("Specify Restriction*") }, modifier = Modifier.fillMaxWidth())
        }

        OutlinedTextField(remarks, { if(it.length <= 120) remarks = it }, label = { Text("Remarks (Optional)") }, modifier = Modifier.fillMaxWidth())
        
        Text("Computed Purpose: $resolvedPurpose", color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val branding = BrandingConfig(settings.headerText, settings.logoPath)
                val now = SystemTimeProvider.now()
                val timing = TimingConfig(now.minusMinutes(20), now)
                
                val patient = PatientDemographics(
                    name = name,
                    age = age,
                    gender = gender,
                    patientId = patientId.ifBlank { null }
                )
                
                val payload = DocumentPayload.FitnessCertificatePayload(
                    patient = patient,
                    issueDateTime = now,
                    purposeText = resolvedPurpose,
                    restrictionsText = if (selectedRestriction == "NONE") null else resolvedRestriction,
                    remarks = remarks
                )
                
                val rendered = FitnessCertificateRenderer.render(context, payload, branding, timing)
                onGenerated(rendered, DocumentType.MEDICAL_FITNESS_CERT)
            }, enabled = isValid) { Text("Generate PDF") }
            
            var showResetDialog by remember { mutableStateOf(false) }
            Button(onClick = { showResetDialog = true }) { Text("Reset") }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Reset Form") },
                    text = { Text("Are you sure you want to clear all form data?") },
                    confirmButton = {
                        TextButton(onClick = {
                            name = ""; age = ""; gender = ""; patientId = ""; selectedPurpose = purposes[0]; otherPurposeText = ""; selectedRestriction = restrictions[0]; otherRestrictionsText = ""; remarks = ""
                            showResetDialog = false
                        }) { Text("Yes, Reset") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
        TextButton(onClick = onBack) { Text("Back") }
    }
}

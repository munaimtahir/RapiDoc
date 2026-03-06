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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun LeaveCertificateFormScreen(
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
    
    var diagnosisOrReason by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_DX] as? String) ?: (initialFields[SystemKeywords.KEY_REASON] as? String) ?: "") }
    var durationDaysStr by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_DAYS] as? String) ?: "3") }
    var startDateStr by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_START] as? String) ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var notes by remember(initialFields) { mutableStateOf((initialFields[SystemKeywords.KEY_REMARKS] as? String) ?: "") }

    val durationDays = durationDaysStr.toIntOrNull()
    var startDate: LocalDate? = null
    try {
        startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: DateTimeParseException) {
        // ignore
    }

    val endDate = if (startDate != null && durationDays != null && durationDays > 0) {
        startDate.plusDays((durationDays - 1).toLong())
    } else null

    val isValid = name.isNotBlank() && age.isNotBlank() && (gender.equals("Male", true) || gender.equals("Female", true) || gender.equals("m", true) || gender.equals("f", true)) &&
            diagnosisOrReason.isNotBlank() && startDate != null && durationDays != null && durationDays in 1..30 &&
            diagnosisOrReason.length <= 120 && notes.length <= 120

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Medical Leave Certificate", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(name, { name = it }, label = { Text("Patient Name*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(age, { age = it.filter(Char::isDigit) }, label = { Text("Age (Years)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(gender, { gender = it }, label = { Text("Gender (Male/Female)*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(patientId, { patientId = it }, label = { Text("Patient ID (Optional)") }, modifier = Modifier.fillMaxWidth())
        
        Spacer(Modifier.height(8.dp))
        Text("Leave Details", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(diagnosisOrReason, { if(it.length <= 120) diagnosisOrReason = it }, label = { Text("Diagnosis / Reason*") }, modifier = Modifier.fillMaxWidth())
        Text("Max 120 chars (${diagnosisOrReason.length}/120)", style = MaterialTheme.typography.bodySmall)

        OutlinedTextField(startDateStr, { startDateStr = it }, label = { Text("Start Date (yyyy-MM-dd)*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(durationDaysStr, { durationDaysStr = it }, label = { Text("Duration (Days 1-30)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        
        if (endDate != null) {
            Text("Computed End Date: ${endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}", color = MaterialTheme.colorScheme.primary)
        } else {
            Text("Invalid date or duration", color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(notes, { if(it.length <= 120) notes = it }, label = { Text("Notes (Optional)") }, modifier = Modifier.fillMaxWidth())
        
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
                
                val payload = DocumentPayload.LeaveCertificatePayload(
                    patient = patient,
                    issueDateTime = now,
                    diagnosisOrReason = diagnosisOrReason,
                    startDate = startDate!!,
                    endDate = endDate!!,
                    durationDays = durationDays!!,
                    notes = notes
                )
                
                val rendered = LeaveCertificateRenderer.render(context, payload, branding, timing)
                onGenerated(rendered, DocumentType.MEDICAL_LEAVE_CERT)
            }, enabled = isValid) { Text("Generate PDF") }
            
            Button(onClick = {
                name = ""; age = ""; gender = ""; patientId = ""; diagnosisOrReason = ""; durationDaysStr = "3"; startDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE); notes = ""
            }) { Text("Reset") }
        }
        TextButton(onClick = onBack) { Text("Back") }
    }
}

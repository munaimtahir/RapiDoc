package com.alshifa.rapidocusg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alshifa.rapidocusg.core.documentengine.AppSettings
import com.alshifa.rapidocusg.core.documentengine.SettingsStore
import com.alshifa.rapidocusg.core.parser.ChatParser
import com.alshifa.rapidocusg.core.parser.SystemKeywords
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParserDictionaryScreen(
    settings: AppSettings,
    settingsStore: SettingsStore,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val gson = remember { Gson() }
    
    var wordInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedSystemWord by remember { mutableStateOf(SystemKeywords.all.first()) }
    var searchQuery by remember { mutableStateOf("") }

    val mappingsType = object : TypeToken<Map<String, String>>() {}.type
    val mappings: Map<String, String> = remember(settings.parserSynonymsJson) {
        if (settings.parserSynonymsJson.isBlank() || settings.parserSynonymsJson == "{}") {
            ChatParser.defaultMappings.toMap()
        } else {
            gson.fromJson(settings.parserSynonymsJson, mappingsType)
        }
    }

    val saveMappings = { newMappings: Map<String, String> ->
        scope.launch {
            settingsStore.updateParserSynonyms(gson.toJson(newMappings))
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Parser Dictionary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { saveMappings(ChatParser.defaultMappings.toMap()) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Reset to Defaults")
            }
        }
        
        HorizontalDivider()
        Text("Add New Mapping", fontWeight = FontWeight.SemiBold)
        
        OutlinedTextField(
            value = wordInput,
            onValueChange = { wordInput = it },
            label = { Text("Word/Phrase") },
            modifier = Modifier.fillMaxWidth()
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedSystemWord,
                onValueChange = {},
                readOnly = true,
                label = { Text("Maps To") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SystemKeywords.all.forEach { kw ->
                    DropdownMenuItem(
                        text = { Text(kw) },
                        onClick = {
                            selectedSystemWord = kw
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Button(
            onClick = {
                if (wordInput.isNotBlank()) {
                    val newMap = mappings.toMutableMap()
                    newMap[wordInput.lowercase()] = selectedSystemWord
                    saveMappings(newMap)
                    wordInput = ""
                }
            },
            enabled = wordInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Mapping")
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Mappings...") },
            modifier = Modifier.fillMaxWidth()
        )
        
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            val filteredList = mappings.entries.filter { 
                it.key.contains(searchQuery, ignoreCase = true) || it.value.contains(searchQuery, ignoreCase = true) 
            }.toList().sortedBy { it.key }
            
            items(filteredList) { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(entry.key, fontWeight = FontWeight.Bold)
                        Text("→ ${entry.value}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = {
                        val newMap = mappings.toMutableMap()
                        newMap.remove(entry.key)
                        saveMappings(newMap)
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
        TextButton(onClick = onBack) { Text("Back") }
    }
}

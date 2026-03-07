package com.alshifa.rapidocusg.core.documentengine

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class AppearanceMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val headerText: String = "AlShifa PolyClinic",
    val logoPath: String? = null,
    val parserSynonymsJson: String = "{}",
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM
)

class SettingsStore(private val context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("app_settings.preferences_pb") }
    )

    val settings: Flow<AppSettings> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            AppSettings(
                headerText = prefs[KEY_HEADER] ?: "AlShifa PolyClinic",
                logoPath = prefs[KEY_LOGO],
                parserSynonymsJson = prefs[KEY_PARSER_SYNONYMS] ?: "{}",
                appearanceMode = prefs[KEY_APPEARANCE_MODE]?.let { modeStr ->
                    runCatching { AppearanceMode.valueOf(modeStr) }.getOrDefault(AppearanceMode.SYSTEM)
                } ?: AppearanceMode.SYSTEM
            )
        }

    suspend fun updateHeader(text: String) = dataStore.edit { it[KEY_HEADER] = text }
    suspend fun updateLogo(path: String?) = dataStore.edit { if (path == null) it.remove(KEY_LOGO) else it[KEY_LOGO] = path }
    suspend fun updateParserSynonyms(json: String) = dataStore.edit { it[KEY_PARSER_SYNONYMS] = json }
    suspend fun updateAppearanceMode(mode: AppearanceMode) = dataStore.edit { it[KEY_APPEARANCE_MODE] = mode.name }
    suspend fun resetFactory() = dataStore.edit { it.clear() }

    companion object {
        private val KEY_HEADER = stringPreferencesKey("header_text")
        private val KEY_LOGO = stringPreferencesKey("logo_path")
        private val KEY_PARSER_SYNONYMS = stringPreferencesKey("parser_synonyms")
        private val KEY_APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
    }
}

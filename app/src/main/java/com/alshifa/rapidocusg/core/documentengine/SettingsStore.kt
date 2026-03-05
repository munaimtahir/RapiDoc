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

data class AppSettings(
    val headerText: String = "AlShifa PolyClinic",
    val logoPath: String? = null,
    val planTier: PlanTier = PlanTier.FREE,
    val includePhoneUsg: Boolean = false,
    val includePhoneMedical: Boolean = false,
    val includePhonePrescription: Boolean = false,
    val includePhoneLab: Boolean = false,
    val includePhoneRadiology: Boolean = false
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
                planTier = runCatching { PlanTier.valueOf(prefs[KEY_PLAN] ?: PlanTier.FREE.name) }.getOrDefault(PlanTier.FREE),
                includePhoneUsg = prefs[KEY_PHONE_USG] ?: false,
                includePhoneMedical = prefs[KEY_PHONE_MED] ?: false,
                includePhonePrescription = prefs[KEY_PHONE_RX] ?: false,
                includePhoneLab = prefs[KEY_PHONE_LAB] ?: false,
                includePhoneRadiology = prefs[KEY_PHONE_RAD] ?: false
            )
        }

    suspend fun updateHeader(text: String) = dataStore.edit { it[KEY_HEADER] = text }
    suspend fun updateLogo(path: String?) = dataStore.edit { if (path == null) it.remove(KEY_LOGO) else it[KEY_LOGO] = path }
    suspend fun updatePlan(planTier: PlanTier) = dataStore.edit { it[KEY_PLAN] = planTier.name }
    suspend fun resetFactory() = dataStore.edit { it.clear() }

    companion object {
        private val KEY_HEADER = stringPreferencesKey("header_text")
        private val KEY_LOGO = stringPreferencesKey("logo_path")
        private val KEY_PLAN = stringPreferencesKey("plan_tier")
        private val KEY_PHONE_USG = booleanPreferencesKey("include_phone_usg")
        private val KEY_PHONE_MED = booleanPreferencesKey("include_phone_med")
        private val KEY_PHONE_RX = booleanPreferencesKey("include_phone_rx")
        private val KEY_PHONE_LAB = booleanPreferencesKey("include_phone_lab")
        private val KEY_PHONE_RAD = booleanPreferencesKey("include_phone_rad")
    }
}

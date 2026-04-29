package io.switstack.switcloud.swittestl3.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.switstack.switcloud.swittestl3.common.Conf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "app_settings"

private val Context.dataStore by preferencesDataStore(
    name = DATASTORE_NAME
)

/**
 * Central manager for app settings using DataStore
 */
class DataStoreManager(private val context: Context) {

    // Keys
    private object PreferencesKeys {
        val SERVER_ADDRESS = stringPreferencesKey("server_address")
        val DEVICE_TYPE = stringPreferencesKey("device_type")
        val POI_ID = stringPreferencesKey("poi_id")
        val MAX_ATTEMPTS = intPreferencesKey("max_attempts")
        val TIMEOUT = intPreferencesKey("timeout")
        val DELAY_RETRIES = longPreferencesKey("delay_retries")
    }

    // Read values
    val serverAddressFlow: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SERVER_ADDRESS] ?: Conf.SERVER_ADDRESS
        }

    val deviceTypeFlow: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DEVICE_TYPE] ?: Conf.DEVICE_TYPE
        }

    val poiIdFlow: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.POI_ID] ?: Conf.POI_ID.toString()
        }

    val maxAttemptsFlow: Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.MAX_ATTEMPTS] ?: Conf.MAX_ATTEMPTS
        }

    val timeoutFlow: Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TIMEOUT] ?: Conf.TIMEOUT
        }

    val delayRetriesFlow: Flow<Long> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DELAY_RETRIES] ?: Conf.DELAY_RETRIES
        }

    // Save values
    suspend fun setServerAddress(address: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_ADDRESS] = address
        }
    }

    suspend fun setDeviceType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_TYPE] = type
        }
    }

    suspend fun setPoiId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POI_ID] = id
        }
    }

    suspend fun setMaxAttempts(attempts: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_ATTEMPTS] = attempts
        }
    }

    suspend fun setTimeout(timeoutSeconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TIMEOUT] = timeoutSeconds
        }
    }

    suspend fun setDelayRetries(delayMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DELAY_RETRIES] = delayMillis
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
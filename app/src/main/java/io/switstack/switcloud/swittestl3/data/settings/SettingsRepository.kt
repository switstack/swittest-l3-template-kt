package io.switstack.switcloud.swittestl3.data.settings

import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val dataStoreManager: DataStoreManager
) {

    // --- READ FLOWS ---
    val serverAddressFlow: Flow<String> = dataStoreManager.serverAddressFlow
    val deviceTypeFlow: Flow<String> = dataStoreManager.deviceTypeFlow
    val poiIdFlow: Flow<String> = dataStoreManager.poiIdFlow
    val maxAttemptsFlow: Flow<Int> = dataStoreManager.maxAttemptsFlow
    val timeoutFlow: Flow<Int> = dataStoreManager.timeoutFlow
    val delayRetriesFlow: Flow<Long> = dataStoreManager.delayRetriesFlow

    // --- SET FUNCTIONS ---

    suspend fun setServerAddress(address: String) {
        dataStoreManager.setServerAddress(address)
    }

    suspend fun setDeviceType(type: String) {
        dataStoreManager.setDeviceType(type)
    }

    suspend fun setPoiId(id: String) {
        dataStoreManager.setPoiId(id)
    }

    suspend fun setMaxAttempts(attempts: Int) {
        dataStoreManager.setMaxAttempts(attempts)
    }

    suspend fun setTimeout(timeoutSeconds: Int) {
        dataStoreManager.setTimeout(timeoutSeconds)
    }

    suspend fun setDelayRetries(delayMillis: Long) {
        dataStoreManager.setDelayRetries(delayMillis)
    }

    suspend fun clearAllSettings() {
        dataStoreManager.clearAllPreferences()
    }
}
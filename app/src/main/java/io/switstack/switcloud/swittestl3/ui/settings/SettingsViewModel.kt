package io.switstack.switcloud.swittestl3.ui.settings

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.switstack.switcloud.swittestl3.data.settings.CombinedSettings
import io.switstack.switcloud.swittestl3.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.UUID

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val serverAddress = repository.serverAddressFlow.stateIn(
        viewModelScope,
        WhileSubscribed(5_000),
        null
    )

    val deviceType = repository.deviceTypeFlow.stateIn(
        viewModelScope,
        WhileSubscribed(5_000),
        null
    )

    val poiId = repository.poiIdFlow.stateIn(
        viewModelScope,
        WhileSubscribed(5_000),
        null
    )

    val maxAttempts = repository.maxAttemptsFlow.stateIn(
        viewModelScope,
        WhileSubscribed(5_000),
        null
    )

    val timeout = repository.timeoutFlow.stateIn(
        viewModelScope,
        WhileSubscribed(5_000),
        null
    )

    val delayRetries = repository.delayRetriesFlow.stateIn(
        viewModelScope,
        WhileSubscribed(5_000),
        null
    )

    val errorList = MutableStateFlow<Map<Int, String>>(mapOf())
    val allCombinedSettings: StateFlow<CombinedSettings?> =
        combineTransform(
            serverAddress.filterNotNull(),
            timeout.filterNotNull(),
            maxAttempts.filterNotNull(),
            delayRetries.filterNotNull(),
            poiId.filterNotNull(),
            deviceType.filterNotNull()
        ) { latestValues: Array<Any?> ->
            val address = latestValues[0] as String
            val timeoutVal = latestValues[1] as Int
            val maxAttemptsVal = latestValues[2] as Int
            val delayRetriesVal = latestValues[3] as Long
            val poiIdVal = latestValues[4] as String
            val deviceTypeVal = latestValues[5] as String

            val poiUuid = try {
                UUID.fromString(poiIdVal)
            } catch (e: Exception) {
                UUID(0L, 0L)
            }
            emit(
                CombinedSettings(
                    serverAddress = address,
                    timeout = timeoutVal,
                    maxAttempts = maxAttemptsVal,
                    delayRetries = delayRetriesVal,
                    poiId = poiUuid,
                    deviceType = deviceTypeVal
                )
            )
        }.stateIn(
            viewModelScope,
            WhileSubscribed(5_000),
            null
        )

    fun onSaveSettings(
        serverAddress: Pair<Int, String?>,
        deviceType: Pair<Int, String?>,
        poiId: Pair<Int, String?>,
        timeout: Pair<Int, Int?>,
    ): Map<Int, String> {
        val valuesErrorList = mutableMapOf<Int, String>()

        serverAddress.second?.let {
            if (Patterns.WEB_URL.matcher(it).matches()) {
                it.toHttpUrl()
            } else {
                valuesErrorList.put(serverAddress.first, it)
            }
        }

        poiId.second?.let {
            try {
                check(it.length == 36)
                UUID.fromString(it)
            } catch (e: Exception) {
                valuesErrorList.put(poiId.first, it)
            }
        }

        errorList.update { valuesErrorList }

        if (valuesErrorList.isEmpty()) {
            serverAddress.second?.let {
                onServerAddressChanged(it)
            }
            deviceType.second?.let {
                onDeviceTypeChanged(it)
            }
            poiId.second?.let {
                onPoiIdChanged(it)
            }
            timeout.second?.let {
                onTimeoutChanged(it)
            }
        }
        return valuesErrorList
    }

    private fun onServerAddressChanged(address: String) {
        viewModelScope.launch {
            repository.setServerAddress(address)
        }
    }

    private fun onDeviceTypeChanged(type: String) {
        viewModelScope.launch {
            repository.setDeviceType(type)
        }
    }

    private fun onPoiIdChanged(id: String) {
        viewModelScope.launch {
            repository.setPoiId(id)
        }
    }

    private fun onTimeoutChanged(timeoutSeconds: Int) {
        viewModelScope.launch {
            repository.setTimeout(timeoutSeconds)
        }
    }
}
package io.switstack.switcloud.swittestl3.data

import io.switstack.switcloud.swittestl3.common.SerializationUtils.decodeMessage
import io.switstack.switcloud.swittestl3.common.SerializationUtils.serializeToJsonString
import io.switstack.switcloud.swittestl3.common.SwittestL3Exception
import io.switstack.switcloud.swittestl3.data.requests.Request
import io.switstack.switcloud.swittestl3.data.settings.CombinedSettings
import io.switstack.switcloud.swittestl3.domain.ResponseResolver
import io.switstack.switcloud.swittestl3.domain.ResponseResolver.createResponsePayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.net.URI

object MessageProcessor {
    suspend fun processMessages(
        messageFlow: Flow<String>,
        settings: CombinedSettings,
        sendMessage: (String) -> Unit,
        isSocketClosing: () -> Boolean,
        errorCallback: () -> Unit
    ) {
        var responseType: MessageTypeEnum? = null
        messageFlow
            .catch { e ->
                if (!isSocketClosing()) {
                    Timber.e(e, "Transport exception: ${e.message}")
                    errorCallback()
                }
            }
            .map { receivedString ->
                Timber.i("Received request '$receivedString'.")
                if (receivedString.isNotBlank()) {
                    decodeMessage<Request>(receivedString)
                } else {
                    throw SwittestL3Exception("Empty payload")
                }
            }.catch { e ->
                Timber.e(e, "Failed to decode request: ${e.message}")
                errorCallback()
            }.map { request ->
                responseType = ResponseResolver.getMessageResponseType(request.header)
                val urlServer = URI(settings.serverAddress).toURL()
                createResponsePayload(request, urlServer.host, settings.poiId, settings.deviceType)
            }.catch { e ->
                Timber.e(e, "Failed to create response payload: ${e.message}")
                errorCallback()
            }.map { response ->
                serializeToJsonString(response).also {
                    Timber.i("Sent response '$it'")
                }
            }.catch { e ->
                Timber.e(e, "Failed to encode response: ${e.message}")
                errorCallback()
            }.onEach { responseString ->
                sendMessage(responseString)
                if (responseType == MessageTypeEnum.RESPONSE_GET_POI_ID) {
                    ResponseResolver.onGetPoiIdSent()
                }
            }.catch { e ->
                Timber.e(e, "Failed to send response: ${e.message}")
                errorCallback()
            }.collect()
        Timber.d("MessageReading finished")
    }
}
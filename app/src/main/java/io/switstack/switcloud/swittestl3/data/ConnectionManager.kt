package io.switstack.switcloud.swittestl3.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.switstack.switcloud.swittestl3.BuildConfig
import io.switstack.switcloud.swittestl3.data.settings.CombinedSettings
import io.switstack.switcloud.swittestl3.domain.ResponseResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ConnectionManager() : ViewModel(), KoinComponent {

    private val okHttpClient: OkHttpClient by inject()
    private var webSocket: WebSocket? = null
    private var connectionClosingRequest: Boolean = false

    val isConnected = MutableStateFlow(false)
    val isConnecting = MutableStateFlow(false)

    val message: MutableStateFlow<Pair<String, DisplayedMessageTypeEnum>?> = MutableStateFlow(null)

    fun connect(settings: CombinedSettings) {
        val url = settings.serverAddress.toHttpUrlOrNull()
        if (url == null) {
            Timber.w("Error while creating connection URL : ${settings.serverAddress}")
            message.update { "Error while creating connection URL : ${settings.serverAddress}" to DisplayedMessageTypeEnum.Warning }
            return
        }
        if (webSocket == null) {
            val request = Request.Builder()
                .url(url)
                .addHeader("Upgrade", "websocket")
                .addHeader("Connection", "Upgrade")
                .addHeader("x-switstack-client-attestation", BuildConfig.SWITSTACK_CLIENT_ATTESTATION_SECRET)
                .build()

            var errorsCounter = 0

            val messageFlow = MutableSharedFlow<String>(10)

            viewModelScope.launch((Dispatchers.IO)) {
                MessageProcessor.processMessages(
                    messageFlow,
                    settings,
                    ::sendMessage,
                    ::isConnectionClosing,
                    {
                        errorsCounter++
                    }
                )
            }

            val listener = SwittestWebSocketListener(
                onOpenCallback = { _, _ ->
                    message.update { "Connected to ${settings.serverAddress}." to DisplayedMessageTypeEnum.Info }
                    isConnected.update { true }
                    isConnecting.update { false }
                },
                onMessageCallback = { _, message -> messageFlow.tryEmit(message) },
                onFailureCallback = { _, t, _ ->
                    message.update { (t.message ?: "error") to DisplayedMessageTypeEnum.Error }
                    errorsCounter = 0
                    onConnectionClosed()
                    disconnect()
                },
                onClosingCallback = { _, _, _ ->
                    errorsCounter = 0
                    onConnectionClosed()
                }
            )

            isConnecting.update { true }
            Timber.i("Connecting to ${settings.serverAddress} ...")
            message.update { "Connecting to ${settings.serverAddress} ..." to DisplayedMessageTypeEnum.Info }
            webSocket = okHttpClient.newWebSocket(request, listener)
        } else {
            Timber.w("Trying to connect even if connection is already in use")
            message.update { "Trying to connect even if connection is already in use" to DisplayedMessageTypeEnum.Warning }
        }
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    private fun disconnect() {
        webSocket?.cancel()
    }

    private fun onConnectionClosed() {
        webSocket = null
        isConnected.update { false }
        isConnecting.update { false }
        ResponseResolver.onDisconnected()
        Timber.i("Connection closed.")
        if (message.value?.second != DisplayedMessageTypeEnum.Error) {
            message.update { "Connection closed" to DisplayedMessageTypeEnum.Info }
        }
    }

    fun requestSocketClose() {
        connectionClosingRequest = true
        disconnect()
    }

    fun isConnectionClosing() = connectionClosingRequest

    override fun onCleared() {
        super.onCleared()
        // Gracefully close connection when ViewModel is destroyed
        webSocket?.close(1000, "ViewModel Cleared")
    }
}

class SwittestWebSocketListener(
    private val onOpenCallback: (webSocket: WebSocket, response: Response) -> Unit = { _, _ -> },
    private val onMessageCallback: (webSocket: WebSocket, text: String) -> Unit = { _, _ -> },
    private val onClosingCallback: (webSocket: WebSocket, code: Int, reason: String) -> Unit = { _, _, _ -> },
    private val onFailureCallback: (webSocket: WebSocket, t: Throwable, response: Response?) -> Unit = { _, _, _ -> }
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Timber.d("WS Connected!")
        onOpenCallback(webSocket, response)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Timber.d("WS Receiving: $text")
        onMessageCallback(webSocket, text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Timber.d("WS Receiving bytes: ${bytes.hex()}")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Timber.d("WS closing")
        onClosingCallback(webSocket, code, reason)
        webSocket.close(code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Timber.d("WS Error: ${t.message}")
        onFailureCallback(webSocket, t, response)
    }
}

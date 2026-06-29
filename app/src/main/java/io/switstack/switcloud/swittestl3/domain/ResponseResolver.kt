package io.switstack.switcloud.swittestl3.domain

import io.switstack.switcloud.switcloudclt.common.SwitcloudClientException
import io.switstack.switcloud.switcloudclt.data.InitiationData
import io.switstack.switcloud.switcloudclt.domain.SwitcloudTestClient
import io.switstack.switcloud.swittestl3.BuildConfig
import io.switstack.switcloud.swittestl3.common.SerializationUtils.json
import io.switstack.switcloud.swittestl3.common.SerializationUtils.toJsonObject
import io.switstack.switcloud.swittestl3.common.SwittestL3Exception
import io.switstack.switcloud.swittestl3.data.Header
import io.switstack.switcloud.swittestl3.data.MessageTypeEnum
import io.switstack.switcloud.swittestl3.data.ResetEvent
import io.switstack.switcloud.swittestl3.data.StartPaymentEvent
import io.switstack.switcloud.swittestl3.data.StatusEnum
import io.switstack.switcloud.swittestl3.data.requests.PaymentRequest
import io.switstack.switcloud.swittestl3.data.requests.Request
import io.switstack.switcloud.swittestl3.data.requests.SetCredentialsRequest
import io.switstack.switcloud.swittestl3.data.responses.PoiInfoResponse
import io.switstack.switcloud.swittestl3.data.responses.Response
import io.switstack.switcloud.swittestl3.data.responses.ResponsePayload
import io.switstack.switcloud.swittestl3.data.responses.Status
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.util.UUID

object ResponseResolver : KoinComponent {

    private val localhostUrlPattern = Regex("^(http|https)://localhost:80\\d{2}$")
    private var client: SwitcloudTestClient? = null

    private val _initiateResponse: MutableSharedFlow<InitiationData> = MutableSharedFlow(1)
    val initiateResponse: SharedFlow<InitiationData> = _initiateResponse.asSharedFlow()

    private val _readyStatus: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val readyStatus: StateFlow<Boolean> = _readyStatus.asStateFlow()

    private val _resetStatus: MutableSharedFlow<ResetEvent> = MutableSharedFlow(1)
    val resetStatus: SharedFlow<ResetEvent> = _resetStatus.asSharedFlow()

    private val _startPayment: MutableSharedFlow<StartPaymentEvent> = MutableSharedFlow(1)
    val startPayment: SharedFlow<StartPaymentEvent> = _startPayment.asSharedFlow()

    fun createResponsePayload(request: Request, serverHost: String, poiId: UUID, deviceType: String): Response {
        var status = Status(code = StatusEnum.ErrInternal.code)
        var data: PoiInfoResponse? = null

        when (request.header.mid) {
            MessageTypeEnum.REQUEST_GET_POI_ID.code -> {
                data = PoiInfoResponse(
                    poiId = poiId,
                    deviceType = deviceType
                ).also {
                    status = Status(StatusEnum.Ok.code)
                }
            }

            MessageTypeEnum.REQUEST_SET_CREDENTIALS.code -> {
                request.payload?.let { payload ->
                    try {
                        val setCredentialsRequest = json.decodeFromString<SetCredentialsRequest>(payload.toString())
                        client = setCredentialsCall(setCredentialsRequest, serverHost)
                        status = Status(StatusEnum.Ok.code, "")
                    } catch (e: Exception) {
                        status = Status(StatusEnum.ErrRuntime.code, e.message ?: "")
                    }
                }
            }

            MessageTypeEnum.REQUEST_START_PAYMENT.code -> {
                request.payload?.let { payload ->
                    val paymentRequest = json.decodeFromString<PaymentRequest>(payload.toString())
                    client?.let { client ->
                        _startPayment.tryEmit(StartPaymentEvent())
                        startPayment(client, paymentRequest) {
                            status = it
                        }
                    }
                } ?: run {
                    onPaymentIncomplete()
                    throw SwittestL3Exception("payload null or client null")
                }
            }

            else -> {
                throw SwittestL3Exception("unexpected message ID")
            }
        }

        return Response(
            Header(
                xid = request.header.xid,
                mid = getMessageResponseType(request.header).code
            ),
            ResponsePayload(
                status = status,
                others = data?.toJsonObject()
            )
        )
    }

    private fun setCredentialsCall(setCredentialsRequest: SetCredentialsRequest, serverAddress: String): SwitcloudTestClient {
        try {
            val serverUrl = if (localhostUrlPattern.matches(setCredentialsRequest.switcloudUrl)) {
                setCredentialsRequest.switcloudUrl.replace("localhost", serverAddress)
            } else {
                setCredentialsRequest.switcloudUrl
            }
            return get<SwitcloudTestClient> { parametersOf(serverUrl) }.apply {
                authenticateMachine(setCredentialsRequest.clientId, setCredentialsRequest.clientSecret, BuildConfig.SWITSTACK_CLIENT_ATTESTATION_SECRET)
            }
        } catch (e: SwitcloudClientException) {
            Timber.e(e, "Failed to authenticate: ${e.cause}")
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to process configuration request: ${e.message}")
            throw e
        }
    }

    private fun startPayment(client: SwitcloudTestClient, request: PaymentRequest, onFinished: (Status) -> Unit) {
        try {
            client.run {
                initialize()
                configure(request.paymentId, null)
                loadVCard(request, client)
                initiate(request.paymentId).also {
                    _initiateResponse.tryEmit(it)
                }
                complete()
            }
            onFinished(Status(StatusEnum.Ok.code))
        } catch (e: Exception) {
            onPaymentIncomplete()
            Timber.w(e, "Failed to process payment request: ${e.cause}")
            onFinished(Status(StatusEnum.ErrRuntime.code, e.message ?: ""))
        } finally {
            client.cleanup()
        }
    }

    private fun loadVCard(request: PaymentRequest, client: SwitcloudTestClient) {
        if (request.vcardData?.isNotEmpty() == true && BuildConfig.FLAVOR == "mokavepl") {
            try {
                client.loadVirtualCard(request.vcardData)
            } catch (e: Exception) {
                Timber.w(e, "Unable to load VirtualCard")
            }
        }
    }
    fun getMessageResponseType(header: Header): MessageTypeEnum =
        when (header.mid) {
            MessageTypeEnum.REQUEST_GET_POI_ID.code -> MessageTypeEnum.RESPONSE_GET_POI_ID
            MessageTypeEnum.REQUEST_SET_CREDENTIALS.code -> MessageTypeEnum.RESPONSE_SET_CREDENTIALS
            MessageTypeEnum.REQUEST_START_PAYMENT.code -> MessageTypeEnum.RESPONSE_START_PAYMENT
            else -> {
                throw SwittestL3Exception("unexpected message ID ${header.mid}")
            }
        }

    private fun onPaymentIncomplete() {
        _resetStatus.tryEmit(ResetEvent())
    }

    fun onGetPoiIdSent() {
        _readyStatus.update { true }
    }

    fun onDisconnected() {
        client?.cleanup()
        _readyStatus.update { false }
    }
}
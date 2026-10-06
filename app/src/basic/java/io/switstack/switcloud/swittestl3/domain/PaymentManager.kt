package io.switstack.switcloud.swittestl3.domain

import io.switstack.switcloud.switcloudclt.data.SessionData
import io.switstack.switcloud.switcloudclt.domain.InitiateResponse
import io.switstack.switcloud.switcloudclt.domain.SwitcloudTestClient
import io.switstack.switcloud.swittestl3.common.Conf.readerParams
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.util.UUID

class PaymentManager(
    val client: SwitcloudTestClient,
    val pinInput: Channel<String?>
) {
    private lateinit var sessionData: SessionData

    fun initialize() = client.initialize(readerParams)

    fun configure(paymentId: UUID, fallbackTrd: String?) {
        sessionData = client.configure(paymentId, fallbackTrd)
    }

    fun loadVCard(vcardData: String) {
        if (vcardData.isNotEmpty() && client.virtualCardsSupported()) {
            try {
                client.loadVirtualCard(vcardData)
            } catch (e: Exception) {
                Timber.w(e, "Unable to load VirtualCard")
            }
        }
    }

    fun initiate(): InitiateResponse = client.initiate(sessionData)

    fun complete(response: InitiateResponse, authorizationResponse: String?) =
        client.complete(
            sessionData,
            authorizationResponse ?: "",
            response
        )

    fun emitReceipt(finalResponse: InitiateResponse) = client.emitReceipt(finalResponse)
}
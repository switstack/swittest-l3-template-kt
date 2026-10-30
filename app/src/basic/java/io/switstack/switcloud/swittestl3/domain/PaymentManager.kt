package io.switstack.switcloud.swittestl3.domain

import io.switstack.switcloud.switcloudclt.data.OutcomeParameterSet.CVM
import io.switstack.switcloud.switcloudclt.data.OutcomeParameterSet.Status
import io.switstack.switcloud.switcloudclt.data.SessionData
import io.switstack.switcloud.switcloudclt.domain.InitiateResponse
import io.switstack.switcloud.switcloudclt.domain.SwitcloudTestClient
import io.switstack.switcloud.swittestl3.common.Conf.readerParams
import kotlinx.coroutines.channels.Channel
import java.util.UUID

class PaymentManager(
    override val client: SwitcloudTestClient,
    override val pinInput: Channel<String?>
): IPaymentManager {


    private lateinit var sessionData: SessionData
    override var isCvmObtainSignature: Boolean = false
    override var isReceiptRequired: Boolean = false

    override fun initialize() = client.initialize(readerParams, pinInput)

    override fun configure(paymentId: UUID, fallbackTrd: String?) {
        sessionData = client.configure(paymentId, fallbackTrd)
    }

    override fun initiate(): InitiateResponse = client.initiate(sessionData)

    override fun complete(initiateResponse: InitiateResponse, authorizationResponse: String): InitiateResponse {
        initiateResponse.outcomeParameterSet?.takeIf { it.status == Status.ONLINE_REQUEST }?.let { ops ->
            if (ops.cvm == CVM.OBTAIN_SIGNATURE || ops.receipt) {
                isCvmObtainSignature = true
                isReceiptRequired = true
            }
        }

        return client.complete(
            sessionData,
            authorizationResponse,
            initiateResponse
        )
    }
}
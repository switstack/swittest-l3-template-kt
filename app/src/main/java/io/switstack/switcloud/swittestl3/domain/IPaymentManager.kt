package io.switstack.switcloud.swittestl3.domain

import io.switstack.switcloud.switcloudclt.common.Utils.tlvFromByteArray
import io.switstack.switcloud.switcloudclt.data.OutcomeParameterSet.CVM
import io.switstack.switcloud.switcloudclt.data.OutcomeParameterSet.Status
import io.switstack.switcloud.switcloudclt.domain.InitiateResponse
import io.switstack.switcloud.switcloudclt.domain.SwitcloudTestClient
import io.switstack.switcloud.swittestl3.common.GlaUtils
import io.switstack.switcloud.swittestl3.data.Receipt
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber
import java.util.UUID

interface IPaymentManager {
    val pinInput: Channel<String?>
    val client: SwitcloudTestClient

    var isCvmObtainSignature: Boolean
    var isReceiptRequired: Boolean
    fun initialize()

    fun configure(paymentId: UUID, fallbackTrd: String?)

    fun loadVCard(vcardData: String) {
        if (vcardData.isNotEmpty() && client.virtualCardsSupported()) {
            try {
                client.loadVirtualCard(vcardData)
            } catch (e: Exception) {
                Timber.w(e, "Unable to load VirtualCard")
            }
        }
    }

    fun initiate(): InitiateResponse

    fun complete(initiateResponse: InitiateResponse, authorizationResponse: String): InitiateResponse

    fun emitReceipt(finalResponse: InitiateResponse, transactionReceipt: MutableSharedFlow<Receipt>) {
        Timber.d("---------------- EMIT RECEIPT BEGIN -------------------")
        finalResponse.outcomeParameterSet?.let { ops ->
            if (isCvmObtainSignature) {
                ops.cvm = CVM.OBTAIN_SIGNATURE
            }
            if (isReceiptRequired) {
                ops.receipt = isReceiptRequired
            }

            when (ops.status) {
                Status.APPROVED -> "transaction approved"
                Status.DECLINED -> "transaction declined"
                Status.END_APPLICATION -> "transaction terminated"
                else -> "transaction terminated (status unknown)"
            }.let {
                Timber.d("verdict > $it")
            }

            if (ops.receipt) {
                val amount = GlaUtils.getTagGlaSafe(client.gla, byteArrayOf(0x9F.toByte(), 0x02.toByte()), "Amount")?.let {
                    tlvFromByteArray(it)?.list?.single()?.hexValue
                } ?: "NA"

                val ac = GlaUtils.getTagGlaSafe(client.gla, byteArrayOf(0x9F.toByte(), 0x26.toByte()), "AC")?.let {
                    tlvFromByteArray(it)?.list?.single()?.hexValue
                } ?: "NA"

                transactionReceipt.tryEmit(
                    Receipt(
                        amount,
                        ac,
                        ops.cvm == CVM.OBTAIN_SIGNATURE
                    )
                )
            }
        }
        Timber.d("---------------- EMIT RECEIPT END -------------------")
    }
}
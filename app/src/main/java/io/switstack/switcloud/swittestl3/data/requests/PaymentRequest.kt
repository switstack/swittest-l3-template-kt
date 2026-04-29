package io.switstack.switcloud.swittestl3.data.requests

import io.switstack.switcloud.swittestl3.common.UuidSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PaymentRequest(
    @SerialName("payment_id")
    @Serializable(with = UuidSerializer::class)
    val paymentId: UUID,
    @SerialName("vcard_data")
    val vcardData: String? = null
)
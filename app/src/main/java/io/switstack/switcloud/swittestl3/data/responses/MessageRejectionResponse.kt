package io.switstack.switcloud.swittestl3.data.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageRejectionResponse(
    @SerialName("reason_code")
    val reasonCode: Int,
    @SerialName("reason_message")
    val reasonMessage: String
)

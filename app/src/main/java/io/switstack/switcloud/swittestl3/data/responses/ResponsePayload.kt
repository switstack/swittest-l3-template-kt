package io.switstack.switcloud.swittestl3.data.responses

import io.switstack.switcloud.swittestl3.common.ResponsePayloadSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable(with = ResponsePayloadSerializer::class)
data class ResponsePayload(
    val status: Status,
    val others: JsonObject?
)
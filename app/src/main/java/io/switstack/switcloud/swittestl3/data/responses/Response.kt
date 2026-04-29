package io.switstack.switcloud.swittestl3.data.responses

import io.switstack.switcloud.swittestl3.data.Header
import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val header: Header,
    val payload: ResponsePayload
)
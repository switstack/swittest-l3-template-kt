package io.switstack.switcloud.swittestl3.data.requests

import io.switstack.switcloud.swittestl3.data.Header
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Request(
    val header: Header,
    val payload: JsonObject? = null
)

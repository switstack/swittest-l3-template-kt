package io.switstack.switcloud.swittestl3.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class Status(
    val code: UInt,
    val message: String = ""
)
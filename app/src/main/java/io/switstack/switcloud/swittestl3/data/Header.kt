package io.switstack.switcloud.swittestl3.data

import kotlinx.serialization.Serializable

@Serializable
data class Header(
    val xid: Int,
    val mid: Int
)

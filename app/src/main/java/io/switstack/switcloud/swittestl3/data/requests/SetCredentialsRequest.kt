package io.switstack.switcloud.swittestl3.data.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetCredentialsRequest(
    @SerialName("switcloud_url")
    val switcloudUrl: String,
    @SerialName("client_id")
    val clientId: String,
    @SerialName("client_secret")
    val clientSecret: String,
)
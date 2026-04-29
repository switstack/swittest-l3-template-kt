package io.switstack.switcloud.swittestl3.data.responses

import io.switstack.switcloud.swittestl3.common.UuidSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PoiInfoResponse(
    @SerialName("device_type")
    val deviceType: String,
    @SerialName("poi_id")
    @Serializable(with = UuidSerializer::class)
    val poiId: UUID
)

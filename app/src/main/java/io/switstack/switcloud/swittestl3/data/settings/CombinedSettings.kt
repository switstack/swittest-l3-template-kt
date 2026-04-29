package io.switstack.switcloud.swittestl3.data.settings

import java.util.UUID

data class CombinedSettings(
    val serverAddress: String,
    val timeout: Int,
    val maxAttempts: Int,
    val delayRetries: Long,
    val poiId: UUID,
    val deviceType: String
)

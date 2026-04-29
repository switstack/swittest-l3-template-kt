package io.switstack.switcloud.swittestl3.common

import io.switstack.switcloud.swittestl3.BuildConfig
import io.switstack.switcloud.swittestl3.data.settings.CombinedSettings
import java.util.UUID

object Conf {
    const val SERVER_ADDRESS = BuildConfig.SWITTEST_URL
    const val DEVICE_TYPE = "poi"
    val POI_ID: UUID = UUID.fromString(BuildConfig.POI_ID)
    const val MAX_ATTEMPTS = 10
    const val TIMEOUT = 10
    const val DELAY_RETRIES = 1000L

    val DEFAULT_SETTINGS = CombinedSettings(
        SERVER_ADDRESS,
        TIMEOUT,
        MAX_ATTEMPTS,
        DELAY_RETRIES,
        POI_ID,
        DEVICE_TYPE
    )
}
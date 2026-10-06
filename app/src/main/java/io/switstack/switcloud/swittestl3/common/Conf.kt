package io.switstack.switcloud.swittestl3.common

import io.switstack.switcloud.swittestl3.BuildConfig
import java.util.UUID

object Conf {
    const val SERVER_ADDRESS = BuildConfig.SWITTEST_URL
    const val DEVICE_TYPE = "poi"
    val POI_ID: UUID = UUID.fromString(BuildConfig.POI_ID)
    const val MAX_ATTEMPTS = 10
    const val TIMEOUT = 10
    const val DELAY_RETRIES = 1000L

    // APDU logging
    val readerParams = byteArrayOf(
        // Supported interfaces
        0xDF.toByte(), 0xA0.toByte(), 0x06, 0x01, 0x04,
        // Trace (on)
        0xDF.toByte(), 0xA0.toByte(), 0x18, 0x01, 0x01,
        // Timeout interfaces detection (25 sec)
        0xDF.toByte(), 0xA0.toByte(), 0x08, 0x01, 0x19,
        // Polling timeout (30 sec)
        0xDF.toByte(), 0xA0.toByte(), 0x07, 0x01, 0x1e
    )
}
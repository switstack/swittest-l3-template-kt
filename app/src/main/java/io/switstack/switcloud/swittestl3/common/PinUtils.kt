package io.switstack.switcloud.swittestl3.common

import com.payneteasy.tlv.BerTag
import com.payneteasy.tlv.BerTlvBuilder
import timber.log.Timber
import java.security.SecureRandom

object PinUtils {

    private const val ISO_FORMAT_1 = 1
    private const val PIN_BLOCK_LENGTH = 8
    private const val MIN_PIN_LENGTH = 4
    private const val MAX_PIN_LENGTH = 12
    private const val ONLINE_PIN_BLOCK_LENGTH = 8
    private const val PAN_BLOCK_LENGTH = 8
    private const val MIN_PAN_LENGTH = 6
    private const val MAX_PAN_LENGTH = 10
    private const val XOR_BLOCK_LENGTH = 8

    private val TDES_KEY = byteArrayOf(
        0x10.toByte(), 0x11.toByte(), 0x12.toByte(), 0x13.toByte(), 0x14.toByte(), 0x15.toByte(), 0x16.toByte(), 0x17.toByte(),
        0x20.toByte(), 0x21.toByte(), 0x22.toByte(), 0x23.toByte(), 0x24.toByte(), 0x25.toByte(), 0x26.toByte(), 0x27.toByte()
    )

    fun readerPinpadGetOnlinePinBlock(pin: ByteArray, pan: ByteArray?): ByteArray {
        /** 1. Build pin block */
        val pinBlock = readerPinpadRoutineSetOnlinePinBlock(ISO_FORMAT_1, pin)

        /** 2. Compute online pin block */
        return pinpadComputeOnlinePinBlock(TDES_KEY, pinBlock, pan)
    }

    private fun pinpadComputeOnlinePinBlock(tdesKey: ByteArray, pinBlock: ByteArray, pan: ByteArray?): ByteArray {
        val xorBlock = ByteArray(XOR_BLOCK_LENGTH)
        val onlinePinBlock = ByteArray(ONLINE_PIN_BLOCK_LENGTH)

        /* Check parameters */
        check(pinBlock.size == PIN_BLOCK_LENGTH)

        Timber.d("[DH] PIN block > ${pinBlock.toHexString()}")
        val isoFormat = (pinBlock[0].toInt() and 0xF0) shr 4

        when (isoFormat) {
            ISO_FORMAT_1 -> {
                /* Set PAN block */
                if (pan == null) {
                    throw SwittestL3Exception("pan should not be null in tag 5A or in fallback tag 57")
                }
                Timber.d("[DH] PAN > ${pan.toHexString()}")
                val panBlock = pinpadRoutineSetPanBlock(pan)
                Timber.d("[DH] PAN block > ${panBlock.toHexString()}")

                /* XOR PIN block with PAN block */
                pinpadRoutineXor(pinBlock, panBlock, XOR_BLOCK_LENGTH).copyInto(xorBlock)
            }
            else -> pinBlock.copyInto(xorBlock)
        }

        /* Cipher XOR */
        Timber.d("[DH] XOR block > ${xorBlock.toHexString()}")
        Hal3DesEcb().apply {
            setKey(tdesKey)
            ecbEncrypt(xorBlock).copyInto(onlinePinBlock)
        }

        Timber.d("[DH] onlinePin block > ${onlinePinBlock.toHexString()}")
        return BerTlvBuilder()
            .addBytes(BerTag(0x99), onlinePinBlock)
            .buildArray()
    }

//region routines

    private fun readerPinpadRoutineSetOnlinePinBlock(isoFormat: Int, pin: ByteArray): ByteArray {
        check(pin.isNotEmpty())
        check(pin.size >= MIN_PIN_LENGTH)
        check(pin.size <= MAX_PIN_LENGTH)

        val random = SecureRandom().nextInt(16)

        /** Offline PIN format is (16 digits = 8 bytes)
         *  C N P P P P P/F P/F P/F P/F P/F P/F P/F P/F F F
         *
         * C: ISO format
         * N: PIN length
         * P: PIN digit
         * P/F: PIN / Filler
         * F: Filler
         *
         */

        val padding = ((random shl 4) or random) and 0xFF // keep unsigned Int

        return getPinBlock(
            padding,
            (isoFormat shl 4) and 0xFF,
            pin
        )
    }

    private fun getPinBlock(
        padding: Int,
        controller: Int,
        pin: ByteArray
    ): ByteArray {
        check(pin.isNotEmpty())
        check(pin.size >= MIN_PIN_LENGTH)
        check(pin.size <= MAX_PIN_LENGTH)

        var i = 0
        var j = 0
        val pinBlock = ByteArray(PIN_BLOCK_LENGTH)

        pinBlock.fill(padding.toByte(), fromIndex = 0, toIndex = PIN_BLOCK_LENGTH)

        pinBlock[j++] = ((controller or pin.size) and 0xFF).toByte()

        while (i < pin.size) {
            if (i % 2 == 0) {
                var b = pinBlock[j].toInt() and 0xFF
                b = b and 0x0F
                b = b or (((pin[i].toInt() and 0xFF) and 0x0F) shl 4)
                pinBlock[j] = b.toByte()
            } else {
                var b = pinBlock[j].toInt() and 0xFF
                b = b and 0xF0
                b = b or ((pin[i].toInt() and 0xFF) and 0x0F)
                pinBlock[j] = b.toByte()
                j++
            }
            i++
        }

        return pinBlock
    }

    private fun pinpadRoutineSetPanBlock(pan: ByteArray): ByteArray {
        var i = PAN_BLOCK_LENGTH - 1
        var panEndingIndex: Int
        var panPadded = false
        val panBlock = ByteArray(PAN_BLOCK_LENGTH)

        /* Check parameters */
        check(pan.isNotEmpty())
        check(pan.size >= MIN_PAN_LENGTH)
        check(pan.size <= MAX_PAN_LENGTH)

        /** PAN format is (between 6 and 10 byte long)
         *  PP PP PP PP PP PP ... PP PC     or
         *  PP PP PP PP PP PP ... PP CF
         *
         * PAN block format is (16 digits = 8 bytes)
         *  0 0 0 0 P P P P P P P P P P P P
         *
         * C: Check digit
         * F: Filler (value 0x0F)
         * P: PAN digit where:
         *  - P are from the 12 right-most digits of the PAN
         *  - C and F are ignored from the PAN
         */

        if ((pan.last().toInt() and 0x0F) != 0x0F) {
            panEndingIndex = pan.size - 1 // check digit ignored
        } else {
            panPadded = true
            panEndingIndex = pan.size - 2 // padding and check digit ignored
        }

        panBlock.fill(0x00)

        // Get PAN's 12 last digits excluding check digit, and padding (if present)
        // And copy them in PAN block
        do {
            if (!panPadded) {
                panBlock[i] = ((pan[panEndingIndex].toInt() and 0xF0) shr 4).toByte()
                panBlock[i--] = (panBlock[i + 1].toInt() or ((pan[panEndingIndex - 1].toInt() and 0x0F) shl 4)).toByte()
                panEndingIndex--
            } else {
                panBlock[i--] = pan[panEndingIndex--]
            }
        } while (i > 1) // First 4 nibbles set to 0

        return panBlock
    }

    fun pinpadRoutineXor(byteArray1: ByteArray, byteArray2: ByteArray, outByteArraySize: Int): ByteArray {
        val out = ByteArray(outByteArraySize)
        for (i in out.indices) {
            out[i] = (byteArray1[i].toInt() xor byteArray2[i].toInt()).toByte()
        }
        return out
    }

//endregion
}
package io.switstack.switcloud.swittestl3.common

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Hal3DesEcb {

    @Suppress("GetInstance") // ECB is required by ISO 9564 PIN block spec — single 8-byte block, not general-purpose encryption
    private var cipher: Cipher = Cipher.getInstance("DESede/ECB/NoPadding")

    fun setKey(key: ByteArray) {
        val secretKey = SecretKeySpec(key, "DESede")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    }

    fun ecbEncrypt(xorBlock: ByteArray): ByteArray =
        cipher.doFinal(xorBlock)
}
package com.fh.payday.utilities.encryption.aes

import android.util.Base64
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * PayDayFH
 * Created by EResolute on 11/16/2018.
 */

fun generateSecretKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance("AES")
    keyGenerator.init(256)
    return keyGenerator.generateKey()
}

fun encryptWithAES(secretKey: String, text: String) : String {
    val raw = secretKey.toByteArray()
    val sKeySpec = SecretKeySpec(raw, "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, IvParameterSpec(ByteArray(16)))
    return Base64.encodeToString(cipher.doFinal(text.toByteArray(Charset.forName("UTF-8"))), Base64.DEFAULT)
}
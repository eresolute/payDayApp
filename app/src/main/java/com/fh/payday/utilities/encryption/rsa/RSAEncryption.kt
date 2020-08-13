package com.fh.payday.utilities.encryption.rsa

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher


/**
 * PayDayFH
 * Created by EResolute on 11/16/2018.
 */

private fun readPublicKey(keyBytes: ByteArray) : PublicKey {
    val spec = X509EncodedKeySpec(keyBytes)
    val kf = KeyFactory.getInstance("RSA")
    return kf.generatePublic(spec)
}

fun encryptWithRSA(keyBytes: ByteArray, secret: String): String {
    val publicKey = readPublicKey(keyBytes)
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    return Base64.encodeToString(cipher.doFinal(secret.toByteArray()), Base64.DEFAULT)
}
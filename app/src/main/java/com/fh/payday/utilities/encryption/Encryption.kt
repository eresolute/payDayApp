package com.fh.payday.utilities.encryption


import android.annotation.TargetApi
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import java.security.InvalidKeyException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException

class Encryption(context: Context) {

    companion object {
        const val DEFAULT_KEY_STORE_NAME = "default_keystore"

        const val MASTER_KEY = "MASTER_KEY"
        const val FINGERPRINT_KEY = "FINGERPRINT_KEY"
        const val CONFIRM_CREDENTIALS_KEY = "CONFIRM_CREDENTIALS_KEY"

        val KEY_VALIDATION_DATA = byteArrayOf(0, 1, 0, 1)
        const val CONFIRM_CREDENTIALS_VALIDATION_DELAY = 120 // Seconds
    }

    private val keyStoreWrapper = KeyStoreWrapper(context, DEFAULT_KEY_STORE_NAME)

    fun createMasterKey(password: String? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createAndroidSymmetricKey()
        } else {
            createDefaultSymmetricKey(password ?: "")
        }
    }

    fun removeMasterKey() {
        keyStoreWrapper.removeAndroidKeyStoreKey(MASTER_KEY)
    }

    fun encrypt(data: String, keyPassword: String? = null): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            encryptWithAndroidSymmetricKey(data)
        } else {
            encryptWithDefaultSymmetricKey(data, keyPassword ?: "")
        }
    }

    fun decrypt(data: String, keyPassword: String? = null): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decryptWithAndroidSymmetricKey(data)
        } else {
            decryptWithDefaultSymmetricKey(data, keyPassword ?: "")
        }
    }

    private fun createAndroidSymmetricKey() {
        val key = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)
        if (key == null)
            keyStoreWrapper.createAndroidKeyStoreSymmetricKey(MASTER_KEY)
    }

    private fun encryptWithAndroidSymmetricKey(data: String): String {
        val masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).encrypt(data, masterKey, true)
    }

    private fun decryptWithAndroidSymmetricKey(data: String): String {
        val masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).decrypt(data, masterKey, true)
    }

    private fun createDefaultSymmetricKey(password: String) {
        val key = keyStoreWrapper.getDefaultKeyStoreSymmetricKey(MASTER_KEY, password)
        if (key == null)
            keyStoreWrapper.createDefaultKeyStoreSymmetricKey(MASTER_KEY, password)
    }

    private fun encryptWithDefaultSymmetricKey(data: String, keyPassword: String): String {
        val masterKey = keyStoreWrapper.getDefaultKeyStoreSymmetricKey(MASTER_KEY, keyPassword)
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).encrypt(data, masterKey, true)
    }

    private fun decryptWithDefaultSymmetricKey(data: String, keyPassword: String): String {
        val masterKey = keyStoreWrapper.getDefaultKeyStoreSymmetricKey(MASTER_KEY, keyPassword)
        return masterKey?.let { CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).decrypt(data, masterKey, true) } ?: ""
    }

    fun createFingerprintKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyStoreWrapper.createAndroidKeyStoreSymmetricKey(FINGERPRINT_KEY,
                userAuthenticationRequired = true,
                invalidatedByBiometricEnrollment = true,
                userAuthenticationValidWhileOnBody = false)
        }
    }

    fun removeFingerprintKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyStoreWrapper.removeAndroidKeyStoreKey(FINGERPRINT_KEY)
        }
    }

    fun prepareFingerprintCryptoObject(): FingerprintManager.CryptoObject? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val symmetricKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(FINGERPRINT_KEY)
                val cipher = CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).cipher
                cipher.init(Cipher.ENCRYPT_MODE, symmetricKey)
                FingerprintManager.CryptoObject(cipher)
            } catch (e: Throwable) {
                // VerifyError will be thrown on API lower then 23 if we will use unedited
                // class reference directly in catch block
                if (e is KeyPermanentlyInvalidatedException || e is IllegalBlockSizeException) {
                    return null
                } else if (e is InvalidKeyException) {
                    // Fingerprint key was not generated
                    return null
                }
                throw e
            }
        } else null
    }

    @TargetApi(23)
    fun validateFingerprintAuthentication(cryptoObject: FingerprintManager.CryptoObject): Boolean {
        try {
            cryptoObject.cipher.doFinal(KEY_VALIDATION_DATA)
            return true
        } catch (e: Throwable) {
            if (e is KeyPermanentlyInvalidatedException || e is IllegalBlockSizeException) {
                return false
            }
            throw e
        }
    }

    fun createConfirmCredentialsKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyStoreWrapper.createAndroidKeyStoreSymmetricKey(
                CONFIRM_CREDENTIALS_KEY,
                userAuthenticationRequired = true,
                userAuthenticationValidityDurationSeconds = CONFIRM_CREDENTIALS_VALIDATION_DELAY)
        }
    }

    fun removeConfirmCredentialsKey() {
        keyStoreWrapper.removeAndroidKeyStoreKey(CONFIRM_CREDENTIALS_KEY)
    }

    fun validateConfirmCredentialsAuthentication(): Boolean {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M) {
            return true
        }

        val symmetricKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(CONFIRM_CREDENTIALS_KEY)
        val cipherWrapper = CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC)

        try {
            return if (symmetricKey != null) {
                cipherWrapper.encrypt(KEY_VALIDATION_DATA.toString(), symmetricKey)
                true
            } else false
        } catch (e: Throwable) {
            if (e is UserNotAuthenticatedException || e is KeyPermanentlyInvalidatedException) {
                return false
            } else if (e is InvalidKeyException) {
                return false
            }
            throw e
        }
    }

}
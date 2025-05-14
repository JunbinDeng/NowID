package com.nowid.safe.datastore

import android.security.keystore.KeyPermanentlyInvalidatedException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class FakeKeyProvider(
    private val throwOnDecryptInit: Boolean = false
) : KeyProvider {
    override fun getEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = SecretKeySpec(ByteArray(16) { 1 }, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    override fun getDecryptCipher(iv: ByteArray): Cipher {
        if (throwOnDecryptInit) {
            throw KeyPermanentlyInvalidatedException("Simulated")
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = SecretKeySpec(ByteArray(16) { 1 }, "AES")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher
    }
}
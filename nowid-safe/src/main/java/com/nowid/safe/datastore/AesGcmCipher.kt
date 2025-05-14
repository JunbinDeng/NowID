package com.nowid.safe.datastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import com.google.protobuf.ByteString
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


/**
 * Utility object for AES/GCM/NoPadding encryption and decryption using AndroidKeyStore.
 */
internal object AesGcmCipher {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_ALIAS = "nowid_key"
    private const val TAG_LENGTH = 128
    private const val USER_AUTHENTICATION_VALIDITY_DURATION_SECONDS = 60

    fun initCipher(mode: Int, iv: ByteString? = null): Cipher {
        // Never export secret keys, only use them for cipher initialization
        val key = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)

        try {
            if (mode == Cipher.ENCRYPT_MODE) {
                cipher.init(Cipher.ENCRYPT_MODE, key)
            } else {
                val spec = GCMParameterSpec(TAG_LENGTH, iv?.toByteArray())
                cipher.init(Cipher.DECRYPT_MODE, key, spec)
            }
        } catch (e: KeyPermanentlyInvalidatedException) {
            Timber.e(e)
            // Key has been permanently invalidated. Delete old key and recreate.
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            keyStore.deleteEntry(KEY_ALIAS)
            // Note: data encrypted with the previous key can no longer be decrypted.
            cipher.init(mode, key)
        }

        return cipher
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) return existingKey.secretKey

        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        @Suppress("DEPRECATION")
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationValidityDurationSeconds(
                USER_AUTHENTICATION_VALIDITY_DURATION_SECONDS
            )
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
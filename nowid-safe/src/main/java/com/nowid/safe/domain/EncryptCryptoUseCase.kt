package com.nowid.safe.domain

import android.security.keystore.UserNotAuthenticatedException
import androidx.biometric.BiometricPrompt.CryptoObject
import com.google.protobuf.ByteString
import com.nowid.safe.data.repository.PasswordRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Handles biometric encryption and decryption by retrieving a [CryptoObject]
 * based on the specified [EncryptionMode].
 *
 * - For [EncryptionMode.ENCRYPT] mode, it returns a new CryptoObject for encryption.
 * - For [EncryptionMode.DECRYPT] mode, it requires an IV and returns a CryptoObject for decryption.
 * - Returns null if the user is not authenticated or an error occurs.
 *
 * @param repository Provides access to the encryption and decryption crypto objects.
 */
class BiometricEncryptUseCase @Inject constructor(
    private val repository: PasswordRepository
) {
    fun tryGetCrypto(
        encryptionMode: EncryptionMode,
        iv: ByteString? = null,
    ): CryptoObject? = try {
        if (encryptionMode == EncryptionMode.DECRYPT) {
            if (iv == null) {
                throw IllegalStateException("Missing IV for decryption")
            }
            repository.getDecryptCrypto(iv)
        } else if (encryptionMode == EncryptionMode.ENCRYPT) {
            repository.getEncryptCrypto()
        } else {
            throw IllegalArgumentException("Invalid encryption mode")
        }
    } catch (e: UserNotAuthenticatedException) {
        Timber.e(e)
        null
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}

enum class EncryptionMode {
    ENCRYPT,
    DECRYPT,
}
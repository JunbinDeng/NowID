package com.nowid.safe.domain

import androidx.biometric.BiometricPrompt.CryptoObject
import com.google.protobuf.ByteString
import com.nowid.safe.data.repository.PasswordRepository
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
    ): Result<CryptoObject> {
        return try {
            if (encryptionMode == EncryptionMode.DECRYPT) {
                if (iv == null) {
                    throw IllegalStateException("Missing IV for decryption")
                }
                val crypto = repository.getDecryptCrypto(iv)
                return Result.success(crypto)
            } else if (encryptionMode == EncryptionMode.ENCRYPT) {
                val crypto = repository.getEncryptCrypto()
                return Result.success(crypto)
            } else {
                throw IllegalArgumentException("Invalid encryption mode")
            }
        } catch (e: Exception) {
            return Result.failure<CryptoObject>(e)
        }
    }
}

enum class EncryptionMode {
    ENCRYPT,
    DECRYPT,
}
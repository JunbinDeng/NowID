package com.nowid.safe.domain

import android.security.keystore.UserNotAuthenticatedException
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.protobuf.ByteString
import com.nowid.safe.data.repository.PasswordRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for performing biometric encryption or decryption operations.
 *
 * This use case attempts to obtain a BiometricPrompt.CryptoObject for either
 * encryption or decryption by:
 * 1. Fetching a CryptoObject from the repository (encrypt or decrypt).
 * 2. Prompting the user for biometric authentication if the CryptoObject is null
 *    due to lack of authentication.
 * 3. Returning a Result containing the CryptoObject on success, or a
 *    UserNotAuthenticatedException on failure.
 *
 * @param repository the PasswordRepository providing crypto operations
 */
class BiometricEncryptUseCase @Inject constructor(
    private val repository: PasswordRepository
) {
    operator fun invoke(
        activity: FragmentActivity,
        promptBuilder: BiometricPrompt.PromptInfo,
        encryptionMode: EncryptionMode,
        iv: ByteString? = null,
        onResult: (Result<CryptoObject>) -> Unit
    ) {
        // Attempt to get a CryptoObject; fall back if keystore requires authentication first
        fun tryGetCrypto(): CryptoObject? = try {
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

        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    try {
                        val crypto: CryptoObject? = tryGetCrypto()
                        if (crypto != null) {
                            onResult(Result.success(crypto))
                        } else {
                            onResult(Result.failure(UserNotAuthenticatedException("Authentication failed")))
                        }
                    } catch (e: Exception) {
                        onResult(Result.failure(e))
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onResult(Result.failure(UserNotAuthenticatedException(errString.toString())))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(Result.failure(UserNotAuthenticatedException("Authentication failed")))
                }
            })

        val crypto: CryptoObject? = tryGetCrypto()
        if (crypto != null) {
            onResult(Result.success(crypto))
        } else {
            // Authenticate without CryptoObject
            biometricPrompt.authenticate(promptBuilder)
        }
    }
}

enum class EncryptionMode {
    ENCRYPT,
    DECRYPT,
}
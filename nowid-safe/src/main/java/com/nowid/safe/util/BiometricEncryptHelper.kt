package com.nowid.safe.util

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Authenticates the user using biometrics and runs [onSuccess] or [onError].
 *
 * Tries to get a [CryptoObject] via [tryGetCrypto]. If found, skips prompt and calls [onSuccess].
 * Otherwise, shows biometric prompt with [promptInfo]. Handles result via callbacks.
 *
 * @param activity Host activity to show the prompt.
 * @param promptInfo Biometric prompt UI configuration.
 * @param tryGetCrypto Attempts to retrieve a CryptoObject.
 * @param onSuccess Called after successful auth or CryptoObject retrieval.
 * @param onError Called with an error on failure.
 */
fun performBiometricEncryption(
    activity: FragmentActivity,
    promptInfo: BiometricPrompt.PromptInfo,
    tryGetCrypto: () -> Result<CryptoObject>,
    onSuccess: (crypto: CryptoObject) -> Unit,
    onError: (e: Throwable) -> Unit
) {
    val result = tryGetCrypto()
    val cryptoObject = result.getOrNull()

    val exception = result.exceptionOrNull()
    when (exception) {
        is KeyPermanentlyInvalidatedException -> return onError(exception)
    }

    if (cryptoObject != null) {
        onSuccess(cryptoObject)
        return
    }

    BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                var cryptoObject = result.cryptoObject
                if (cryptoObject != null) {
                    return onSuccess(cryptoObject)
                }

                val cryptoResult = tryGetCrypto()
                cryptoObject = cryptoResult.getOrNull()
                if (cryptoResult.isSuccess && cryptoObject != null) {
                    onSuccess(cryptoObject)
                    return
                }

                onError(
                    cryptoResult.exceptionOrNull()
                        ?: IllegalStateException("Authentication succeeded but no cryptoObject found")
                )
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(IllegalArgumentException(errString.toString()))
            }

            override fun onAuthenticationFailed() {
                onError(IllegalArgumentException("Authentication failed"))
            }
        }
    ).authenticate(promptInfo)
}
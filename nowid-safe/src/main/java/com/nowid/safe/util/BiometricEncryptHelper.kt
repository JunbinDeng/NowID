package com.nowid.safe.util

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
 * @param onError Called with an error message on failure.
 */
fun performBiometricEncryption(
    activity: FragmentActivity,
    promptInfo: BiometricPrompt.PromptInfo,
    tryGetCrypto: () -> CryptoObject?,
    onSuccess: (crypto: CryptoObject) -> Unit,
    onError: (String) -> Unit
) {
    val crypto = tryGetCrypto()
    if (crypto != null) {
        onSuccess(crypto)
        return
    }

    BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val cryptoObject = result.cryptoObject ?: tryGetCrypto()
                if (cryptoObject != null) {
                    onSuccess(cryptoObject)
                } else {
                    onError("Authentication succeeded but no cryptoObject found")
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onError("Authentication failed")
            }
        }
    ).authenticate(promptInfo)
}
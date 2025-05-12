package com.nowid.safe.ui.feature.password.add

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nowid.safe.R
import com.nowid.safe.data.repository.PasswordRepository
import com.nowid.safe.domain.BiometricEncryptUseCase
import com.nowid.safe.domain.EncryptionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddPasswordViewModel @Inject constructor(
    private val biometricCryptoUseCase: BiometricEncryptUseCase,
    private val repository: PasswordRepository
) : ViewModel() {
    fun addPassword(
        activity: FragmentActivity,
        title: String,
        plain: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val promptBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_prompt_encrypt_title))
            .setSubtitle(activity.getString(R.string.biometric_prompt_encrypt_subtitle))
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        fun addPasswordWithCrypto(crypto: CryptoObject) {
            viewModelScope.launch {
                try {
                    repository.savePasswordWithCrypto(
                        UUID.randomUUID().toString(),
                        title,
                        plain,
                        crypto
                    )
                    onResult(Result.success(Unit))
                } catch (e: Exception) {
                    Timber.e(e)
                    onResult(Result.failure(e))
                }
            }
        }

        biometricCryptoUseCase(
            activity,
            promptBuilder,
            EncryptionMode.ENCRYPT
        ) { result ->
            if (result.isSuccess) {
                val crypto = repository.getEncryptCrypto()
                addPasswordWithCrypto(crypto)
            } else {
                onResult(Result.failure(result.exceptionOrNull()!!))
            }
        }
    }
}
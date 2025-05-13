package com.nowid.safe.ui.feature.password.detail

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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PasswordDetailViewModel.Factory::class)
class PasswordDetailViewModel @AssistedInject constructor(
    private val biometricCryptoUseCase: BiometricEncryptUseCase,
    private val repository: PasswordRepository, @Assisted val id: String
) : ViewModel() {
    /**
     * Factory for assisted injection of [PasswordDetailViewModel], providing the required password ID.
     */
    @AssistedFactory
    interface Factory {
        fun create(
            id: String,
        ): PasswordDetailViewModel
    }

    private val _title = MutableStateFlow<String?>(null)
    val title: StateFlow<String?> = _title.asStateFlow()

    private val _decrypted = MutableStateFlow<String?>(null)
    val decrypted: StateFlow<String?> = _decrypted.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun loadPassword(activity: FragmentActivity, id: String) {
        viewModelScope.launch {
            val encryptedData = repository.getEncryptedData(id) ?: return@launch
            _title.value = encryptedData.passwordItem.title

            val promptBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.biometric_prompt_decrypt_title))
                .setSubtitle(activity.getString(R.string.biometric_prompt_decrypt_subtitle))
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()

            fun loadPasswordWithCrypto(crypto: CryptoObject) {
                viewModelScope.launch {
                    _authError.value = null

                    try {
                        val plain = repository.loadPasswordWithCrypto(id, crypto)
                        _decrypted.value = plain
                    } catch (e: Exception) {
                        Timber.e(e)
                        _authError.value = e.message
                    }
                }
            }

            biometricCryptoUseCase(
                activity,
                promptBuilder,
                EncryptionMode.DECRYPT,
                encryptedData.iv
            ) { result ->
                if (result.isSuccess) {
                    loadPasswordWithCrypto(result.getOrThrow())
                } else {
                    _authError.value = result.exceptionOrNull()?.message
                }
            }
        }
    }
}
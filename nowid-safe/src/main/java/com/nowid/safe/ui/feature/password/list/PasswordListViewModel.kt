package com.nowid.safe.ui.feature.password.list

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nowid.safe.R
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore.EncryptedPasswordData.PasswordItem
import com.nowid.safe.data.repository.PasswordRepository
import com.nowid.safe.domain.BiometricEncryptUseCase
import com.nowid.safe.domain.EncryptionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordListViewModel @Inject constructor(
    private val biometricCryptoUseCase: BiometricEncryptUseCase,
    private val repository: PasswordRepository
) : ViewModel() {
    val items: StateFlow<List<PasswordItem>> = repository
        .observePasswordItems()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            emptyList()
        )

    fun authenticateToViewPassword(
        activity: FragmentActivity,
        id: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val encryptedData = repository.getEncryptedData(id) ?: return@launch

            val promptBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.biometric_prompt_decrypt_title))
                .setSubtitle(activity.getString(R.string.biometric_prompt_decrypt_subtitle))
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()

            biometricCryptoUseCase(
                activity,
                promptBuilder,
                EncryptionMode.DECRYPT,
                encryptedData.iv
            ) { result ->
                if (result.isSuccess) {
                    onResult(Result.success(Unit))
                } else {
                    onResult(Result.failure(result.exceptionOrNull()!!))
                }
            }
        }
    }
}
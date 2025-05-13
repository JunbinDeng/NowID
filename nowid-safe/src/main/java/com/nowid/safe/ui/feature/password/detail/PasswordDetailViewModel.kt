package com.nowid.safe.ui.feature.password.detail

import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.ByteString
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore.EncryptedPasswordData
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

    suspend fun getEncryptedData(): EncryptedPasswordData? {
        return repository.getEncryptedData(id).apply {
            _title.value = this?.passwordItem?.title
        }
    }

    fun tryGetCrypto(iv: ByteString?): CryptoObject? {
        return biometricCryptoUseCase.tryGetCrypto(EncryptionMode.DECRYPT, iv)
    }

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
}
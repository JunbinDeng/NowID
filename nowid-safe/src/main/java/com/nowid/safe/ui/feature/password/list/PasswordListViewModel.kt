package com.nowid.safe.ui.feature.password.list

import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.ByteString
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore.EncryptedPasswordData
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

    suspend fun getEncryptedData(id: String): EncryptedPasswordData? {
        return repository.getEncryptedData(id)
    }

    fun tryGetCrypto(iv: ByteString?): Result<CryptoObject> =
        biometricCryptoUseCase.tryGetCrypto(EncryptionMode.DECRYPT, iv)

    fun loadPasswordWithCrypto(
        id: String,
        crypto: CryptoObject,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val plain = repository.loadPasswordWithCrypto(id, crypto)!!
                onResult(Result.success(plain))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }
}
package com.nowid.safe.ui.feature.password.add

import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nowid.safe.data.repository.PasswordRepository
import com.nowid.safe.domain.BiometricEncryptUseCase
import com.nowid.safe.domain.EncryptionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddPasswordViewModel @Inject constructor(
    private val biometricCryptoUseCase: BiometricEncryptUseCase,
    private val repository: PasswordRepository
) : ViewModel() {
    fun tryGetCrypto(): CryptoObject? {
        return biometricCryptoUseCase.tryGetCrypto(EncryptionMode.ENCRYPT, null)
    }

    fun addPassword(
        title: String,
        plain: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val crypto = repository.getEncryptCrypto()
            val result = addPasswordWithCrypto(title, plain, crypto)
            onResult(result)
        }
    }

    private suspend fun addPasswordWithCrypto(
        title: String,
        plain: String,
        crypto: CryptoObject
    ): Result<Unit> {
        try {
            repository.savePasswordWithCrypto(
                UUID.randomUUID().toString(),
                title,
                plain,
                crypto
            )
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
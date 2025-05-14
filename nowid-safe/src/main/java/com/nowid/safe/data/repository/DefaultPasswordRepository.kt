package com.nowid.safe.data.repository

import androidx.annotation.VisibleForTesting
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.datastore.core.DataStore
import com.google.protobuf.ByteString
import com.google.protobuf.kotlin.toByteString
import com.nowid.safe.common.Dispatcher
import com.nowid.safe.common.NowIDDispatchers.IO
import com.nowid.safe.data.PasswordStoreKt.EncryptedPasswordDataKt.passwordItem
import com.nowid.safe.data.PasswordStoreKt.encryptedPasswordData
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore.EncryptedPasswordData
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore.EncryptedPasswordData.PasswordItem
import com.nowid.safe.datastore.KeyProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.crypto.AEADBadTagException
import javax.inject.Inject

/**
 * Default implementation of [PasswordRepository] using a DataStore backend.
 */
class DefaultPasswordRepository @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    @get:VisibleForTesting val passwordStore: DataStore<PasswordStore>,
    @get:VisibleForTesting val keyProvider: KeyProvider,
) : PasswordRepository {
    override fun observePasswordItems(): Flow<List<PasswordItem>> =
        passwordStore.data
            .map { store -> store.entriesList.map { it.passwordItem } }
            .flowOn(ioDispatcher)


    override fun getEncryptCrypto(): CryptoObject {
        val cipher = keyProvider.getEncryptCipher()
        return CryptoObject(cipher)
    }

    override suspend fun savePasswordWithCrypto(
        id: String, title: String, plain: String, crypto: CryptoObject
    ) {
        return withContext(ioDispatcher) {
            val cipher =
                crypto.cipher ?: throw IllegalStateException("Encryption cipher unavailable")
            val ivBytes = cipher.iv

            try {
                val encryptedBytes = cipher.doFinal(plain.toByteArray())

                val newEntry = encryptedPasswordData {
                    passwordItem = passwordItem {
                        this.id = id
                        this.title = title
                    }
                    iv = ivBytes.toByteString()
                    encryptedPassword = encryptedBytes.toByteString()
                }

                passwordStore.updateData { store ->
                    store.toBuilder().apply {
                        // Update the locally stored data object with the new ID received
                        clearEntries()
                        addAllEntries(
                            store.entriesList
                                .filter { it.passwordItem.id != newEntry.passwordItem.id }
                                .toMutableList()
                                .apply { add(newEntry) }
                        )
                    }.build()
                }
            } catch (e: AEADBadTagException) {
                // Key invalidated due to device credential change
                deletePassword(id)
                throw e
            }
        }
    }

    override suspend fun getEncryptedData(id: String): EncryptedPasswordData? =
        passwordStore.data.map { store ->
            store.entriesList.firstOrNull { it.passwordItem.id == id }
        }.firstOrNull()

    override fun getDecryptCrypto(iv: ByteString): CryptoObject {
        val cipher = keyProvider.getDecryptCipher(iv.toByteArray())
        return CryptoObject(cipher)
    }

    override suspend fun loadPasswordWithCrypto(
        id: String, crypto: CryptoObject
    ): String? = withContext(ioDispatcher) {
        val data = getEncryptedData(id) ?: throw IllegalStateException("Data not found")
        val cipher = crypto.cipher ?: throw IllegalStateException("Decryption cipher unavailable")
        val encryptedBytes = data.encryptedPassword.toByteArray()

        try {
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: AEADBadTagException) {
            // Key invalidated due to device credential change
            deletePassword(id)
            throw e
        }
    }

    override suspend fun deletePassword(id: String) {
        return withContext(ioDispatcher) {
            passwordStore.updateData { store ->
                store.toBuilder().apply {
                    // Remove the entry with the matching id
                    clearEntries()
                    addAllEntries(
                        store.entriesList.filter { it.passwordItem.id != id }
                    )
                }.build()
            }
        }
    }
}
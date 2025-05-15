package com.nowid.safe.data.repository

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.datastore.core.DataStore
import com.google.protobuf.kotlin.toByteString
import com.nowid.safe.data.PasswordStoreKt.EncryptedPasswordDataKt.passwordItem
import com.nowid.safe.data.PasswordStoreKt.encryptedPasswordData
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore
import com.nowid.safe.datastore.FakeKeyProvider
import com.nowid.safe.datastore.KeyProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher

class DefaultPasswordRepositoryTest {
    private class FakeDataStore(
        initial: PasswordStore = PasswordStore.getDefaultInstance()
    ) : DataStore<PasswordStore> {
        // A simple in-memory fake DataStore for testing
        private val _state = MutableStateFlow(initial)
        override val data: StateFlow<PasswordStore> get() = _state

        override suspend fun updateData(transform: suspend (PasswordStore) -> PasswordStore): PasswordStore {
            val newValue = transform(_state.value)
            _state.value = newValue
            return newValue
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createRepositoryWithFakeStore(fakeKeyProvider: KeyProvider = FakeKeyProvider())
            : DefaultPasswordRepository {
        val fakeDispatcher = UnconfinedTestDispatcher()
        val fakeStore = FakeDataStore()
        val repository = DefaultPasswordRepository(
            fakeDispatcher, fakeStore,
            fakeKeyProvider
        )
        return repository
    }

    private val mockCipherEncrypt: Cipher = mock()
    private val mockCipherDecrypt: Cipher = mock()
    private val mockCryptoObjectDecrypt = CryptoObject(mockCipherDecrypt)

    @Test
    fun `observe password items flow with empty store`() = runTest {
        val repository = createRepositoryWithFakeStore()

        val items = repository.observePasswordItems().take(1).toList(mutableListOf())

        assertEquals(1, items.size)
        assertTrue(items[0].isEmpty())
    }

    @Test
    fun `observe password items flow with populated store`() = runTest {
        val repository = createRepositoryWithFakeStore()

        val fakeStore = repository.passwordStore as FakeDataStore

        val item1 = encryptedPasswordData {
            passwordItem = passwordItem {
                this.id = "id1"
                this.title = "title1"
            }
            iv = ByteArray(0).toByteString()
            encryptedPassword = ByteArray(0).toByteString()
        }
        val item2 = encryptedPasswordData {
            passwordItem = passwordItem {
                this.id = "id2"
                this.title = "title2"
            }
            iv = ByteArray(0).toByteString()
            encryptedPassword = ByteArray(0).toByteString()
        }

        fakeStore.updateData { current ->
            current.toBuilder()
                .addEntries(item1)
                .addEntries(item2)
                .build()
        }

        val items = repository.observePasswordItems().take(1).toList(mutableListOf())
        assertEquals(1, items.size)
        val list = items[0]
        assertEquals(2, list.size)

        assertTrue(list.any { it.id == "id1" && it.title == "title1" })
        assertTrue(list.any { it.id == "id2" && it.title == "title2" })
    }

    @Test(expected = IllegalStateException::class)
    fun `save password with null crypto cipher`() = runTest {
        val repository = createRepositoryWithFakeStore()

        val id = "id1"
        val title = "title1"
        val password = "password1"
        val cryptoObject = object : CryptoObject(mockCipherEncrypt) {
            override fun getCipher(): Cipher? = null
        }

        repository.savePasswordWithCrypto(id, title, password, cryptoObject)
    }

    @Test(expected = AEADBadTagException::class)
    fun `save password with AEADBadTagException`() = runTest {
        val repository = createRepositoryWithFakeStore()

        val id = "idAead"
        val title = "title1"
        val password = "password1"
        val cryptoObject = object : CryptoObject(mockCipherEncrypt) {
            override fun getCipher(): Cipher? = throw AEADBadTagException()
        }

        repository.savePasswordWithCrypto(id, title, password, cryptoObject)
    }


    @Test
    fun `get encrypted data for non existent id`() = runTest {
        val repository = createRepositoryWithFakeStore()

        val encryptedData = repository.getEncryptedData("nonexistentId")

        assertNull(encryptedData)
    }

    @Test(expected = IllegalStateException::class)
    fun `load password for non existent id`() = runTest {
        val repository = createRepositoryWithFakeStore()

        val cryptoObject = mockCryptoObjectDecrypt

        repository.loadPasswordWithCrypto("nonexistentId", cryptoObject)
    }

    @Test(expected = IllegalStateException::class)
    fun `load password with null crypto cipher`() = runTest {
        val repository = createRepositoryWithFakeStore()

        // Insert an entry with id "id" before calling loadPasswordWithCrypto
        val fakeStore = repository.passwordStore as FakeDataStore
        val item = encryptedPasswordData {
            passwordItem = passwordItem {
                this.id = "id"
                this.title = "test"
            }
            iv = ByteArray(0).toByteString()
            encryptedPassword = ByteArray(0).toByteString()
        }
        fakeStore.updateData { current ->
            current.toBuilder().addEntries(item).build()
        }

        // Create a CryptoObject with a null cipher
        val cryptoObject = object : CryptoObject(mockCipherDecrypt) {
            override fun getCipher(): Cipher? = null
        }

        repository.loadPasswordWithCrypto("id", cryptoObject)
    }

    @Test
    fun `delete password for non existent id`() = runTest {
        val repository = createRepositoryWithFakeStore()

        // Should not throw or fail
        repository.deletePassword("nonexistentId")
        // DataStore remains unchanged (empty)
        val items = repository.observePasswordItems().take(1).toList(mutableListOf())

        assertTrue(items[0].isEmpty())
    }

    @Test
    fun `load password with key permanently invalidated exception`() = runTest {
        val repository = createRepositoryWithFakeStore(FakeKeyProvider(throwOnDecryptInit = true))

        val iv = ByteArray(0).toByteString()

        assertThrows(KeyPermanentlyInvalidatedException::class.java) {
            // Attempt to decrypt
            repository.getDecryptCrypto(iv)
        }
    }
}
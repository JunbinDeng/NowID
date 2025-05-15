package com.nowid.safe.data.repository

import androidx.datastore.core.DataStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.protobuf.kotlin.toByteString
import com.nowid.safe.common.Dispatcher
import com.nowid.safe.common.NowIDDispatchers.IO
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore.EncryptedPasswordData.PasswordItem
import com.nowid.safe.datastore.AesGcmKeyProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.crypto.AEADBadTagException
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DefaultPasswordRepositoryInstrumentedTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Dispatcher(IO)
    lateinit var fakeDispatcher: CoroutineDispatcher

    @Before
    fun setup() {
        hiltRule.inject()
    }

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
    private fun createRepositoryWithFakeStore(): DefaultPasswordRepository {
        // A controlled environment without interacting with actual data storage
        val fakeStore = FakeDataStore()
        val keyProvider = AesGcmKeyProvider()
        val repository = DefaultPasswordRepository(fakeDispatcher, fakeStore, keyProvider)
        return repository
    }

    @Test
    fun observePasswordItemsFlowWithUpdates() = runTest {
        val repository = createRepositoryWithFakeStore()

        val validCrypto1 = repository.getEncryptCrypto()
        val validCrypto2 = repository.getEncryptCrypto()
        checkNotNull(validCrypto1.cipher) { "Encrypt cipher1 is null" }
        checkNotNull(validCrypto2.cipher) { "Encrypt cipher2 is null" }

        val collected = mutableListOf<List<PasswordItem>>()
        val job = launch {
            repository.observePasswordItems().take(3).toList(collected)
        }

        val id1 = "id1"
        val title1 = "title1"
        val password1 = "password1"
        val id2 = "id2"
        val title2 = "title2"
        val password2 = "password2"

        repository.savePasswordWithCrypto(id1, title1, password1, validCrypto1)
        // Yield to allow emission after repository update
        yield()

        repository.savePasswordWithCrypto(id2, title2, password2, validCrypto2)
        yield()

        repository.deletePassword("id1")
        yield()

        // Wait for the flow to emit all values
        job.join()

        // Expect three emissions: [], [id1, id2], [id2]
        assertEquals(3, collected.size)
        // Second emission should contain id1 (after first two saves)
        assertTrue(collected[1].any { it.id == "id1" })
        // Third emission should not contain id1 (after deletion)
        assertTrue(collected[2].none { it.id == "id1" })
    }

    @Test
    fun getDecryptCryptoObject() {
        val repository = createRepositoryWithFakeStore()

        val iv = ByteArray(12) { 0 }
        val cryptoObject = repository.getDecryptCrypto(iv.toByteString())

        assertNotNull(cryptoObject)
        assertNotNull(cryptoObject.cipher)
    }

    @Test
    fun savePasswordWithValidCryptoAndData() = runTest {
        val repository = createRepositoryWithFakeStore()

        val validCrypto = repository.getEncryptCrypto()
        checkNotNull(validCrypto.cipher) { "Encrypt cipher is null" }

        val id = "id1"
        val title = "title1"
        val password = "password1"
        repository.savePasswordWithCrypto(id, title, password, validCrypto)

        val encryptedData = repository.getEncryptedData(id)
        assertNotNull(encryptedData)
        assertEquals(id, encryptedData?.passwordItem?.id)
        assertEquals(title, encryptedData?.passwordItem?.title)
    }

    @Test
    fun getEncryptedDataForExistingId() = runTest {
        val repository = createRepositoryWithFakeStore()

        val validCrypto = repository.getEncryptCrypto()
        checkNotNull(validCrypto.cipher) { "Encrypt cipher is null" }

        val id = "existingId"
        val title = "title"
        val password = "password"
        repository.savePasswordWithCrypto(id, title, password, validCrypto)

        val encryptedData = repository.getEncryptedData(id)
        assertNotNull(encryptedData)
        assertEquals(id, encryptedData?.passwordItem?.id)
    }

    @Test
    fun loadPasswordWithValidCryptoAndData() = runTest {
        val repository = createRepositoryWithFakeStore()

        val validCrypto = repository.getEncryptCrypto()
        checkNotNull(validCrypto.cipher) { "Encrypt cipher is null" }

        val id = "idLoad"
        val title = "titleLoad"
        val password = "passwordLoad"
        repository.savePasswordWithCrypto(id, title, password, validCrypto)

        val encryptedData = repository.getEncryptedData(id)
        assertNotNull(encryptedData)

        val cryptoObject = repository.getDecryptCrypto(encryptedData!!.iv)
        val loadedPassword = repository.loadPasswordWithCrypto(id, cryptoObject)
        assertEquals(password, loadedPassword)
    }

    @Test
    fun savePasswordWithDuplicateId() = runTest {
        val repository = createRepositoryWithFakeStore()

        val validCrypto1 = repository.getEncryptCrypto()
        val validCrypto2 = repository.getEncryptCrypto()
        checkNotNull(validCrypto1.cipher) { "Encrypt cipher1 is null" }
        checkNotNull(validCrypto2.cipher) { "Encrypt cipher2 is null" }

        val id = "dupId"
        repository.savePasswordWithCrypto(id, "title1", "pass1", validCrypto1)
        repository.savePasswordWithCrypto(id, "title2", "pass2", validCrypto2)

        val encryptedData = repository.getEncryptedData(id)
        assertNotNull(encryptedData)

        assertEquals(id, encryptedData?.passwordItem?.id)
        assertEquals("title2", encryptedData?.passwordItem?.title)
    }

    @Test
    fun deletePasswordForExistingId() = runTest {
        val repository = createRepositoryWithFakeStore()

        val validCrypto = repository.getEncryptCrypto()
        checkNotNull(validCrypto.cipher) { "Encrypt cipher is null" }

        val id = "idToDelete"
        val title = "title"
        val password = "password"
        repository.savePasswordWithCrypto(id, title, password, validCrypto)

        repository.deletePassword(id)

        val encryptedData = repository.getEncryptedData(id)
        assertNull(encryptedData)
    }

    @Test
    fun loadPasswordWithAEADBadTagException() = runTest {
        val repository = createRepositoryWithFakeStore()

        val validCrypto = repository.getEncryptCrypto()
        checkNotNull(validCrypto.cipher) { "Encrypt cipher is null" }

        // Save with valid cipher
        val id = "idAeadLoad"
        val title = "title"
        val password = "password"
        repository.savePasswordWithCrypto(id, title, password, validCrypto)

        // Create a wrong IV to simulate AEADBadTagException
        val badIv = ByteArray(12) { 0 } // incorrect IV
        val badCrypto = repository.getDecryptCrypto(badIv.toByteString())
        checkNotNull(badCrypto.cipher) { "Decrypt cipher is null" }

        assertThrows(AEADBadTagException::class.java) {
            runBlocking {
                repository.loadPasswordWithCrypto(id, badCrypto)
            }
        }
    }
}
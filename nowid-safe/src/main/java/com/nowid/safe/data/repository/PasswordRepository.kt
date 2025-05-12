package com.nowid.safe.data.repository

import androidx.biometric.BiometricPrompt.CryptoObject
import com.google.protobuf.ByteString
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore.EncryptedPasswordData
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore.EncryptedPasswordData.PasswordItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for secure storage and retrieval of passwords using biometric crypto.
 */
interface PasswordRepository {

    /**
     * Observes the list of stored password items.
     *
     * @return a Flow emitting updates to the list of PasswordItem
     */
    fun observePasswordItems(): Flow<List<PasswordItem>>

    /**
     * Provides a CryptoObject configured for encryption operations.
     *
     * @return a CryptoObject for encrypting password data
     */
    fun getEncryptCrypto(): CryptoObject

    /**
     * Encrypts and saves a plaintext password entry.
     *
     * @param id unique identifier for the password entry
     * @param title human-readable title for the password
     * @param plain the plaintext password to encrypt and store
     * @param crypto CryptoObject containing the encryption cipher
     */
    suspend fun savePasswordWithCrypto(
        id: String, title: String, plain: String, crypto: CryptoObject
    )

    /**
     * Retrieves stored encrypted password data by its identifier.
     *
     * @param id unique identifier of the password entry
     * @return EncryptedPasswordData or null if not found
     */
    suspend fun getEncryptedData(id: String): EncryptedPasswordData?

    /**
     * Provides a CryptoObject configured for decryption using the given IV.
     *
     * @param iv initialization vector used during encryption
     * @return a CryptoObject for decrypting password data
     */
    fun getDecryptCrypto(iv: ByteString): CryptoObject

    /**
     * Decrypts and loads the plaintext password using the provided CryptoObject.
     *
     * @param id unique identifier of the password entry
     * @param crypto CryptoObject containing the decryption cipher
     * @return decrypted plaintext password or null on failure
     */
    suspend fun loadPasswordWithCrypto(
        id: String, crypto: CryptoObject
    ): String?

    /**
     * Deletes the password entry identified by the given id.
     *
     * @param id unique identifier of the password entry to delete
     */
    suspend fun deletePassword(id: String)
}
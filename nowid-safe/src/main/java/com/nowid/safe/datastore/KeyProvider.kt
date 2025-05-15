package com.nowid.safe.datastore

import javax.crypto.Cipher

/**
 * Provides access to encryption and decryption ciphers backed by a secure key.
 */
interface KeyProvider {
    /**
     * Returns a cipher initialized for encryption using the secure key.
     */
    fun getEncryptCipher(): Cipher

    /**
     * Returns a cipher initialized for decryption using the secure key and the provided IV.
     *
     * @param iv the initialization vector used during encryption
     */
    fun getDecryptCipher(iv: ByteArray): Cipher

    /**
     * Deletes the stored encryption key from the secure keystore.
     */
    fun deleteEntry()
}
package com.nowid.safe.datastore

import javax.crypto.Cipher

interface KeyProvider {
    fun getEncryptCipher(): Cipher
    fun getDecryptCipher(iv: ByteArray): Cipher
}
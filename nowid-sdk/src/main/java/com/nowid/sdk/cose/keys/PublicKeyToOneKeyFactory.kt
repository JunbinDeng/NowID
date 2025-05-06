package com.nowid.sdk.cose.keys

import COSE.OneKey
import com.nowid.sdk.cose.keys.converters.EC2PublicKeyConverter
import java.security.PublicKey

/**
 * Converts Java [PublicKey] instances into COSE [OneKey] using registered converters.
 */
internal object PublicKeyToOneKeyFactory {
    private val converters = arrayOf(EC2PublicKeyConverter())

    /**
     * @param publicKey the Java public key
     * @return the corresponding COSE [OneKey]
     * @throws IllegalArgumentException if no converter supports the key algorithm
     */
    fun from(publicKey: PublicKey): OneKey {
        val converter = converters.find { it.supports(publicKey) }
            ?: throw IllegalArgumentException("Unsupported key type: ${publicKey.algorithm}")
        return converter.convert(publicKey)
    }
}
package com.nowid.sdk.cose.keys.converters

import COSE.OneKey
import java.security.PublicKey

/**
 * Converts Java [PublicKey] instances into COSE [OneKey]s.
 */
internal interface PublicKeysConverter {
    /**
     * @param publicKey the key to check
     * @return true if this converter supports the key’s algorithm
     */
    fun supports(publicKey: PublicKey): Boolean

    /**
     * @param publicKey the key to convert
     * @return the corresponding COSE [OneKey]
     * @throws IllegalArgumentException if the key’s algorithm is unsupported
     */
    fun convert(publicKey: PublicKey): OneKey
}
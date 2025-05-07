package com.nowid.sdk.processor

import com.nowid.sdk.cose.message.CoseDispatcher
import com.nowid.sdk.transport.TransportStrategy
import java.security.PublicKey

/**
 * Entry point for processing mdoc payloads from different transport sources.
 *
 * This class coordinates the flow of extracting raw payload bytes via a [TransportStrategy]
 * and verifying them using COSE signature validation through [CoseDispatcher].
 *
 * It provides a single unified interface for processing NFC, BLE, or other input methods.
 */
object MdocProcessor {
    /**
     * Extracts and verifies a CBOR-encoded mdoc payload from the given [transport].
     *
     * @param transport the strategy used to extract raw payload bytes (e.g., from NFC)
     * @param publicKey the key used to verify the COSE signature
     * @return a [Result] containing the decoded JSON string on success, or a failure on error
     */
    fun process(
        transport: TransportStrategy,
        publicKey: PublicKey
    ): Result<String> {
        return transport.extractPayload()
            .fold(
                onSuccess = { payload -> CoseDispatcher.dispatch(payload, publicKey) },
                onFailure = { error -> Result.failure(error) }
            )
    }
}
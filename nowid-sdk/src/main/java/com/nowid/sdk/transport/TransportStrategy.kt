package com.nowid.sdk.transport

/**
 * Strategy interface for retrieving raw CBOR-encoded payloads from different transport sources
 * (e.g., NFC, BLE, mock data).
 */
interface TransportStrategy {
    /**
     * Extracts the CBOR payload bytes from the underlying transport mechanism.
     *
     * @return a [Result] containing the raw payload on success, or an error on failure
     */
    fun extractPayload(): Result<ByteArray>
}
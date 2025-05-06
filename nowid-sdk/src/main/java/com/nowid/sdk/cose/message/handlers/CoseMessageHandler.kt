package com.nowid.sdk.cose.message.handlers

import COSE.MessageTag
import java.security.PublicKey

/**
 * Handler for COSE messages (e.g., Sign1, Mac0, Encrypt0).
 */
internal interface CoseMessageHandler {
    /**
     * Returns whether this handler can process the given COSE message tag.
     *
     * @param messageTag the COSE [MessageTag]
     * @return true if supported, false otherwise
     */
    fun supports(messageTag: MessageTag): Boolean

    /**
     * Processes the raw COSE payload using the provided public key.
     *
     * @param payload    the signed or encrypted data
     * @param publicKey  the key for signature verification or decryption
     * @return a [Result] containing a JSON string of the parsed payload on success,
     *         or an error on failure
     */
    fun handle(payload: ByteArray, publicKey: PublicKey): Result<String>
}
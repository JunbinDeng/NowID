package com.nowid.sdk.cose.message

import COSE.MessageTag
import com.nowid.sdk.cose.message.handlers.Sign1Handler
import com.nowid.sdk.exceptions.UnsupportedMessageTagException
import com.upokecenter.cbor.CBORObject
import java.security.PublicKey

/**
 * Routes COSE messages to the matching handler.
 */
internal object CoseDispatcher {
    private val handlers = arrayOf(Sign1Handler())

    /**
     * Decodes a CBOR-encoded COSE message and routes it to a supported handler.
     *
     * @param payload   the COSE message bytes
     * @param publicKey the key used for signature verification
     * @return a [Result] containing JSON output on success, or an error on failure
     */
    fun dispatch(payload: ByteArray, publicKey: PublicKey): Result<String> {
        return try {
            // Decode the payload into a CBOR object to inspect its structure
            val cbor = CBORObject.DecodeFromBytes(payload)

            // Extract the COSE message tag from the CBOR object
            val messageTag = MessageTag.FromInt(cbor.getMostInnerTag().ToInt32Unchecked())

            val handler = handlers.find { it.supports(messageTag) }
                ?: return Result.failure(UnsupportedMessageTagException(messageTag))
            handler.handle(payload, publicKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
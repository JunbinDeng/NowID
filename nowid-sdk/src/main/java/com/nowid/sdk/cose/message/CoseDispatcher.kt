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
     * @param payload   CBOR‐encoded COSE message
     * @param publicKey key for signature verification or decryption
     * @return a [Result] with the handler’s JSON string output or an error
     */
    fun dispatch(payload: ByteArray, publicKey: PublicKey): Result<String> {
        val cbor = CBORObject.DecodeFromBytes(payload)
        val messageTag = MessageTag.FromInt(cbor.getMostInnerTag().ToInt32Unchecked())
        val handler = handlers.find { it.supports(messageTag) }
            ?: return Result.failure(UnsupportedMessageTagException(messageTag))
        return handler.handle(payload, publicKey)
    }
}
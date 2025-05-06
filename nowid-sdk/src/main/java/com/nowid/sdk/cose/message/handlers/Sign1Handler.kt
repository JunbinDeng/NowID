package com.nowid.sdk.cose.message.handlers

import COSE.MessageTag
import COSE.Sign1Message
import com.nowid.sdk.cose.keys.PublicKeyToOneKeyFactory
import com.upokecenter.cbor.CBORObject
import java.security.PublicKey

/**
 * Verifies and parses COSE Sign1 messages.
 */
internal class Sign1Handler : CoseMessageHandler {
    override fun supports(messageTag: MessageTag): Boolean {
        return messageTag == MessageTag.Sign1
    }

    override fun handle(payload: ByteArray, publicKey: PublicKey): Result<String> {
        return try {
            // Decode and verify the COSE message
            val message = Sign1Message.DecodeFromBytes(payload, MessageTag.Sign1) as Sign1Message
            val oneKey = PublicKeyToOneKeyFactory.from(publicKey)
            if (!message.validate(oneKey)) return Result.failure(Exception("Invalid signature"))

            // Decode only the signed payload
            val cborMap = CBORObject.DecodeFromBytes(message.GetContent())

            val result = cborMap.ToJSONString()

            return Result.success(result)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
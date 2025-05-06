package com.nowid.sdk.cose.message.handlers

import COSE.AlgorithmID
import COSE.Attribute
import COSE.HeaderKeys
import COSE.MessageTag
import COSE.OneKey
import COSE.Sign1Message
import com.nowid.sdk.testutil.BaseUnitTest
import com.upokecenter.cbor.CBORObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sign1HandlerTest : BaseUnitTest() {
    private val handler = Sign1Handler()

    @Test
    fun `supports should return true for Sign1 message`() {
        assertTrue(handler.supports(MessageTag.Sign1))
    }

    @Test
    fun `should return success when message is valid`() {
        // Generate EC P-256 key pair
        val oneKey = OneKey.generateKey(AlgorithmID.ECDSA_256)
        val payload = """{ "id": 123, "status": "valid" }"""

        val message = Sign1Message().apply {
            addAttribute(
                HeaderKeys.Algorithm,
                AlgorithmID.ECDSA_256.AsCBOR(),
                Attribute.PROTECTED
            )
            SetContent(CBORObject.FromJSONString(payload).EncodeToBytes())
            sign(oneKey)
        }

        val result = handler.handle(message.EncodeToBytes(), oneKey.AsPublicKey())

        assertTrue(result.isSuccess)
        assertEquals(payload.replace(" ", ""), result.getOrNull()?.replace(" ", ""))
    }

    @Test
    fun `should return failure when message is invalid`() {
        // Generate mismatched EC P-256 key pairs
        val oneKey1 = OneKey.generateKey(AlgorithmID.ECDSA_256)
        val oneKey2 = OneKey.generateKey(AlgorithmID.ECDSA_256)
        val payload = """{ "id": 123, "status": "valid" }"""

        val message = Sign1Message().apply {
            addAttribute(HeaderKeys.Algorithm, AlgorithmID.ECDSA_256.AsCBOR(), Attribute.PROTECTED)
            SetContent(CBORObject.FromJSONString(payload).EncodeToBytes())
            sign(oneKey1)
        }

        val result = handler.handle(message.EncodeToBytes(), oneKey2.AsPublicKey())

        assertTrue(result.isFailure)
    }
}
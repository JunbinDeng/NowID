package com.nowid.sdk.cose.message

import COSE.AlgorithmID
import COSE.Attribute
import COSE.HeaderKeys
import COSE.MAC0Message
import COSE.OneKey
import COSE.Sign1Message
import com.nowid.sdk.exceptions.UnsupportedMessageTagException
import com.nowid.sdk.testutil.BaseUnitTest
import com.upokecenter.cbor.CBORException
import com.upokecenter.cbor.CBORObject
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator

class CoseDispatcherTest : BaseUnitTest() {
    @Test
    fun `should route to Sign1Handler and return success`() {
        // Generate EC P-256 key pair
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()

        val message = Sign1Message().apply {
            addAttribute(
                HeaderKeys.Algorithm,
                AlgorithmID.ECDSA_256.AsCBOR(),
                Attribute.PROTECTED
            )
            SetContent(CBORObject.FromObject("test").EncodeToBytes())
            sign(OneKey(keyPair.public, keyPair.private))
        }

        val result = CoseDispatcher.dispatch(message.EncodeToBytes(), keyPair.public)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when payload is not invalid`() {
        // Generate EC P-256 key pair
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()

        val mockPayload = byteArrayOf(0x01, 0x02)

        val result = CoseDispatcher.dispatch(mockPayload, keyPair.public)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CBORException)
    }

    @Test
    fun `should return failure when MessageTag is unsupported`() {
        // Build unsupported MAC0 COSE message
        val message = MAC0Message().apply {
            addAttribute(
                HeaderKeys.Algorithm,
                AlgorithmID.HMAC_SHA_256.AsCBOR(),
                Attribute.PROTECTED
            )
            SetContent(CBORObject.FromObject("test").EncodeToBytes())
            Create(ByteArray(32) { 0x01 })
        }

        val result = CoseDispatcher.dispatch(
            message.EncodeToBytes(),
            // Provide dummy public key (won’t be used)
            KeyPairGenerator.getInstance("EC").apply {
                initialize(256)
            }.generateKeyPair().public
        )

        assertTrue(result.exceptionOrNull() is UnsupportedMessageTagException)
    }
}
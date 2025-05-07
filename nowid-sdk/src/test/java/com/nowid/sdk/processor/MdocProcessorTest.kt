package com.nowid.sdk.processor

import COSE.AlgorithmID
import COSE.Attribute
import COSE.HeaderKeys
import COSE.OneKey
import COSE.Sign1Message
import com.nowid.sdk.testutil.BaseUnitTest
import com.nowid.sdk.transport.TransportStrategy
import com.upokecenter.cbor.CBORObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator

class MdocProcessorTest : BaseUnitTest() {
    @Test
    fun `should return success when payload is valid`() {
        // Generate EC P-256 key pair representing the holder's key
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()

        // Simulate mdoc issuance: encode a CBOR payload and sign it using Sign1Message
        val payloadCbor = CBORObject.FromJSONString("""{"name":"Jamie"}""")

        val sign1 = Sign1Message().apply {
            addAttribute(HeaderKeys.Algorithm, AlgorithmID.ECDSA_256.AsCBOR(), Attribute.PROTECTED)
            SetContent(payloadCbor.EncodeToBytes())
            sign(OneKey(keyPair.public, keyPair.private))
        }

        // Simulated NFC transport returning the signed COSE message
        val mockPayload = sign1.EncodeToBytes()

        val transport = object : TransportStrategy {
            override fun extractPayload() = Result.success(mockPayload)
        }

        // Verifier process: run the full mdoc verification pipeline
        val result = MdocProcessor.process(transport, keyPair.public)

        assertTrue(result.isSuccess)
        assertEquals("""{"name":"Jamie"}""", result.getOrNull())
    }

    @Test
    fun `should return failure when payload is invalid`() {
        val mockPayload = byteArrayOf(0x01, 0x02)

        val transport = object : TransportStrategy {
            override fun extractPayload() = Result.success(mockPayload)
        }

        val publicKey = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair().public

        val result = MdocProcessor.process(transport, publicKey)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should return failure when transport fails`() {
        val transport = object : TransportStrategy {
            override fun extractPayload(): Result<ByteArray> =
                Result.failure(Exception("Missing NFC"))
        }

        val publicKey = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair().public

        val result = MdocProcessor.process(transport, publicKey)

        assertTrue(result.isFailure)
        assertEquals("Missing NFC", result.exceptionOrNull()?.message)
    }
}
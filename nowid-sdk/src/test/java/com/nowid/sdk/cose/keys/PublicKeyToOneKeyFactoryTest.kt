package com.nowid.sdk.cose.keys

import COSE.AlgorithmID
import COSE.KeyKeys
import com.nowid.sdk.testutil.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey

class PublicKeyToOneKeyFactoryTest : BaseUnitTest() {
    @Test
    fun `should convert ECPublicKey to OneKey with valid fields`() {
        // Generate EC P-256 key pair
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()

        val ecPublicKey = keyPair.public as ECPublicKey

        // Convert to the public key to the COSE OneKey
        val oneKey = PublicKeyToOneKeyFactory.from(ecPublicKey)

        // Verify required key fields are correctly encoded
        assertEquals(AlgorithmID.ECDSA_256.AsCBOR(), oneKey.get(KeyKeys.Algorithm))
        assertEquals(KeyKeys.KeyType_EC2, oneKey.get(KeyKeys.KeyType))
        assertEquals(KeyKeys.EC2_P256, oneKey.get(KeyKeys.EC2_Curve))

        val xBytes = oneKey.get(KeyKeys.EC2_X).GetByteString()
        val yBytes = oneKey.get(KeyKeys.EC2_Y).GetByteString()

        // Ensure x and y are 32 bytes long
        assertEquals(32, xBytes.size)
        assertEquals(32, yBytes.size)
    }

    @Test
    fun `should delegate to EC2PublicKeyConverter for EC key`() {
        // Generate EC P-256 key pair
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()

        val oneKey = PublicKeyToOneKeyFactory.from(keyPair.public)
        assertNotNull(oneKey)
    }

    @Test
    fun `should throw for unsupported key types`() {
        // Generate RSA key pair
        val keyPair = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.generateKeyPair()

        val ex = assertThrows(IllegalArgumentException::class.java) {
            PublicKeyToOneKeyFactory.from(keyPair.public)
        }
        assertTrue(ex.message!!.contains("Unsupported"))
    }
}
package com.nowid.sdk.cose.keys.converters

import COSE.AlgorithmID
import COSE.KeyKeys
import com.nowid.sdk.testutil.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey

class EC2PublicKeyConverterTest : BaseUnitTest() {
    private val converter = EC2PublicKeyConverter()

    @Test
    fun `supports should return true for EC2PublicKey`() {
        // Generate EC P-256 key pair
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()

        assertTrue(converter.supports(keyPair.public))
    }

    @Test
    fun `convert should produce valid OneKey with 32-byte x and y`() {
        // Generate EC P-256 key pair
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()

        val ecPub = keyPair.public as ECPublicKey

        val oneKey = converter.convert(ecPub)

        assertEquals(AlgorithmID.ECDSA_256.AsCBOR(), oneKey.get(KeyKeys.Algorithm))
        assertEquals(KeyKeys.KeyType_EC2, oneKey.get(KeyKeys.KeyType))
        assertEquals(KeyKeys.EC2_P256, oneKey.get((KeyKeys.EC2_Curve)))

        val x = oneKey.get(KeyKeys.EC2_X).GetByteString()
        val y = oneKey.get(KeyKeys.EC2_Y).GetByteString()

        assertEquals(32, x.size)
        assertEquals(32, y.size)
    }
}
package com.nowid.sdk.cose.keys.converters

import COSE.AlgorithmID
import COSE.KeyKeys
import COSE.OneKey
import com.nowid.sdk.cose.keys.util.asUnsignedByteArray
import com.upokecenter.cbor.CBORObject
import java.security.PublicKey
import java.security.interfaces.ECPublicKey

/**
 * EC2PublicKeyConverter is responsible for determining if a given PublicKey is an EC (Elliptic Curve)
 * public key and converting it to an OneKey representation if it is.
 */
internal class EC2PublicKeyConverter : PublicKeysConverter {
    override fun supports(publicKey: PublicKey): Boolean {
        return publicKey is ECPublicKey
    }

    override fun convert(publicKey: PublicKey): OneKey {
        publicKey as ECPublicKey

        val keyMap = CBORObject.NewMap().apply {
            this[KeyKeys.KeyType.AsCBOR()] = KeyKeys.KeyType_EC2
            this[KeyKeys.Algorithm.AsCBOR()] = AlgorithmID.ECDSA_256.AsCBOR()
            this[KeyKeys.EC2_Curve.AsCBOR()] = KeyKeys.EC2_P256
            this[KeyKeys.EC2_X.AsCBOR()] =
                CBORObject.FromObject(publicKey.w.affineX.asUnsignedByteArray(32))
            this[KeyKeys.EC2_Y.AsCBOR()] =
                CBORObject.FromObject(publicKey.w.affineY.asUnsignedByteArray(32))
        }
        return OneKey(keyMap)
    }
}
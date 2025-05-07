package com.nowid.sdk.processor

import COSE.AlgorithmID
import COSE.Attribute
import COSE.HeaderKeys
import COSE.OneKey
import COSE.Sign1Message
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nowid.sdk.transport.NfcTransportStrategy
import com.upokecenter.cbor.CBORObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPairGenerator

@RunWith(AndroidJUnit4::class)
class MdocProcessorInstrumentedTest {
    @Test
    fun processValidSign1PayloadViaNfc() {
        // Generate key pair
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()

        // Create CBOR payload
        val json = """{"name":"Jamie"}"""
        val cbor = CBORObject.FromJSONString(json)

        // Sign with COSE
        val message = Sign1Message().apply {
            addAttribute(HeaderKeys.Algorithm, AlgorithmID.ECDSA_256.AsCBOR(), Attribute.PROTECTED)
            SetContent(cbor.EncodeToBytes())
            sign(OneKey(keyPair.public, keyPair.private))
        }

        // Wrap into NFC intent
        val record = NdefRecord.createMime("application/cose-sign1+cbor", message.EncodeToBytes())
        val ndefMessage = NdefMessage(arrayOf(record))
        val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(ndefMessage))
        }

        // Process
        val result = MdocProcessor.process(NfcTransportStrategy(intent), keyPair.public)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(json, result.getOrNull())
    }
}
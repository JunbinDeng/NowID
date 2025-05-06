package com.nowid.sdk.transport

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NfcTransportStrategyTest {
    @Test
    fun shouldExtractPayloadFromValidNdefIntent() {
        val payload = "test".toByteArray()
        val record = NdefRecord.createMime("text/plain", payload)
        val message = NdefMessage(arrayOf(record))

        val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(message))
        }

        val strategy = NfcTransportStrategy(intent)
        val result = strategy.extractPayload()

        assertTrue(result.isSuccess)
        assertEquals("test", result.getOrNull()?.toString(Charsets.UTF_8))
    }

    @Test
    fun shouldFailIfActionIsNotNdefDiscovered() {
        val intent = Intent(Intent.ACTION_VIEW)
        val strategy = NfcTransportStrategy(intent)
        val result = strategy.extractPayload()

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldFailIfNdefMessageIsMissing() {
        val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val strategy = NfcTransportStrategy(intent)
        val result = strategy.extractPayload()

        assertTrue(result.isFailure)
    }
}
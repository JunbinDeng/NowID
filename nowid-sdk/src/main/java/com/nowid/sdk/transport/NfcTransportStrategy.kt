package com.nowid.sdk.transport

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build

/**
 * Reads an NDEF payload from an NFC intent.
 *
 * @param intent the NFC [Intent] containing NDEF messages
 */
class NfcTransportStrategy(val intent: Intent) : TransportStrategy {
    override fun extractPayload(): Result<ByteArray> {
        if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED) {
            return Result.failure(IllegalArgumentException("Not an NFC action"))
        }

        val rawMessages = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES,
                    NdefMessage::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val ndefMessage = (rawMessages?.firstOrNull() as? NdefMessage)
            ?: return Result.failure(IllegalStateException("Missing NDEF message"))

        val payload = ndefMessage.records.firstOrNull()?.payload
            ?: return Result.failure(IllegalStateException("Missing NDEF record payload"))

        return Result.success(payload)
    }
}
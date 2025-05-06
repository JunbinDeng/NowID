package com.nowid.sdk

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import com.nowid.sdk.cose.message.CoseDispatcher
import java.security.PublicKey

class MdocReceiver {
    fun handleNfcIntent(
        intent: Intent,
        publicKey: PublicKey,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Check if the intent is for NFC
        if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED) {
            onError("Not NFC action")
            return
        }

        // Decode the NFC Data Exchange Format (NDEF) message
        val rawMessages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
        } else {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        }
        val ndefMessage = (rawMessages?.firstOrNull() as? NdefMessage) ?: return

        try {
            // Get payload from the first record in the first NDEF message
            val payload = ndefMessage.records.first().payload

            val result = CoseDispatcher.dispatch(payload, publicKey)

            onResult(result.getOrThrow())
        } catch (e: Exception) {
            onError("Parse error: ${e.message}")
        }
    }
}
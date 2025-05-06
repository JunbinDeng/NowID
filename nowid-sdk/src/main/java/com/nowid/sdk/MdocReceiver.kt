package com.nowid.sdk

import COSE.Sign1Message
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import com.nowid.sdk.cose.keys.PublicKeyToOneKeyFactory
import com.upokecenter.cbor.CBORObject
import java.security.PublicKey

class MdocReceiver {
    fun handleNfcIntent(
        intent: Intent,
        publicKey: PublicKey,
        onResult: (Map<String, Any>) -> Unit,
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

            // Decode the payload as a COSE Single Signer Data
            val sign1Message = Sign1Message.DecodeFromBytes(payload)
            if (sign1Message !is Sign1Message) {
                onError("Message is not a Sign1Message");
                return
            }

            // Verify the signature
            if (!sign1Message.validate(PublicKeyToOneKeyFactory.from(publicKey))) {
                onError("Signature verification failed")
                return
            }

            // Parse the CBOR payload
            val cborObject = CBORObject.DecodeFromBytes(payload)

            // Convert the CBOR payload to result map
            val result = mutableMapOf<String, Any>()
            for (key in cborObject.keys) {
                result[key.AsString()] = cborObject[key].ToObject(Any::class.java)
            }
            onResult(result.toMap())
        } catch (e: Exception) {
            onError("Parse error: ${e.message}")
        }
    }
}
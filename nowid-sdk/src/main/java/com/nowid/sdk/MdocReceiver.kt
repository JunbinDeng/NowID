package com.nowid.sdk

import android.content.Intent
import com.nowid.sdk.cose.message.CoseDispatcher
import com.nowid.sdk.transport.NfcTransportStrategy
import java.security.PublicKey

class MdocReceiver {
    fun handleNfcIntent(
        intent: Intent,
        publicKey: PublicKey,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val payload = NfcTransportStrategy(intent).extractPayload().getOrThrow()

            val result = CoseDispatcher.dispatch(payload, publicKey)

            onResult(result.getOrThrow())
        } catch (e: Exception) {
            onError("Parse error: ${e.message}")
        }
    }
}
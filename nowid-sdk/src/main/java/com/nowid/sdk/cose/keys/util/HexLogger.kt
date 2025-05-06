package com.nowid.sdk.cose.keys.util

import timber.log.Timber

/**
 * Logs a byte array in hex format with a given tag and message.
 *
 * @param tag The Timber log tag.
 * @param message The message prefix shown before the hex output.
 * @param bytes The ByteArray to be logged as hex.
 */
@OptIn(ExperimentalStdlibApi::class)
fun logHex(tag: String, message: String, bytes: ByteArray) {
    Timber.tag(tag).d("$message: %s", bytes.toHexString())
}
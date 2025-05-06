package com.nowid.sdk.cose.keys.util

import java.math.BigInteger

/**
 * Converts this [BigInteger] into an unsigned big-endian [ByteArray] of the specified [length],
 * padding with leading zeros if necessary.
 *
 * Reference: https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/util/BigIntegers.java
 *
 * @receiver    the [BigInteger] to convert
 * @param length the exact length of the resulting byte array
 * @return        a [ByteArray] of size [length] containing the unsigned big-endian representation
 *                of this value, left-padded with zeros
 * @throws IllegalArgumentException if the unsigned representation requires more than [length] bytes
 */
fun BigInteger.asUnsignedByteArray(length: Int): ByteArray {
    val bytes = this.toByteArray()
    if (bytes.size == length) {
        return bytes
    }

    val start = if (bytes[0].toInt() == 0 && bytes.size != 1) 1 else 0
    val count = bytes.size - start

    require(count <= length) { "standard length exceeded for value" }

    val tmp = ByteArray(length)
    System.arraycopy(bytes, start, tmp, tmp.size - count, count)
    return tmp
}
package com.nowid.sdk.cose.keys.util

import com.nowid.sdk.testutil.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class BigIntegersTest : BaseUnitTest() {
    @Test
    fun `should pad to correct length`() {
        val bi = BigInteger("1234567890", 10)
        val result = bi.asUnsignedByteArray(32)
        logHex("Test", "Unsigned bytes", result)

        assertEquals(32, result.size)
        assertEquals(0x49, result[28].toInt() and 0xFF)
        assertEquals(0x96, result[29].toInt() and 0xFF)
        assertEquals(0x02, result[30].toInt() and 0xFF)
        assertEquals(0xD2, result[31].toInt() and 0xFF)
    }

    @Test
    fun `should throw if byte length exceeds expected size`() {
        val bi = BigInteger(ByteArray(40) { 0x01 })
        val ex = assertThrows(IllegalArgumentException::class.java) { bi.asUnsignedByteArray(32) }
        assertTrue(ex.message!!.contains("exceeded"))
    }
}
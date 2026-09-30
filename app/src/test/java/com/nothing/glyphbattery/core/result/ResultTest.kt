package com.nothing.glyphbattery.core.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun `fold on Success invokes onSuccess callback`() {
        val result: Result<String, Throwable> = Result.Success("Nothing Phone (2)")

        val output = result.fold(
            onSuccess = { "Device: $it" },
            onFailure = { "Failed: ${it.message}" }
        )

        assertEquals("Device: Nothing Phone (2)", output)
    }

    @Test
    fun `fold on Failure invokes onFailure callback`() {
        val exception = IllegalStateException("Glyph service unavailable")
        val result: Result<String, Throwable> = Result.Failure(exception)

        val output = result.fold(
            onSuccess = { "Device: $it" },
            onFailure = { "Failed: ${it.message}" }
        )

        assertEquals("Failed: Glyph service unavailable", output)
    }

    @Test
    fun `getOrNull returns value on Success and null on Failure`() {
        val success: Result<Int, Throwable> = Result.Success(100)
        val failure: Result<Int, Throwable> = Result.Failure(RuntimeException("Error"))

        assertEquals(100, success.getOrNull())
        assertNull(failure.getOrNull())
    }
}

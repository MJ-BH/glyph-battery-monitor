package com.nothing.glyphbattery.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MorseMessageTest {

    @Test
    fun `encodeToMorse encodes SOS accurately`() {
        val result = MorseMessage.encodeToMorse("SOS")
        assertEquals("... --- ...", result)
    }

    @Test
    fun `toSignalSequence produces correct sequence for SOS`() {
        val signals = MorseMessage.toSignalSequence("SOS")
        assertTrue(signals.isNotEmpty())

        // First 3 dots should be LightOn(150)
        val firstSignal = signals[0] as MorseMessage.MorseSignal.LightOn
        assertEquals(MorseMessage.DOT_DURATION_MS, firstSignal.durationMs)
    }
}

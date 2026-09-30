package com.nothing.glyphbattery.domain.model

object MorseMessage {

    const val DOT_DURATION_MS = 150L
    const val DASH_DURATION_MS = 450L
    const val ELEMENT_GAP_MS = 150L
    const val LETTER_GAP_MS = 350L
    const val WORD_GAP_MS = 800L

    private val MORSE_MAP = mapOf(
        'A' to ".-",    'B' to "-...",  'C' to "-.-.",  'D' to "-..",
        'E' to ".",     'F' to "..-.",  'G' to "--.",   'H' to "....",
        'I' to "..",    'J' to ".---",  'K' to "-.-",   'L' to ".-..",
        'M' to "--",    'N' to "-.",    'O' to "---",   'P' to ".--.",
        'Q' to "--.-",  'R' to ".-.",   'S' to "...",   'T' to "-",
        'U' to "..-",   'V' to "...-",  'W' to ".--",   'X' to "-..-",
        'Y' to "-.--",  'Z' to "--..",
        '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..",
        '9' to "----.", '0' to "-----", ' ' to "/"
    )

    fun encodeToMorse(text: String): String {
        return text.uppercase()
            .map { MORSE_MAP[it] ?: "" }
            .filter { it.isNotEmpty() }
            .joinToString(" ")
    }

    sealed interface MorseSignal {
        data class LightOn(val durationMs: Long) : MorseSignal
        data class LightOff(val durationMs: Long) : MorseSignal
    }

    fun toSignalSequence(text: String): List<MorseSignal> {
        val signals = mutableListOf<MorseSignal>()
        val upper = text.uppercase()

        for (charIndex in upper.indices) {
            val char = upper[charIndex]
            if (char == ' ') {
                signals.add(MorseSignal.LightOff(WORD_GAP_MS))
                continue
            }
            val morse = MORSE_MAP[char] ?: continue
            for (elementIndex in morse.indices) {
                when (morse[elementIndex]) {
                    '.' -> signals.add(MorseSignal.LightOn(DOT_DURATION_MS))
                    '-' -> signals.add(MorseSignal.LightOn(DASH_DURATION_MS))
                }
                if (elementIndex < morse.length - 1) {
                    signals.add(MorseSignal.LightOff(ELEMENT_GAP_MS))
                }
            }
            if (charIndex < upper.length - 1 && upper[charIndex + 1] != ' ') {
                signals.add(MorseSignal.LightOff(LETTER_GAP_MS))
            }
        }
        return signals
    }
}

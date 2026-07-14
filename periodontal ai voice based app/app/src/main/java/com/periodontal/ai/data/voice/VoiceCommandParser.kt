package com.periodontal.ai.data.voice

import java.util.Locale

sealed class VoiceCommand {
    data class SelectTooth(val toothNumber: Int) : VoiceCommand()
    data class InputDepths(val depths: List<Int>) : VoiceCommand()
    data class InputSiteDepth(val site: ProbingSite, val depth: Int) : VoiceCommand()
    data class ToggleBleeding(val site: ProbingSite, val bleeding: Boolean) : VoiceCommand()
    object NextTooth : VoiceCommand()
    object PrevTooth : VoiceCommand()
    object ClearRecord : VoiceCommand()
    data class Unknown(val rawText: String) : VoiceCommand()
}

enum class ProbingSite {
    DISTAL_FACIAL, FACIAL, MESIAL_FACIAL,
    DISTAL_LINGUAL, LINGUAL, MESIAL_LINGUAL
}

object VoiceCommandParser {

    fun parse(text: String): VoiceCommand {
        val cleanText = text.lowercase(Locale.ROOT).trim()

        // 1. Check for Tooth Selection (e.g. "tooth 12", "select tooth 5", "tooth number 20")
        val toothRegex = Regex("(?:tooth|number|select)\\s*(\\d+)")
        toothRegex.find(cleanText)?.let { match ->
            val num = match.groupValues[1].toIntOrNull()
            if (num in 1..32) {
                return VoiceCommand.SelectTooth(num!!)
            }
        }

        // 2. Check for navigation commands
        if (cleanText.contains("next") || cleanText.contains("forward")) {
            return VoiceCommand.NextTooth
        }
        if (cleanText.contains("back") || cleanText.contains("previous") || cleanText.contains("prev")) {
            return VoiceCommand.PrevTooth
        }
        if (cleanText.contains("clear") || cleanText.contains("reset")) {
            return VoiceCommand.ClearRecord
        }

        // 3. Check for specific site updates (e.g. "distal facial 4", "facial 3", "mesial 5")
        val site = determineSite(cleanText)
        if (site != null) {
            val depthRegex = Regex("(\\d+)")
            depthRegex.findAll(cleanText).lastOrNull()?.let { match ->
                val depth = match.groupValues[1].toIntOrNull()
                if (depth != null && depth in 1..15) {
                    // Check for bleeding command combined (e.g., "facial 4 bleeding")
                    if (cleanText.contains("bleed") || cleanText.contains("blood") || cleanText.contains("yes")) {
                        // We will return site depth and can toggle bleeding separately, or combine it
                    }
                    return VoiceCommand.InputSiteDepth(site, depth)
                }
            }

            if (cleanText.contains("bleed") || cleanText.contains("blood") || cleanText.contains("yes")) {
                return VoiceCommand.ToggleBleeding(site, true)
            } else if (cleanText.contains("no bleed") || cleanText.contains("dry") || cleanText.contains("no")) {
                return VoiceCommand.ToggleBleeding(site, false)
            }
        }

        // 4. Check for sequential numbers input (e.g. "3 4 3", "three four three", "depths 2 3 2")
        // Extract all numeric digits
        val numbers = mutableListOf<Int>()
        val words = cleanText.split("\\s+".toRegex())
        words.forEach { word ->
            val num = word.toIntOrNull()
            if (num != null && num in 1..15) {
                numbers.add(num)
            } else {
                // Parse word equivalents
                val wordNum = when (word) {
                    "one" -> 1
                    "two" -> 2
                    "three", "free" -> 3
                    "four", "fore" -> 4
                    "five" -> 5
                    "six" -> 6
                    "seven" -> 7
                    "eight" -> 8
                    "nine" -> 9
                    "ten" -> 10
                    else -> null
                }
                if (wordNum != null) numbers.add(wordNum)
            }
        }

        // If we found exactly 3 depths (e.g. "3 4 3"), input them sequentially
        if (numbers.size == 3) {
            return VoiceCommand.InputDepths(numbers)
        } else if (numbers.size == 6) {
            return VoiceCommand.InputDepths(numbers)
        }

        // If we found a single standalone number between 1 and 15, treat it as sequential input
        if (numbers.size == 1 && numbers[0] in 1..15 && !cleanText.contains("tooth")) {
            return VoiceCommand.InputDepths(listOf(numbers[0]))
        }

        return VoiceCommand.Unknown(text)
    }

    private fun determineSite(text: String): ProbingSite? {
        return when {
            text.contains("distal facial") || text.contains("df") || text.contains("distal buccal") || text.contains("db") -> ProbingSite.DISTAL_FACIAL
            text.contains("mesial facial") || text.contains("mf") || text.contains("mesial buccal") || text.contains("mb") -> ProbingSite.MESIAL_FACIAL
            text.contains("facial") || text.contains("buccal") -> ProbingSite.FACIAL
            text.contains("distal lingual") || text.contains("dl") || text.contains("distal palatal") || text.contains("dp") -> ProbingSite.DISTAL_LINGUAL
            text.contains("mesial lingual") || text.contains("ml") || text.contains("mesial palatal") || text.contains("mp") -> ProbingSite.MESIAL_LINGUAL
            text.contains("lingual") || text.contains("palatal") -> ProbingSite.LINGUAL
            else -> null
        }
    }
}

package com.example.pocketdepthai.util

import android.util.Log

object Parser {
    private val numberWords = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
        "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13,
        "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
        "eighteen" to 18, "nineteen" to 19, "twenty" to 20, "thirty" to 30,
        "forty" to 40, "fifty" to 50
    )

    /**
     * Extracts tooth number and probing values from speech text.
     * Supports digits, word numbers (e.g. "eleven"), compound phrases, and non-numeric punctuation.
     * Example input: "Tooth eleven: three, four, five"
     * Result: Pair(11, listOf(3, 4, 5))
     */
    fun parseSpeech(input: String): Pair<Int, List<Int>>? {
        return try {
            Log.d("Parser", "Original voice input: $input")
            
            // Clean up text: convert to lowercase and replace punctuation with spaces
            val cleaned = input.lowercase()
                .replace(Regex("[\\-,:;._]"), " ")
                .replace("tooth number", "tooth")
                .replace("tooth no", "tooth")
            
            // Reconstruct string replacing word numbers with their digit strings
            val words = cleaned.split(Regex("\\s+")).filter { it.isNotEmpty() }
            val reconstructed = mutableListOf<String>()
            
            var i = 0
            while (i < words.size) {
                val currentWord = words[i]
                if (numberWords.containsKey(currentWord)) {
                    var valNum = numberWords[currentWord]!!
                    
                    // Support compound numbers like "twenty one"
                    if (i + 1 < words.size && (currentWord == "twenty" || currentWord == "thirty")) {
                        val nextWord = words[i + 1]
                        if (numberWords.containsKey(nextWord) && numberWords[nextWord]!! < 10) {
                            valNum += numberWords[nextWord]!!
                            i++ // skip next word
                        }
                    }
                    reconstructed.add(valNum.toString())
                } else {
                    reconstructed.add(currentWord)
                }
                i++
            }
            
            val processText = reconstructed.joinToString(" ")
            Log.d("Parser", "Cleaned speech text: $processText")
            
            // Attempt to match "tooth" followed by tooth number
            val regex = Regex("""(?i)tooth\s*(\d+)(.*)""")
            val match = regex.find(processText)
            
            if (match != null) {
                val toothNumber = match.groupValues[1].toInt()
                val restOfText = match.groupValues[2]
                
                // Extract all numbers from the rest of the text
                val digitsRegex = Regex("""\b\d+\b""")
                val values = digitsRegex.findAll(restOfText)
                    .map { it.value.toInt() }
                    .filter { it in 0..10 } // typical periodontal pocket depths are 0..10 mm
                    .toList()
                
                if (toothNumber in 1..32 && values.isNotEmpty()) {
                    Pair(toothNumber, values)
                } else {
                    null
                }
            } else {
                // Safe Fallback: if the word "tooth" was not recognized, check if we have a sequence of numbers.
                // The first number represents the tooth, subsequent numbers represent probing depths.
                // E.g., "11 3 4 5" or "eleven three four five" (which preprocessed to "11 3 4 5")
                val digitsRegex = Regex("""\b\d+\b""")
                val allNumbers = digitsRegex.findAll(processText)
                    .map { it.value.toInt() }
                    .toList()
                
                if (allNumbers.size >= 2) {
                    val toothNumber = allNumbers[0]
                    val values = allNumbers.subList(1, allNumbers.size).filter { it in 0..10 }
                    
                    if (toothNumber in 1..32 && values.isNotEmpty()) {
                        Pair(toothNumber, values)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("Parser", "Error parsing speech: ${e.message}")
            null
        }
    }

    fun classifyDisease(values: List<Int>): String {
        if (values.isEmpty()) return "Unknown"
        val maxVal = values.maxOrNull() ?: 0
        return when {
            maxVal <= 3 -> "Healthy"
            maxVal <= 5 -> "Gingivitis"
            else -> "Periodontitis"
        }
    }
}

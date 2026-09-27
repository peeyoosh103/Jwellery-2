package com.example.voice

import java.util.Locale

object VoiceHindiNumberParser {

    private val singleWordsMap = mapOf(
        "zero" to 0.0, "shunya" to 0.0, "sunya" to 0.0,
        "half" to 0.5, "aadha" to 0.5, "adha" to 0.5, "aadhe" to 0.5, "adhe" to 0.5,
        "pauna" to 0.75, "paun" to 0.75, "paune" to 0.75,
        "sawa" to 1.25,
        "dedh" to 1.5, "dhed" to 1.5, "derh" to 1.5,
        "dhai" to 2.5, "adhai" to 2.5,
        "ek" to 1.0, "one" to 1.0,
        "do" to 2.0, "two" to 2.0,
        "teen" to 3.0, "tin" to 3.0, "three" to 3.0,
        "char" to 4.0, "chaar" to 4.0, "four" to 4.0,
        "paanch" to 5.0, "panch" to 5.0, "five" to 5.0,
        "chhah" to 6.0, "chhe" to 6.0, "che" to 6.0, "six" to 6.0,
        "saat" to 7.0, "sat" to 7.0, "seven" to 7.0,
        "aath" to 8.0, "ath" to 8.0, "eight" to 8.0,
        "nau" to 9.0, "no" to 9.0, "nine" to 9.0,
        "dus" to 10.0, "das" to 10.0, "ten" to 10.0,
        "gyarah" to 11.0, "gyara" to 11.0, "eleven" to 11.0,
        "barah" to 12.0, "bara" to 12.0, "twelve" to 12.0,
        "terah" to 13.0, "tera" to 13.0, "thirteen" to 13.0,
        "chaudah" to 14.0, "chauda" to 14.0, "fourteen" to 14.0,
        "pandrah" to 15.0, "pandra" to 15.0, "fifteen" to 15.0,
        "solah" to 16.0, "sola" to 16.0, "sixteen" to 16.0,
        "satrah" to 17.0, "satra" to 17.0, "seventeen" to 17.0,
        "atharah" to 18.0, "athara" to 18.0, "eighteen" to 18.0,
        "unnis" to 19.0, "nineteen" to 19.0,
        "bees" to 20.0, "bis" to 20.0, "twenty" to 20.0,
        "ikkis" to 21.0, "baais" to 22.0, "teis" to 23.0, "chaubis" to 24.0, "pacchis" to 25.0, "pachis" to 25.0,
        "chhabis" to 26.0, "sattais" to 27.0, "atthais" to 28.0, "unattis" to 29.0, "untis" to 29.0,
        "tees" to 30.0, "tis" to 30.0, "thirty" to 30.0,
        "ekattis" to 31.0, "battis" to 32.0, "tentees" to 33.0, "chauntis" to 34.0, "paintis" to 35.0,
        "chhattis" to 36.0, "saintis" to 37.0, "adhtis" to 38.0, "untalis" to 39.0,
        "chalis" to 40.0, "forty" to 40.0,
        "iktalis" to 41.0, "bayalis" to 42.0, "tentalis" to 43.0, "chaualis" to 44.0, "paintalis" to 45.0,
        "chhiyalis" to 46.0, "saintalis" to 47.0, "adhtalis" to 48.0, "unchas" to 49.0,
        "pachas" to 50.0, "pachaas" to 50.0, "fifty" to 50.0,
        "ikkyavan" to 51.0, "bavan" to 52.0, "tirepan" to 53.0, "chauvan" to 54.0, "pachpan" to 55.0,
        "chhappan" to 56.0, "sattavan" to 57.0, "atthavan" to 58.0, "unsath" to 59.0,
        "saath" to 60.0, "sath" to 60.0, "sixty" to 60.0,
        "iksath" to 61.0, "baasath" to 62.0, "tirsath" to 63.0, "chaunsath" to 64.0, "painsath" to 65.0,
        "chhiyasath" to 66.0, "sadsath" to 67.0, "adhsath" to 68.0, "unhattar" to 69.0,
        "sattar" to 70.0, "seventy" to 70.0,
        "ikhattar" to 71.0, "bahattar" to 72.0, "tihattar" to 73.0, "chauhattar" to 74.0, "pachhattar" to 75.0,
        "chhihattar" to 76.0, "sathattar" to 77.0, "athhattar" to 78.0, "unasi" to 79.0,
        "assi" to 80.0, "eighty" to 80.0,
        "ikyasi" to 81.0, "bayasi" to 82.0, "tirasi" to 83.0, "chaurasi" to 84.0, "pachasi" to 85.0,
        "chhiyasi" to 86.0, "sattasi" to 87.0, "atthasi" to 88.0, "navasi" to 89.0, "nauasi" to 89.0,
        "nabbe" to 90.0, "nabbe" to 90.0, "ninety" to 90.0,
        "ikyanve" to 91.0, "baanve" to 92.0, "tiranve" to 93.0, "chauranve" to 94.0, "pichanve" to 95.0, "pichaanve" to 95.0,
        "chhiyanve" to 96.0, "santanve" to 97.0, "anthanve" to 98.0, "ninyanve" to 99.0,
        "sau" to 100.0, "so" to 100.0, "hundred" to 100.0
    )

    /**
     * Extracts a numeric value from a string snippet (e.g. "1 lakh 10 hazar", "5.25", "dedh lakh", "95k", "ek lakh").
     */
    fun parseNumber(text: String): Double? {
        val cleaned = text.lowercase(Locale.ROOT)
            .replace(",", "")
            .replace("₹", "")
            .replace("rs.", "")
            .replace("rs", "")
            .trim()

        if (cleaned.isEmpty()) return null

        // 1. Direct numeric match
        cleaned.toDoubleOrNull()?.let { return it }

        // 2. Format with 'k' or 'K' e.g. "95k", "100k"
        val kMatch = Regex("""^(\d+(?:\.\d+)?)\s*k$""").find(cleaned)
        if (kMatch != null) {
            val num = kMatch.groupValues[1].toDoubleOrNull()
            if (num != null) return num * 1000.0
        }

        // 3. Spoken Indian composite expressions (e.g., "1 lakh 10 hazar", "ek lakh bees hazar", "dhai lakh", "dedh lakh")
        return parseIndianSpokenNumber(cleaned)
    }

    /**
     * Parses composite Hindi/English number phrases.
     */
    private fun parseIndianSpokenNumber(text: String): Double? {
        val tokens = text.split(Regex("""\s+""")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return null

        var total = 0.0
        var currentChunk = 0.0
        var hasMatchedAny = false

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]

            // Handle prefixes: "sadhe" (e.g. sadhe teen = 3.5, sadhe char lakh = 450000)
            if (token == "sadhe" || token == "sade") {
                if (i + 1 < tokens.size) {
                    val nextVal = parseTokenValue(tokens[i + 1])
                    if (nextVal != null) {
                        currentChunk += (nextVal + 0.5)
                        hasMatchedAny = true
                        i += 2
                        continue
                    }
                }
            }

            // Handle prefixes: "sawa" (e.g. sawa do = 2.25)
            if (token == "sawa") {
                if (i + 1 < tokens.size) {
                    val nextVal = parseTokenValue(tokens[i + 1])
                    if (nextVal != null) {
                        currentChunk += (nextVal + 0.25)
                        hasMatchedAny = true
                        i += 2
                        continue
                    }
                }
                currentChunk += 1.25
                hasMatchedAny = true
                i++
                continue
            }

            // Handle prefixes: "paune" (e.g. paune do = 1.75, paune do lakh = 175000)
            if (token == "paune" || token == "paun") {
                if (i + 1 < tokens.size) {
                    val nextVal = parseTokenValue(tokens[i + 1])
                    if (nextVal != null) {
                        currentChunk += (nextVal - 0.25)
                        hasMatchedAny = true
                        i += 2
                        continue
                    }
                }
                currentChunk += 0.75
                hasMatchedAny = true
                i++
                continue
            }

            // Multipliers: Lakh / Lac
            if (token == "lakh" || token == "lac" || token == "lakhs" || token == "laakh") {
                val multiplier = if (currentChunk > 0) currentChunk else 1.0
                total += multiplier * 100_000.0
                currentChunk = 0.0
                hasMatchedAny = true
                i++
                continue
            }

            // Multipliers: Crore / Karod
            if (token == "crore" || token == "karod" || token == "crores" || token == "koti") {
                val multiplier = if (currentChunk > 0) currentChunk else 1.0
                total += multiplier * 10_000_000.0
                currentChunk = 0.0
                hasMatchedAny = true
                i++
                continue
            }

            // Multipliers: Hazar / Thousand / Hazaar / K
            if (token == "hazar" || token == "hazaar" || token == "thousand" || token == "hazaron" || token == "k") {
                val multiplier = if (currentChunk > 0) currentChunk else 1.0
                total += multiplier * 1_000.0
                currentChunk = 0.0
                hasMatchedAny = true
                i++
                continue
            }

            // Multipliers: Sau / Hundred
            if (token == "sau" || token == "hundred" || token == "so") {
                val multiplier = if (currentChunk > 0) currentChunk else 1.0
                total += multiplier * 100.0
                currentChunk = 0.0
                hasMatchedAny = true
                i++
                continue
            }

            // Normal token
            val valOfToken = parseTokenValue(token)
            if (valOfToken != null) {
                currentChunk += valOfToken
                hasMatchedAny = true
            }

            i++
        }

        total += currentChunk
        return if (hasMatchedAny) total else null
    }

    private fun parseTokenValue(token: String): Double? {
        // Direct number
        token.toDoubleOrNull()?.let { return it }

        // Mapping dictionary
        return singleWordsMap[token.lowercase(Locale.ROOT)]
    }
}

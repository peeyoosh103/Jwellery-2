package com.example.voice

import com.example.models.GoldPurity
import com.example.models.MetalType
import com.example.models.SilverPurity
import java.util.Locale

object VoiceCommandParser {

    /**
     * Parses a spoken sentence into structured parameters.
     * [currentMetalType]: GOLD or SILVER context of the current screen.
     */
    fun parse(rawText: String, currentMetalType: MetalType): ParsedVoiceData {
        val text = rawText.lowercase(Locale.ROOT)
            .replace("।", " ")
            .replace(",", " ")
            .replace("-", " ")
            .replace("  ", " ")
            .trim()

        if (text.isBlank()) {
            return ParsedVoiceData(rawSpokenText = rawText)
        }

        // 1. Check for Reset / Clear Commands
        if (isResetCommand(text)) {
            return ParsedVoiceData(
                metalType = currentMetalType,
                isResetTriggered = true,
                rawSpokenText = rawText,
                feedbackMessage = "Calculator reset ho gaya",
                spokenConfirmation = "Calculator reset kar diya gaya hai"
            )
        }

        // 2. Check for Calculate Commands (e.g. "calculate karo", "hisab karo", "result batao")
        val isCalculateOnly = isCalculateCommand(text)

        var grams: Double? = null
        var milligrams: Double? = null
        var marketRate: Double? = null
        var makingCharge: Double? = null
        var wastage: Double? = null
        var gst: Double? = null
        var goldPurity: GoldPurity? = null
        var silverPurity: SilverPurity? = null

        // 3. Extract Weight (grams, milligrams, tola, kg)
        val weightParsed = extractWeight(text)
        if (weightParsed != null) {
            grams = weightParsed.first
            milligrams = weightParsed.second
        }

        // 4. Extract Market Rate
        val rateParsed = extractRate(text, currentMetalType)
        if (rateParsed != null) {
            marketRate = rateParsed
        }

        // 5. Extract Making Charge
        val makingParsed = extractMakingCharge(text)
        if (makingParsed != null) {
            makingCharge = makingParsed
        }

        // 6. Extract Wastage
        val wastageParsed = extractWastage(text)
        if (wastageParsed != null) {
            wastage = wastageParsed
        }

        // 7. Extract GST
        val gstParsed = extractGst(text)
        if (gstParsed != null) {
            gst = gstParsed
        }

        // 8. Extract Purity (OPTIONAL - only if user explicitly mentioned it)
        if (currentMetalType == MetalType.GOLD) {
            goldPurity = extractGoldPurity(text)
        } else {
            silverPurity = extractSilverPurity(text)
        }

        // Determine if calculate was requested in combination with values
        val hasAnyValue = grams != null || milligrams != null || marketRate != null ||
                makingCharge != null || wastage != null || gst != null || goldPurity != null || silverPurity != null

        val triggersCalculate = isCalculateOnly || containsCalculateTrigger(text)

        // Generate helpful feedback summary
        if (!hasAnyValue && !triggersCalculate) {
            // Unclear command
            return ParsedVoiceData(
                metalType = currentMetalType,
                rawSpokenText = rawText,
                clarificationNeeded = true,
                clarificationPrompt = "Command samajh nahi aayi. Udaharan: '5 gram gold, rate 1 lakh' ya 'Calculate karo'",
                feedbackMessage = "Udaharan: '5 gram सोना, rate 1 लाख' ya 'Hisab karo'"
            )
        }

        val feedbackBuilder = StringBuilder()
        if (grams != null || milligrams != null) {
            val g = grams ?: 0.0
            val mg = milligrams ?: 0.0
            val totalG = g + (mg / 1000.0)
            feedbackBuilder.append("Weight: ${String.format(Locale.ENGLISH, "%.3f", totalG)}g ")
        }
        if (marketRate != null) {
            feedbackBuilder.append("Rate: ₹${marketRate.toLong()} ")
        }
        if (makingCharge != null) {
            feedbackBuilder.append("Making: $makingCharge% ")
        }
        if (wastage != null) {
            feedbackBuilder.append("Wastage: $wastage% ")
        }
        if (gst != null) {
            feedbackBuilder.append("GST: $gst% ")
        }
        if (goldPurity != null) {
            feedbackBuilder.append("Purity: ${goldPurity.label} ")
        }
        if (silverPurity != null) {
            feedbackBuilder.append("Purity: ${silverPurity.label} ")
        }
        if (triggersCalculate) {
            feedbackBuilder.append("• Calculate")
        }

        val spokenConfirmation = if (triggersCalculate && !hasAnyValue) {
            "Hisab calculate kiya ja raha hai"
        } else {
            "Voice input update ho gaya"
        }

        return ParsedVoiceData(
            metalType = currentMetalType,
            grams = grams,
            milligrams = milligrams,
            marketRate = marketRate,
            makingChargePercent = makingCharge,
            wastagePercent = wastage,
            gstPercent = gst,
            goldPurity = goldPurity, // Remains null if not mentioned (OPTIONAL)
            silverPurity = silverPurity, // Remains null if not mentioned (OPTIONAL)
            isCalculateTriggered = triggersCalculate,
            rawSpokenText = rawText,
            feedbackMessage = feedbackBuilder.toString().trim(),
            spokenConfirmation = spokenConfirmation
        )
    }

    private fun isResetCommand(text: String): Boolean {
        val keywords = listOf(
            "reset", "clear", "clean", "sab clear", "clear all",
            "reset karo", "clear karo", "sab clear karo", "dobara start karo",
            "naya hisab", "shuru se", "restart", "hata do sab"
        )
        return keywords.any { text.contains(it) }
    }

    private fun isCalculateCommand(text: String): Boolean {
        val exactMatches = listOf(
            "calculate", "calculate karo", "hisab", "hisab karo", "hisab lagao",
            "hisab nikalo", "result", "result batao", "result nikalo", "total",
            "total batao", "kitna hua", "shuru karo", "bhav nikalo", "nikalo",
            "calculate kar do", "hisab kar do"
        )
        return exactMatches.any { text == it || text.startsWith("$it ") || text.endsWith(" $it") }
    }

    private fun containsCalculateTrigger(text: String): Boolean {
        val patterns = listOf(
            "calculate karo", "calculate kar do", "calculate", "hisab karo",
            "hisab nikalo", "hisab lagao", "result batao", "result nikalo",
            "total batao", "kitna hua", "total kitna", "bhav nikalo"
        )
        return patterns.any { text.contains(it) }
    }

    /**
     * Extracts weight: Grams and Milligrams.
     */
    private fun extractWeight(text: String): Pair<Double, Double>? {
        // Pattern 1: Dual weight: "5 gram 250 milligram" / "5g 250mg" / "5 gram 500 mg"
        val dualRegex = Regex("""(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:gram|grm|gm|g|ग्राम)\s*(?:aur|and|,)?\s*(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:milligram|milli|mili|mg|मिलीग्राम)""")
        val dualMatch = dualRegex.find(text)
        if (dualMatch != null) {
            val gVal = VoiceHindiNumberParser.parseNumber(dualMatch.groupValues[1]) ?: 0.0
            val mgVal = VoiceHindiNumberParser.parseNumber(dualMatch.groupValues[2]) ?: 0.0
            return Pair(gVal, mgVal)
        }

        // Pattern 2: Single Grams e.g. "5 gram", "5.25 g", "weight 5 gram", "vazan 7 gram", "5 gm", "5g sona"
        val gramRegex = Regex("""(?:weight|vazan|wajan|vajan)?\s*(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:gram|grm|gm|g|ग्राम)(?!\s*(?:ka|per|rate))""")
        val gramMatch = gramRegex.find(text)
        if (gramMatch != null) {
            val rawNum = gramMatch.groupValues[1].trim()
            val parsed = VoiceHindiNumberParser.parseNumber(rawNum)
            if (parsed != null && parsed > 0) {
                val fullGrams = parsed.toInt().toDouble()
                val decimalMg = ((parsed - fullGrams) * 1000.0)
                return Pair(fullGrams, decimalMg)
            }
        }

        // Pattern 3: Tola e.g. "1 tola", "2 tola" (1 tola = 10 grams in standard Indian jewellery market)
        val tolaRegex = Regex("""(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:tola|tole|तोला)""")
        val tolaMatch = tolaRegex.find(text)
        if (tolaMatch != null) {
            val tolaNum = VoiceHindiNumberParser.parseNumber(tolaMatch.groupValues[1])
            if (tolaNum != null) {
                return Pair(tolaNum * 10.0, 0.0)
            }
        }

        // Pattern 4: Milligrams only e.g. "500 milligram", "750 mg"
        val mgRegex = Regex("""(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:milligram|milli|mili|mg|मिलीग्राम)""")
        val mgMatch = mgRegex.find(text)
        if (mgMatch != null) {
            val mgNum = VoiceHindiNumberParser.parseNumber(mgMatch.groupValues[1])
            if (mgNum != null) {
                return Pair(0.0, mgNum)
            }
        }

        // Pattern 5: Kilograms e.g. "1 kg", "2 kilo"
        val kgRegex = Regex("""(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:kg|kilo|kilogram|किलो)""")
        val kgMatch = kgRegex.find(text)
        if (kgMatch != null) {
            val kgNum = VoiceHindiNumberParser.parseNumber(kgMatch.groupValues[1])
            if (kgNum != null) {
                return Pair(kgNum * 1000.0, 0.0)
            }
        }

        // Pattern 6: Direct "weight 5" or "weight 5.5"
        val directWeightRegex = Regex("""(?:weight|vazan|wajan|vajan)\s*(?:hai|is|=)?\s*(\d+(?:\.\d+)?)""")
        val directMatch = directWeightRegex.find(text)
        if (directMatch != null) {
            val parsed = directMatch.groupValues[1].toDoubleOrNull()
            if (parsed != null) {
                val fullGrams = parsed.toInt().toDouble()
                val decimalMg = ((parsed - fullGrams) * 1000.0)
                return Pair(fullGrams, decimalMg)
            }
        }

        return null
    }

    /**
     * Extracts market rate.
     * Note:
     * - Gold market rate is quoted per 10g (e.g. 100000).
     * - Silver market rate is quoted per 1kg (e.g. 120000).
     * If user explicitly says "rate 10 hazar per gram" for gold, we multiply by 10 to get 10g rate.
     */
    private fun extractRate(text: String, metalType: MetalType): Double? {
        // Pattern 1: "rate 1 lakh 10 gram ka hai", "rate 95 hazar 10g ka", "rate 1 lakh per 10g"
        val per10gRegex = Regex("""(?:rate|bhav|daam|kimat|rate hai)\s*(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:per 10\s*g|10\s*gram ka|10\s*gram|10g)""")
        val per10gMatch = per10gRegex.find(text)
        if (per10gMatch != null) {
            val parsed = VoiceHindiNumberParser.parseNumber(per10gMatch.groupValues[1])
            if (parsed != null && parsed > 0) return parsed
        }

        // Pattern 2: "rate 10 hazar per gram" or "rate 10000 per g"
        val perGramRegex = Regex("""(?:rate|bhav|daam|kimat)\s*(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:per gram|per g|1 gram ka|gram ka)""")
        val perGramMatch = perGramRegex.find(text)
        if (perGramMatch != null) {
            val parsed = VoiceHindiNumberParser.parseNumber(perGramMatch.groupValues[1])
            if (parsed != null && parsed > 0) {
                return if (metalType == MetalType.GOLD) parsed * 10.0 else parsed * 1000.0
            }
        }

        // Pattern 3: "rate 1 lakh", "bhav 95000", "rate 1 lakh 10 hazar hai", "rate 120000 per kg"
        val generalRateRegex = Regex("""(?:rate|bhav|daam|kimat|price)\s*(?:hai|is|=)?\s*(\d+(?:\.\d+)?|[a-zA-Z\s]+?)(?=\s*(?:aur|making|wastage|gst|gold|sona|chandi|calculate|kar|$))""")
        val generalMatch = generalRateRegex.find(text)
        if (generalMatch != null) {
            val parsed = VoiceHindiNumberParser.parseNumber(generalMatch.groupValues[1])
            if (parsed != null && parsed > 0) {
                // If user stated "1 lakh" -> 100000, "95 hazar" -> 95000
                return parsed
            }
        }

        return null
    }

    /**
     * Extracts making charge percentage.
     */
    private fun extractMakingCharge(text: String): Double? {
        val makingRegex = Regex("""(?:making|making charge|banawat|judaai|jadai)\s*(?:charge|hai|is|=|rakho|karo)?\s*(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:percent|pratishat|%|rupaye|rs)?""")
        val match = makingRegex.find(text)
        if (match != null) {
            val parsed = VoiceHindiNumberParser.parseNumber(match.groupValues[1])
            if (parsed != null && parsed >= 0) return parsed
        }
        return null
    }

    /**
     * Extracts wastage percentage.
     */
    private fun extractWastage(text: String): Double? {
        if (text.contains("wastage nahi") || text.contains("zero wastage") || text.contains("wastage 0")) {
            return 0.0
        }

        val wastageRegex = Regex("""(?:wastage|khichai|chhijan)\s*(?:hai|is|=|rakho|karo)?\s*(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:percent|pratishat|%)?""")
        val match = wastageRegex.find(text)
        if (match != null) {
            val parsed = VoiceHindiNumberParser.parseNumber(match.groupValues[1])
            if (parsed != null && parsed >= 0) return parsed
        }
        return null
    }

    /**
     * Extracts GST percentage.
     */
    private fun extractGst(text: String): Double? {
        if (text.contains("gst nahi") || text.contains("bina gst") || text.contains("without gst") || text.contains("zero gst")) {
            return 0.0
        }

        val gstRegex = Regex("""(?:gst|tax)\s*(?:hai|is|=|rakho|karo)?\s*(\d+(?:\.\d+)?|[a-zA-Z\s]+?)\s*(?:percent|pratishat|%)?""")
        val match = gstRegex.find(text)
        if (match != null) {
            val parsed = VoiceHindiNumberParser.parseNumber(match.groupValues[1])
            if (parsed != null && parsed >= 0) return parsed
        }
        return null
    }

    /**
     * Extracts Gold Purity (OPTIONAL). Returns null if not spoken.
     */
    private fun extractGoldPurity(text: String): GoldPurity? {
        return when {
            text.contains("24k") || text.contains("24 k") || text.contains("24 carat") || text.contains("24 karat") || text.contains("chaubis carat") -> GoldPurity.K24
            text.contains("22k") || text.contains("22 k") || text.contains("22 carat") || text.contains("22 karat") || text.contains("baais carat") || text.contains("916") || text.contains("hallmark") -> GoldPurity.K22
            text.contains("18k") || text.contains("18 k") || text.contains("18 carat") || text.contains("18 karat") || text.contains("atharah carat") || text.contains("750") -> GoldPurity.K18
            else -> null // Purity is OPTIONAL!
        }
    }

    /**
     * Extracts Silver Purity (OPTIONAL). Returns null if not spoken.
     */
    private fun extractSilverPurity(text: String): SilverPurity? {
        return when {
            text.contains("999") || text.contains("fine silver") || text.contains("shuddh") -> SilverPurity.P999
            text.contains("925") || text.contains("sterling") || text.contains("silver 925") -> SilverPurity.P925
            else -> null // Purity is OPTIONAL!
        }
    }
}

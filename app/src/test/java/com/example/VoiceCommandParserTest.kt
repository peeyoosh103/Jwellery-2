package com.example

import com.example.models.GoldPurity
import com.example.models.MetalType
import com.example.models.SilverPurity
import com.example.voice.VoiceCommandParser
import com.example.voice.VoiceHindiNumberParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {

    @Test
    fun testIndianNumberParser_spokenWordsAndLakhs() {
        assertEquals(100000.0, VoiceHindiNumberParser.parseNumber("1 lakh") ?: 0.0, 0.01)
        assertEquals(100000.0, VoiceHindiNumberParser.parseNumber("ek lakh") ?: 0.0, 0.01)
        assertEquals(110000.0, VoiceHindiNumberParser.parseNumber("1 lakh 10 hazar") ?: 0.0, 0.01)
        assertEquals(150000.0, VoiceHindiNumberParser.parseNumber("dedh lakh") ?: 0.0, 0.01)
        assertEquals(250000.0, VoiceHindiNumberParser.parseNumber("dhai lakh") ?: 0.0, 0.01)
        assertEquals(95000.0, VoiceHindiNumberParser.parseNumber("95 hazar") ?: 0.0, 0.01)
        assertEquals(95000.0, VoiceHindiNumberParser.parseNumber("95k") ?: 0.0, 0.01)
        assertEquals(5.25, VoiceHindiNumberParser.parseNumber("5.25") ?: 0.0, 0.01)
        assertEquals(0.5, VoiceHindiNumberParser.parseNumber("aadha") ?: 0.0, 0.01)
    }

    @Test
    fun testVoiceCommand_withoutPurity_purityMustBeOptional() {
        // STEP 4 & 5 Requirement: "5 gram gold, rate 1 lakh 10 gram ka hai" must process without asking for purity
        val parsed = VoiceCommandParser.parse("5 gram gold, rate 1 lakh 10 gram ka hai", MetalType.GOLD)

        assertEquals(5.0, parsed.grams ?: 0.0, 0.01)
        assertEquals(100000.0, parsed.marketRate ?: 0.0, 0.01)
        assertNull("Purity must be null when not spoken so default UI purity is preserved without prompt", parsed.goldPurity)
        assertFalse("Clarification must not be needed for missing purity", parsed.clarificationNeeded)
    }

    @Test
    fun testVoiceCommand_multipleValuesInOneSentence() {
        // STEP 8: "5 gram gold hai, rate 1 lakh hai, making 8 percent aur wastage 2 percent rakho"
        val parsed = VoiceCommandParser.parse(
            "5 gram gold hai, rate 1 lakh hai, making 8 percent aur wastage 2 percent rakho",
            MetalType.GOLD
        )

        assertEquals(5.0, parsed.grams ?: 0.0, 0.01)
        assertEquals(100000.0, parsed.marketRate ?: 0.0, 0.01)
        assertEquals(8.0, parsed.makingChargePercent ?: 0.0, 0.01)
        assertEquals(2.0, parsed.wastagePercent ?: 0.0, 0.01)
        assertNull(parsed.goldPurity)
    }

    @Test
    fun testVoiceCommand_explicitPurity() {
        // If user explicitly says 24 carat
        val parsed24k = VoiceCommandParser.parse("10 gram 24 carat sona rate 1 lakh", MetalType.GOLD)
        assertEquals(10.0, parsed24k.grams ?: 0.0, 0.01)
        assertEquals(GoldPurity.K24, parsed24k.goldPurity)

        // If user explicitly says 18k
        val parsed18k = VoiceCommandParser.parse("7 gram 18k gold", MetalType.GOLD)
        assertEquals(7.0, parsed18k.grams ?: 0.0, 0.01)
        assertEquals(GoldPurity.K18, parsed18k.goldPurity)
    }

    @Test
    fun testVoiceCommand_calculateTrigger() {
        // STEP 10: Calculate commands
        val parsed1 = VoiceCommandParser.parse("calculate karo", MetalType.GOLD)
        assertTrue(parsed1.isCalculateTriggered)

        val parsed2 = VoiceCommandParser.parse("hisab karo", MetalType.GOLD)
        assertTrue(parsed2.isCalculateTriggered)

        val parsed3 = VoiceCommandParser.parse("result batao", MetalType.GOLD)
        assertTrue(parsed3.isCalculateTriggered)

        val parsed4 = VoiceCommandParser.parse("5 gram gold rate 1 lakh calculate karo", MetalType.GOLD)
        assertTrue(parsed4.isCalculateTriggered)
        assertEquals(5.0, parsed4.grams ?: 0.0, 0.01)
        assertEquals(100000.0, parsed4.marketRate ?: 0.0, 0.01)
    }

    @Test
    fun testVoiceCommand_resetTrigger() {
        // STEP 13: Reset commands
        val parsed1 = VoiceCommandParser.parse("reset karo", MetalType.GOLD)
        assertTrue(parsed1.isResetTriggered)

        val parsed2 = VoiceCommandParser.parse("sab clear karo", MetalType.GOLD)
        assertTrue(parsed2.isResetTriggered)

        val parsed3 = VoiceCommandParser.parse("dobara start karo", MetalType.GOLD)
        assertTrue(parsed3.isResetTriggered)
    }

    @Test
    fun testVoiceCommand_singleFieldEdit() {
        // STEP 12: "Weight 7 gram kar do"
        val parsed = VoiceCommandParser.parse("weight 7 gram kar do", MetalType.GOLD)
        assertEquals(7.0, parsed.grams ?: 0.0, 0.01)
        assertNull(parsed.marketRate)
        assertNull(parsed.makingChargePercent)
    }

    @Test
    fun testVoiceCommand_silverParsing() {
        val parsed = VoiceCommandParser.parse("250 gram chandi, rate 1 lakh 20 hazar per kg, making 5 percent", MetalType.SILVER)
        assertEquals(250.0, parsed.grams ?: 0.0, 0.01)
        assertEquals(120000.0, parsed.marketRate ?: 0.0, 0.01)
        assertEquals(5.0, parsed.makingChargePercent ?: 0.0, 0.01)
        assertNull(parsed.silverPurity)
    }
}

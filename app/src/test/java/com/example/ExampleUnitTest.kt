package com.example

import com.example.calculation.JewelleryCalculator
import com.example.models.CalculationInput
import com.example.models.GoldPurity
import com.example.models.MetalType
import com.example.models.SilverPurity
import com.example.validation.CalculationValidator
import com.example.validation.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JewelleryCalculatorTest {

    @Test
    fun testWeightConversion_gramsAndMilligrams() {
        // Gram = 5, Milligram = 250 -> 5.250 g
        val weight1 = JewelleryCalculator.calculateTotalWeight(5.0, 250.0)
        assertEquals(5.250, weight1, 0.0001)

        // Gram = 10, Milligram = 500 -> 10.500 g
        val weight2 = JewelleryCalculator.calculateTotalWeight(10.0, 500.0)
        assertEquals(10.500, weight2, 0.0001)

        // Milligram only: 750 mg -> 0.750 g
        val weight3 = JewelleryCalculator.calculateTotalWeight(0.0, 750.0)
        assertEquals(0.750, weight3, 0.0001)
    }

    @Test
    fun testGoldRateConversion_per10gToPerGram() {
        // ₹1,00,000 per 10g for 24K -> ₹10,000 per gram
        val rate24k = JewelleryCalculator.calculateGoldRatePerGram(100000.0, GoldPurity.K24.purityFactor)
        assertEquals(10000.0, rate24k, 0.01)

        // ₹90,000 per 10g for 22K -> ₹9,000 per gram (uses same 24K method: rate / 10)
        val rate22k = JewelleryCalculator.calculateGoldRatePerGram(90000.0, GoldPurity.K22.purityFactor)
        assertEquals(9000.0, rate22k, 0.01)

        // ₹75,000 per 10g for 18K -> ₹7,500 per gram (uses same 24K method: rate / 10)
        val rate18k = JewelleryCalculator.calculateGoldRatePerGram(75000.0, GoldPurity.K18.purityFactor)
        assertEquals(7500.0, rate18k, 0.01)
    }

    @Test
    fun testSilverRateConversion_per1kgToPerGram() {
        // ₹1,20,000 per 1 kg 999 -> ₹120 per gram
        val rate999 = JewelleryCalculator.calculateSilverRatePerGram(120000.0, SilverPurity.P999.purityFactor)
        assertEquals(120.0, rate999, 0.01)

        // ₹1,20,000 per 1 kg 925 -> ₹120 per gram (uses same 999 method: rate / 1000)
        val rate925 = JewelleryCalculator.calculateSilverRatePerGram(120000.0, SilverPurity.P925.purityFactor)
        assertEquals(120.0, rate925, 0.01)
    }

    @Test
    fun testFullGoldCalculationFormula() {
        // 10g 24K gold at ₹1,00,000/10g -> Metal value = ₹1,00,000
        // Making charge = 5% (₹5,000)
        // Wastage = 0% (₹0)
        // Taxable = ₹1,05,000
        // GST = 3% (₹3,150)
        // Total = ₹1,08,150
        val input = CalculationInput(
            metalType = MetalType.GOLD,
            grams = 10.0,
            milligrams = 0.0,
            purityLabel = "24K",
            purityFineness = "999",
            purityFactor = 1.0,
            marketRate = 100000.0,
            makingChargePercent = 5.0,
            wastagePercent = 0.0,
            gstPercent = 3.0
        )

        val result = JewelleryCalculator.calculate(input)
        assertEquals(10.0, result.weightGrams, 0.001)
        assertEquals(10000.0, result.ratePerGram, 0.01)
        assertEquals(100000.0, result.metalValue, 0.01)
        assertEquals(5000.0, result.makingChargeAmount, 0.01)
        assertEquals(0.0, result.wastageAmount, 0.01)
        assertEquals(105000.0, result.taxableAmount, 0.01)
        assertEquals(3150.0, result.gstAmount, 0.01)
        assertEquals(108150.0, result.totalAmount, 0.01)
    }

    @Test
    fun testFullSilverCalculationFormula() {
        // 500g 999 silver at ₹1,00,000/kg -> Rate = ₹100/g -> Metal value = ₹50,000
        // Making charge = 10% (₹5,000)
        // Wastage = 2% (₹1,000)
        // Taxable = ₹56,000
        // GST = 3% (₹1,680)
        // Total = ₹57,680
        val input = CalculationInput(
            metalType = MetalType.SILVER,
            grams = 500.0,
            milligrams = 0.0,
            purityLabel = "999",
            purityFineness = "99.9%",
            purityFactor = 1.0,
            marketRate = 100000.0,
            makingChargePercent = 10.0,
            wastagePercent = 2.0,
            gstPercent = 3.0
        )

        val result = JewelleryCalculator.calculate(input)
        assertEquals(500.0, result.weightGrams, 0.001)
        assertEquals(100.0, result.ratePerGram, 0.01)
        assertEquals(50000.0, result.metalValue, 0.01)
        assertEquals(5000.0, result.makingChargeAmount, 0.01)
        assertEquals(1000.0, result.wastageAmount, 0.01)
        assertEquals(56000.0, result.taxableAmount, 0.01)
        assertEquals(1680.0, result.gstAmount, 0.01)
        assertEquals(57680.0, result.totalAmount, 0.01)
    }

    @Test
    fun testValidation_emptyInputs() {
        val emptyWeight = CalculationValidator.validateInput("", "", "100000", "5", "0", "3")
        assertTrue(emptyWeight is ValidationResult.Invalid)

        val emptyRate = CalculationValidator.validateInput("5", "250", "", "5", "0", "3")
        assertTrue(emptyRate is ValidationResult.Invalid)

        val valid = CalculationValidator.validateInput("5", "250", "100000", "5", "0", "3")
        assertTrue(valid is ValidationResult.Valid)
    }

    @Test
    fun testValidation_negativeAndBounds() {
        val negative = CalculationValidator.validateInput("-5", "0", "100000", "5", "0", "3")
        assertTrue(negative is ValidationResult.Invalid)

        val invalidPercent = CalculationValidator.validateInput("5", "0", "100000", "150", "0", "3")
        assertTrue(invalidPercent is ValidationResult.Invalid)
    }
}

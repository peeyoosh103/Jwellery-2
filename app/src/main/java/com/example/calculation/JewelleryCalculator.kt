package com.example.calculation

import com.example.models.CalculationInput
import com.example.models.CalculationResult
import com.example.models.MetalType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToLong

object JewelleryCalculator {

    /**
     * Converts Grams and Milligrams to total weight in Grams.
     * Formula: Weight in grams = Gram + (Milligram / 1000)
     */
    fun calculateTotalWeight(grams: Double, milligrams: Double): Double {
        val validGrams = if (grams.isNaN() || grams < 0) 0.0 else grams
        val validMg = if (milligrams.isNaN() || milligrams < 0) 0.0 else milligrams
        return validGrams + (validMg / 1000.0)
    }

    /**
     * Converts Market Gold Rate (₹ per 10 gram) to ₹ per gram, taking purity into account.
     * Formula: Base Rate per gram = Gold Market Rate / 10.0
     * Rate per gram = Base Rate per gram * Purity Factor
     */
    fun calculateGoldRatePerGram(marketRatePer10g: Double, purityFactor: Double = 1.0): Double {
        if (marketRatePer10g <= 0) return 0.0
        val baseRatePerGram = marketRatePer10g / 10.0
        return baseRatePerGram * purityFactor
    }

    /**
     * Converts Market Silver Rate (₹ per 1 kg) to ₹ per gram, taking purity into account.
     * Formula: Base Rate per gram = Silver Market Rate / 1000.0
     * Rate per gram = Base Rate per gram * Purity Factor
     */
    fun calculateSilverRatePerGram(marketRatePer1kg: Double, purityFactor: Double = 1.0): Double {
        if (marketRatePer1kg <= 0) return 0.0
        val baseRatePerGram = marketRatePer1kg / 1000.0
        return baseRatePerGram * purityFactor
    }

    /**
     * Executes the standardized jewellery calculation for Gold or Silver.
     */
    fun calculate(input: CalculationInput): CalculationResult {
        val totalWeight = calculateTotalWeight(input.grams, input.milligrams)

        val ratePerGram = when (input.metalType) {
            MetalType.GOLD -> calculateGoldRatePerGram(input.marketRate, input.purityFactor)
            MetalType.SILVER -> calculateSilverRatePerGram(input.marketRate, input.purityFactor)
        }

        val metalValue = totalWeight * ratePerGram
        val makingChargeAmount = metalValue * (input.makingChargePercent / 100.0)
        val wastageAmount = metalValue * (input.wastagePercent / 100.0)
        val taxableAmount = metalValue + makingChargeAmount + wastageAmount
        val gstAmount = taxableAmount * (input.gstPercent / 100.0)
        val totalAmount = taxableAmount + gstAmount

        return CalculationResult(
            metalType = input.metalType,
            weightGrams = totalWeight,
            purityLabel = input.purityLabel,
            purityFineness = input.purityFineness,
            marketRate = input.marketRate,
            ratePerGram = ratePerGram,
            metalValue = metalValue,
            makingChargePercent = input.makingChargePercent,
            makingChargeAmount = makingChargeAmount,
            wastagePercent = input.wastagePercent,
            wastageAmount = wastageAmount,
            taxableAmount = taxableAmount,
            gstPercent = input.gstPercent,
            gstAmount = gstAmount,
            totalAmount = totalAmount
        )
    }

    /**
     * Formats currency with Indian rupee numbering system (e.g. ₹1,00,000 or ₹1,23,456.78).
     */
    fun formatCurrency(amount: Double, round: Boolean = true): String {
        if (amount.isNaN() || amount.isInfinite()) return "₹0.00"
        val formattedNumber = if (round) {
            val rounded = amount.roundToLong()
            formatIndianNumber(rounded.toDouble(), includeDecimals = false)
        } else {
            formatIndianNumber(amount, includeDecimals = true)
        }
        return "₹$formattedNumber"
    }

    /**
     * Formats Indian number comma grouping (3 digits for first thousands, then 2 digits: e.g. 10,00,000).
     */
    private fun formatIndianNumber(amount: Double, includeDecimals: Boolean): String {
        val symbols = DecimalFormatSymbols(Locale.ENGLISH)
        val longPart = amount.toLong()
        val decimalPart = String.format(Locale.ENGLISH, "%.2f", amount - longPart).removePrefix("0.")

        val str = longPart.toString()
        if (str.length <= 3) {
            return if (includeDecimals) "$str.$decimalPart" else str
        }

        val lastThree = str.substring(str.length - 3)
        val remaining = str.substring(0, str.length - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count == 2 && i != 0) {
                sb.append(',')
                count = 0
            }
        }
        val grouped = sb.reverse().toString() + "," + lastThree
        return if (includeDecimals) "$grouped.$decimalPart" else grouped
    }

    /**
     * Formats weight to 3 decimal places (e.g., 5.250 g).
     */
    fun formatWeight(weightGrams: Double): String {
        return String.format(Locale.ENGLISH, "%.3f", weightGrams)
    }

    /**
     * Formats rate per gram.
     */
    fun formatRate(rate: Double): String {
        return String.format(Locale.ENGLISH, "%.2f", rate)
    }
}

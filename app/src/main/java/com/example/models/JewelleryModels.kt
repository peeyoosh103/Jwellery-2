package com.example.models

enum class MetalType(val displayName: String) {
    GOLD("Gold"),
    SILVER("Silver")
}

enum class GoldPurity(val label: String, val fineness: String, val purityFactor: Double) {
    K24("24K", "999 / 999.9", 1.0),
    K22("22K", "916", 1.0),
    K18("18K", "750", 1.0);

    companion object {
        fun fromLabel(label: String): GoldPurity = entries.firstOrNull { it.label == label } ?: K22
    }
}

enum class SilverPurity(val label: String, val fineness: String, val purityFactor: Double) {
    P999("999", "99.9%", 1.0),
    P925("925", "92.5%", 1.0);

    companion object {
        fun fromLabel(label: String): SilverPurity = entries.firstOrNull { it.label == label } ?: P999
    }
}

data class CalculationInput(
    val metalType: MetalType,
    val grams: Double,
    val milligrams: Double,
    val purityLabel: String,
    val purityFineness: String,
    val purityFactor: Double,
    val marketRate: Double,
    val makingChargePercent: Double,
    val wastagePercent: Double,
    val gstPercent: Double
)

data class CalculationResult(
    val metalType: MetalType,
    val weightGrams: Double,
    val purityLabel: String,
    val purityFineness: String,
    val marketRate: Double,
    val ratePerGram: Double,
    val metalValue: Double,
    val makingChargePercent: Double,
    val makingChargeAmount: Double,
    val wastagePercent: Double,
    val wastageAmount: Double,
    val taxableAmount: Double,
    val gstPercent: Double,
    val gstAmount: Double,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.validation

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
}

object CalculationValidator {

    fun validateInput(
        gramsText: String,
        milligramsText: String,
        marketRateText: String,
        makingChargeText: String,
        wastageText: String,
        gstText: String
    ): ValidationResult {
        val grams = gramsText.trim().toDoubleOrNull() ?: 0.0
        val milligrams = milligramsText.trim().toDoubleOrNull() ?: 0.0

        if (grams <= 0.0 && milligrams <= 0.0) {
            return ValidationResult.Invalid("Please enter weight.")
        }

        if (grams < 0.0 || milligrams < 0.0) {
            return ValidationResult.Invalid("Weight cannot be negative.")
        }

        if (grams > 100000.0) {
            return ValidationResult.Invalid("Weight exceeds supported maximum limit.")
        }

        val rate = marketRateText.trim().toDoubleOrNull()
        if (rate == null || rate <= 0.0) {
            return ValidationResult.Invalid("Please enter rate.")
        }

        if (rate > 100_000_000.0) {
            return ValidationResult.Invalid("Market rate value is too large.")
        }

        val making = makingChargeText.trim().toDoubleOrNull() ?: 0.0
        if (making < 0.0 || making > 100.0) {
            return ValidationResult.Invalid("Making charge must be between 0% and 100%.")
        }

        val wastage = wastageText.trim().toDoubleOrNull() ?: 0.0
        if (wastage < 0.0 || wastage > 100.0) {
            return ValidationResult.Invalid("Wastage must be between 0% and 100%.")
        }

        val gst = gstText.trim().toDoubleOrNull() ?: 0.0
        if (gst < 0.0 || gst > 100.0) {
            return ValidationResult.Invalid("GST must be between 0% and 100%.")
        }

        return ValidationResult.Valid
    }
}

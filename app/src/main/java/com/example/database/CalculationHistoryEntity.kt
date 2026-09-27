package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.models.CalculationResult
import com.example.models.MetalType

@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val metalType: String, // "GOLD" or "SILVER"
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
) {
    fun toCalculationResult(): CalculationResult {
        return CalculationResult(
            metalType = if (metalType.equals("SILVER", ignoreCase = true)) MetalType.SILVER else MetalType.GOLD,
            weightGrams = weightGrams,
            purityLabel = purityLabel,
            purityFineness = purityFineness,
            marketRate = marketRate,
            ratePerGram = ratePerGram,
            metalValue = metalValue,
            makingChargePercent = makingChargePercent,
            makingChargeAmount = makingChargeAmount,
            wastagePercent = wastagePercent,
            wastageAmount = wastageAmount,
            taxableAmount = taxableAmount,
            gstPercent = gstPercent,
            gstAmount = gstAmount,
            totalAmount = totalAmount,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromCalculationResult(result: CalculationResult): CalculationHistoryEntity {
            return CalculationHistoryEntity(
                metalType = result.metalType.name,
                weightGrams = result.weightGrams,
                purityLabel = result.purityLabel,
                purityFineness = result.purityFineness,
                marketRate = result.marketRate,
                ratePerGram = result.ratePerGram,
                metalValue = result.metalValue,
                makingChargePercent = result.makingChargePercent,
                makingChargeAmount = result.makingChargeAmount,
                wastagePercent = result.wastagePercent,
                wastageAmount = result.wastageAmount,
                taxableAmount = result.taxableAmount,
                gstPercent = result.gstPercent,
                gstAmount = result.gstAmount,
                totalAmount = result.totalAmount,
                timestamp = result.timestamp
            )
        }
    }
}

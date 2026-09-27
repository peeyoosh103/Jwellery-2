package com.example.sharing

import android.content.Context
import android.content.Intent
import com.example.calculation.JewelleryCalculator
import com.example.models.CalculationResult
import com.example.models.MetalType

object CalculationShareHelper {

    fun generateShareText(result: CalculationResult): String {
        val weightStr = JewelleryCalculator.formatWeight(result.weightGrams)
        val metalValueStr = JewelleryCalculator.formatCurrency(result.metalValue)
        val makingChargeStr = JewelleryCalculator.formatCurrency(result.makingChargeAmount)
        val wastageStr = JewelleryCalculator.formatCurrency(result.wastageAmount)
        val gstStr = JewelleryCalculator.formatCurrency(result.gstAmount)
        val totalStr = JewelleryCalculator.formatCurrency(result.totalAmount)

        return if (result.metalType == MetalType.GOLD) {
            val marketRateStr = JewelleryCalculator.formatCurrency(result.marketRate)
            """
            Peeyoosh_Creation

            Metal: Gold
            Purity: ${result.purityLabel} / ${result.purityFineness}
            Weight: $weightStr g
            Market Rate: $marketRateStr / 10 g

            Gold Value: $metalValueStr
            Making (${result.makingChargePercent}%): $makingChargeStr
            Wastage (${result.wastagePercent}%): $wastageStr
            GST (${result.gstPercent}%): $gstStr

            TOTAL: $totalStr
            """.trimIndent()
        } else {
            val marketRateStr = JewelleryCalculator.formatCurrency(result.marketRate)
            """
            Peeyoosh_Creation

            Metal: Silver
            Purity: ${result.purityLabel} (${result.purityFineness})
            Weight: $weightStr g
            Market Rate: $marketRateStr / kg

            Silver Value: $metalValueStr
            Making (${result.makingChargePercent}%): $makingChargeStr
            Wastage (${result.wastagePercent}%): $wastageStr
            GST (${result.gstPercent}%): $gstStr

            TOTAL: $totalStr
            """.trimIndent()
        }
    }

    fun shareCalculation(context: Context, result: CalculationResult) {
        val text = generateShareText(result)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Peeyoosh_Creation - ${result.metalType.displayName} Price Estimate")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Share Calculation via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

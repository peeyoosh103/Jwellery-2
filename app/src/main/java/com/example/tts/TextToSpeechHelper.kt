package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.models.CalculationResult
import com.example.models.MetalType
import com.example.settings.AppLanguage
import java.util.Locale
import kotlin.math.roundToLong

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("TextToSpeechHelper", "Failed to initialize TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            try {
                val hindiLocale = Locale("hi", "IN")
                val result = textToSpeech?.setLanguage(hindiLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.setLanguage(Locale.ENGLISH)
                }
            } catch (e: Exception) {
                Log.w("TextToSpeechHelper", "Could not set Hindi locale on TTS engine", e)
            }
        } else {
            isInitialized = false
        }
    }

    fun speakResult(result: CalculationResult, language: AppLanguage, speed: Float) {
        if (!isInitialized || textToSpeech == null) return

        try {
            textToSpeech?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))

            val speechText = generateSpeechText(result, language)

            if (language == AppLanguage.HINDI) {
                val hindiLocale = Locale("hi", "IN")
                textToSpeech?.setLanguage(hindiLocale)
            } else {
                textToSpeech?.setLanguage(Locale.ENGLISH)
            }

            textToSpeech?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "JewelleryCalcTTS")
        } catch (e: Exception) {
            Log.e("TextToSpeechHelper", "Error speaking text", e)
        }
    }

    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.w("TextToSpeechHelper", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isInitialized = false
        } catch (e: Exception) {
            Log.w("TextToSpeechHelper", "Error shutting down TTS", e)
        }
    }

    private fun generateSpeechText(result: CalculationResult, language: AppLanguage): String {
        val roundedAmount = result.totalAmount.roundToLong()
        val metalNameHi = if (result.metalType == MetalType.GOLD) "सोने" else "चांदी"
        val metalNameEn = if (result.metalType == MetalType.GOLD) "Gold" else "Silver"

        return if (language == AppLanguage.HINDI) {
            val amountInWordsHi = convertNumberToHindiWords(roundedAmount)
            "$metalNameHi ka kul total amount hai $amountInWordsHi rupaye."
        } else {
            val amountInWordsEn = convertNumberToEnglishWords(roundedAmount)
            "The total $metalNameEn price is $amountInWordsEn rupees."
        }
    }

    /**
     * Converts a number to spoken Hindi text for clear pronunciation.
     */
    private fun convertNumberToHindiWords(num: Long): String {
        if (num <= 0) return "shunya"
        if (num >= 10_00_00_000) return "$num"

        val sb = StringBuilder()
        var n = num

        val crore = n / 10000000
        if (crore > 0) {
            sb.append(getHindiNumberWord(crore)).append(" karod ")
            n %= 10000000
        }

        val lakh = n / 100000
        if (lakh > 0) {
            sb.append(getHindiNumberWord(lakh)).append(" lakh ")
            n %= 100000
        }

        val thousand = n / 1000
        if (thousand > 0) {
            sb.append(getHindiNumberWord(thousand)).append(" hazaar ")
            n %= 1000
        }

        val hundred = n / 100
        if (hundred > 0) {
            sb.append(getHindiNumberWord(hundred)).append(" sau ")
            n %= 100
        }

        if (n > 0) {
            sb.append(getHindiNumberWord(n)).append(" ")
        }

        return sb.toString().trim()
    }

    private fun getHindiNumberWord(n: Long): String {
        return when (n) {
            1L -> "ek"
            2L -> "do"
            3L -> "teen"
            4L -> "char"
            5L -> "paanch"
            6L -> "chhah"
            7L -> "saat"
            8L -> "aath"
            9L -> "nau"
            10L -> "dus"
            11L -> "gyarah"
            12L -> "barah"
            13L -> "terah"
            14L -> "chaudah"
            15L -> "pandrah"
            16L -> "solah"
            17L -> "satrah"
            18L -> "atharah"
            19L -> "unnis"
            20L -> "bees"
            21L -> "ikkis"
            22L -> "baais"
            23L -> "teis"
            24L -> "chaubis"
            25L -> "pacchis"
            28L -> "atthaais"
            30L -> "tees"
            35L -> "paintis"
            40L -> "chalis"
            45L -> "paintalis"
            50L -> "pachaas"
            55L -> "pachpan"
            60L -> "saath"
            65L -> "painsath"
            66L -> "chhiyasath"
            70L -> "sattar"
            75L -> "pachattar"
            80L -> "assi"
            85L -> "pachasi"
            90L -> "nabbe"
            91L -> "ikyanve"
            92L -> "baanve"
            95L -> "pichanve"
            99L -> "ninyanve"
            100L -> "sau"
            else -> n.toString()
        }
    }

    private fun convertNumberToEnglishWords(num: Long): String {
        if (num == 0L) return "zero"
        val units = arrayOf(
            "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
        )
        val tens = arrayOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")

        fun helper(n: Long): String {
            return when {
                n < 20 -> units[n.toInt()]
                n < 100 -> tens[(n / 10).toInt()] + (if (n % 10 != 0L) " " + units[(n % 10).toInt()] else "")
                n < 1000 -> units[(n / 100).toInt()] + " hundred" + (if (n % 100 != 0L) " " + helper(n % 100) else "")
                n < 100000 -> helper(n / 1000) + " thousand" + (if (n % 1000 != 0L) " " + helper(n % 1000) else "")
                n < 10000000 -> helper(n / 100000) + " lakh" + (if (n % 100000 != 0L) " " + helper(n % 100000) else "")
                else -> helper(n / 10000000) + " crore" + (if (n % 10000000 != 0L) " " + helper(n % 10000000) else "")
            }
        }

        return helper(num).trim()
    }
}

package com.github.numberspellout

/**
 * Coder: Madhav Aneraye
 * Converts numbers into their fully spelled-out English word form,
 * using the international numbering system (thousand, million, billion, trillion).
 *
 * Example:
 *   NumberSpellout.toWords(1234)        -> "One Thousand Two Hundred Thirty-Four"
 *   NumberSpellout.toWords(1_000_000)   -> "One Million"
 *   NumberSpellout.toWords(-42)         -> "Negative Forty-Two"
 *   NumberSpellout.toWords(19.5)        -> "Nineteen Point Five"
 */
object NumberSpellout {

    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"
    )
    private val teens = arrayOf(
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen",
        "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )
    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )
    private val scales = arrayOf(
        "", "Thousand", "Million", "Billion", "Trillion", "Quadrillion"
    )

    /** Converts a [Long] into spelled-out words. */
    fun toWords(number: Long): String {
        if (number == 0L) return "Zero"

        val isNegative = number < 0
        var n = if (isNegative) -number else number

        if (n > 999_000_000_000_000_999L) {
            // Beyond Quadrillion range supported here; fall back gracefully.
            return if (isNegative) "Negative ${n}" else n.toString()
        }

        val groups = mutableListOf<Long>()
        while (n > 0) {
            groups.add(n % 1000)
            n /= 1000
        }

        val parts = mutableListOf<String>()
        for (i in groups.indices.reversed()) {
            val group = groups[i]
            if (group == 0L) continue
            val groupWords = threeDigitsToWords(group.toInt())
            val scale = scales.getOrElse(i) { "" }
            parts.add(if (scale.isNotEmpty()) "$groupWords $scale" else groupWords)
        }

        val result = parts.joinToString(" ")
        return if (isNegative) "Negative $result" else result
    }

    /** Convenience overload for [Int]. */
    fun toWords(number: Int): String = toWords(number.toLong())

    /**
     * Converts a [Double] into spelled-out words, including the decimal part
     * read digit-by-digit after "Point" (e.g. 19.5 -> "Nineteen Point Five").
     */
    fun toWords(number: Double, decimalPlaces: Int = 2): String {
        val isNegative = number < 0
        val absNumber = kotlin.math.abs(number)

        val wholePart = absNumber.toLong()
        val factor = Math.pow(10.0, decimalPlaces.toDouble())
        val decimalPart = Math.round((absNumber - wholePart) * factor).toString().padStart(decimalPlaces, '0')

        val wholeWords = toWords(wholePart)

        if (decimalPart.toLongOrNull() == 0L || decimalPlaces == 0) {
            return if (isNegative) "Negative $wholeWords" else wholeWords
        }

        val decimalWords = decimalPart.map { digit ->
            units.getOrElse(digit.toString().toInt()) { "" }.ifEmpty { "Zero" }
        }.joinToString(" ")

        val result = "$wholeWords Point $decimalWords"
        return if (isNegative) "Negative $result" else result
    }

    /**
     * Converts a number into a currency-style phrase, e.g.
     * toCurrencyWords(1250.75, "Dollar", "Cent") ->
     *   "One Thousand Two Hundred Fifty Dollars and Seventy-Five Cents"
     */
    fun toCurrencyWords(
        amount: Double,
        majorUnit: String = "Dollar",
        minorUnit: String = "Cent",
        decimalPlaces: Int = 2
    ): String {
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)

        val wholePart = absAmount.toLong()
        val factor = Math.pow(10.0, decimalPlaces.toDouble())
        val minorPart = Math.round((absAmount - wholePart) * factor)

        val majorWords = toWords(wholePart)
        val majorLabel = pluralize(wholePart, majorUnit)

        val result = if (minorPart > 0) {
            val minorWords = toWords(minorPart)
            val minorLabel = pluralize(minorPart, minorUnit)
            "$majorWords $majorLabel and $minorWords $minorLabel"
        } else {
            "$majorWords $majorLabel"
        }

        return if (isNegative) "Negative $result" else result
    }

    private fun pluralize(value: Long, unit: String): String =
        if (value == 1L) unit else "${unit}s"

    private fun threeDigitsToWords(number: Int): String {
        val hundredsDigit = number / 100
        val remainder = number % 100

        val parts = mutableListOf<String>()
        if (hundredsDigit > 0) {
            parts.add("${units[hundredsDigit]} Hundred")
        }
        if (remainder > 0) {
            parts.add(twoDigitsToWords(remainder))
        }
        return parts.joinToString(" ")
    }

    private fun twoDigitsToWords(number: Int): String {
        return when {
            number < 10 -> units[number]
            number < 20 -> teens[number - 10]
            else -> {
                val tensWord = tens[number / 10]
                val unitWord = units[number % 10]
                if (unitWord.isEmpty()) tensWord else "$tensWord-$unitWord"
            }
        }
    }
}

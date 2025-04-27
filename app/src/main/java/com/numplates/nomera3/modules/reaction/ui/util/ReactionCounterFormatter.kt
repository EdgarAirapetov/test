package com.numplates.nomera3.modules.reaction.ui.util

import com.meera.core.extensions.empty
import java.math.BigDecimal
import java.math.RoundingMode

private const val HUNDRED_END_VALUE = 999

private const val THOUSAND = 1000
private const val LAST_VALUE_BEFORE_TEN_THOUSAND = 9999
private const val THOUSAND_END_VALUE = 999999

private const val MILLION = 1000000

/**
 * @param oneAllow
 * Разрешенно ли отображать число 1
 *
 * @param thousandAllow
 * разрешенио ли отображать значения от 1000 до 9999 не форматируя
 */
class ReactionCounterFormatter(
    val thousandLabel: String,
    val millionLabel: String,
    val oneAllow: Boolean,
    val thousandAllow: Boolean
) {

    fun format(value: Int): String {
        return format(value.toLong())
    }

    fun format(value: Long): String {
        if (value == 0L || value < 0L) {
            return String.empty()
        }
        if (value == 1L && !oneAllow) {
            return String.empty()
        }
        if (value == 1L && oneAllow) {
            return value.toString()
        }
        if (value in 2..HUNDRED_END_VALUE && !thousandAllow) {
            return value.toString()
        }
        if (value in 2..LAST_VALUE_BEFORE_TEN_THOUSAND && thousandAllow) {
            return value.toString()
        }
        return postfixLabelFormatter(value)
    }

    private fun postfixLabelFormatter(counter: Long): String {
        var returnedValue = String.empty()
        if (counter in THOUSAND..THOUSAND_END_VALUE) {
            val formattedValue = formatValue(counter, THOUSAND)
            returnedValue = "$formattedValue$thousandLabel"
        }
        if (counter >= MILLION) {
            val formattedValue = formatValue(counter, MILLION)
            returnedValue = "$formattedValue$millionLabel"
        }
        return returnedValue
    }

    private fun formatValue(longCounter: Long, intDivider: Int): String {
        val counter = longCounter.toBigDecimal()
        val divider = intDivider.toBigDecimal()
        val fractionalValue = counter.divide(divider)
        val fractionalPart = fractionalValue.setScale(1, RoundingMode.DOWN)
        return fractionalPart.toCounterFormat()
    }

    private fun BigDecimal.toCounterFormat(): String {
        val stringNumber = this.toString()
        return stringNumber.replace(",0", "")
    }
}

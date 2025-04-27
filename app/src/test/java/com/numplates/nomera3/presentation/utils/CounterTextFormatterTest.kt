package com.numplates.nomera3.presentation.utils

import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter
import org.junit.Assert
import org.junit.Test

private const val THOUSAND_LABEL = "k"
private const val MILLION_LABEL = "m"

internal class CounterTextFormatterTest {
    @Test
    fun testPostCounterFormatter() {
        val formatter = ReactionCounterFormatter(
            THOUSAND_LABEL,
            MILLION_LABEL,
            oneAllow = true,
            thousandAllow = false
        )

        // проверить на обычных значениях
        Assert.assertEquals("1", formatter.format(1))
        Assert.assertEquals("2", formatter.format(2))
        Assert.assertEquals("999", formatter.format(999))

        // проверить значения 1000
        Assert.assertEquals("1$THOUSAND_LABEL", formatter.format(1000))
        Assert.assertEquals("1$THOUSAND_LABEL", formatter.format(1001))
        Assert.assertEquals("1$THOUSAND_LABEL", formatter.format(1002))
        Assert.assertEquals("1$THOUSAND_LABEL", formatter.format(1049))
        Assert.assertEquals("1$THOUSAND_LABEL", formatter.format(1051))
        // шаг в 100 не пройден - отображаем 1k
        Assert.assertEquals("1$THOUSAND_LABEL", formatter.format(1099))
        // шаг в 100 пройден - отображаем 1,1k
        Assert.assertEquals("1,1$THOUSAND_LABEL", formatter.format(1100))
        Assert.assertEquals("1,1$THOUSAND_LABEL", formatter.format(1149))
        Assert.assertEquals("1,1$THOUSAND_LABEL", formatter.format(1150))
        Assert.assertEquals("1,1$THOUSAND_LABEL", formatter.format(1151))
        Assert.assertEquals("1,1$THOUSAND_LABEL", formatter.format(1199))
        // шаг в 400 пройден - отображаем 1,4k, но не округляем до 1,5k
        Assert.assertEquals("1,4$THOUSAND_LABEL", formatter.format(1400))
        Assert.assertEquals("1,4$THOUSAND_LABEL", formatter.format(1449))
        Assert.assertEquals("1,4$THOUSAND_LABEL", formatter.format(1450))
        Assert.assertEquals("1,4$THOUSAND_LABEL", formatter.format(1451))
        Assert.assertEquals("1,4$THOUSAND_LABEL", formatter.format(1499))
        // шаг в 900 пройден - отображаем 1,9k, но не округляем до 2k
        Assert.assertEquals("1,9$THOUSAND_LABEL", formatter.format(1900))
        Assert.assertEquals("1,9$THOUSAND_LABEL", formatter.format(1949))
        Assert.assertEquals("1,9$THOUSAND_LABEL", formatter.format(1950))
        Assert.assertEquals("1,9$THOUSAND_LABEL", formatter.format(1951))
        Assert.assertEquals("1,9$THOUSAND_LABEL", formatter.format(1999))

        // проверить значения 10000
        Assert.assertEquals("10$THOUSAND_LABEL", formatter.format(10049))
        Assert.assertEquals("10$THOUSAND_LABEL", formatter.format(10051))
        // шаг в 100 не пройден - отображаем 10k
        Assert.assertEquals("10$THOUSAND_LABEL", formatter.format(10099))
        // шаг в 100 пройден - отображаем 10,1k
        Assert.assertEquals("10,1$THOUSAND_LABEL", formatter.format(10100))
        // шаг в 100 пройден - отображаем 10,1k, но не округляем до 10,2k
        Assert.assertEquals("10,1$THOUSAND_LABEL", formatter.format(10149))
        Assert.assertEquals("10,1$THOUSAND_LABEL", formatter.format(10150))
        Assert.assertEquals("10,1$THOUSAND_LABEL", formatter.format(10151))
        Assert.assertEquals("10,1$THOUSAND_LABEL", formatter.format(10199))
        // проверяем на промежуточных значениях
        Assert.assertEquals("10,2$THOUSAND_LABEL", formatter.format(10200))
        Assert.assertEquals("10,3$THOUSAND_LABEL", formatter.format(10300))
        Assert.assertEquals("10,4$THOUSAND_LABEL", formatter.format(10451))
        Assert.assertEquals("10,4$THOUSAND_LABEL", formatter.format(10499))
        // шаг в 500 пройден - отображаем 10,5k, но не округляем до 10,6k
        Assert.assertEquals("10,5$THOUSAND_LABEL", formatter.format(10500))
        Assert.assertEquals("10,5$THOUSAND_LABEL", formatter.format(10549))
        Assert.assertEquals("10,5$THOUSAND_LABEL", formatter.format(10550))
        Assert.assertEquals("10,5$THOUSAND_LABEL", formatter.format(10551))
        Assert.assertEquals("10,5$THOUSAND_LABEL", formatter.format(10599))

        // проверяем значения 100000
        Assert.assertEquals("100$THOUSAND_LABEL", formatter.format(100000))
        Assert.assertEquals("100,5$THOUSAND_LABEL", formatter.format(100555))
        Assert.assertEquals("100,5$THOUSAND_LABEL", formatter.format(100599))
        Assert.assertEquals("109,5$THOUSAND_LABEL", formatter.format(109599))
        Assert.assertEquals("119,9$THOUSAND_LABEL", formatter.format(119999))
        Assert.assertEquals("120$THOUSAND_LABEL", formatter.format(120000))
        // проверяем значение 500000
        Assert.assertEquals("500$THOUSAND_LABEL", formatter.format(500000))
        // проверяем значение 500500
        Assert.assertEquals("500,5$THOUSAND_LABEL", formatter.format(500500))
        Assert.assertEquals("500,5$THOUSAND_LABEL", formatter.format(500500))
        // проверяем на промежуточных значениях
        Assert.assertEquals("544,9$THOUSAND_LABEL", formatter.format(544999))
        Assert.assertEquals("549,9$THOUSAND_LABEL", formatter.format(549999))
        Assert.assertEquals("599,9$THOUSAND_LABEL", formatter.format(599999))

        // проверяем значение 999XXX
        // округления не должно произойти
        Assert.assertEquals("999,4$THOUSAND_LABEL", formatter.format(999499))
        Assert.assertEquals("999,5$THOUSAND_LABEL", formatter.format(999500))
        Assert.assertEquals("999,9$THOUSAND_LABEL", formatter.format(999949))
        Assert.assertEquals("999,9$THOUSAND_LABEL", formatter.format(999950))
        Assert.assertEquals("999,9$THOUSAND_LABEL", formatter.format(999951))
        Assert.assertEquals("999,9$THOUSAND_LABEL", formatter.format(999999))

        // проверяем значение
        Assert.assertEquals("1$MILLION_LABEL", formatter.format(1_010_999))
        Assert.assertEquals("1$MILLION_LABEL", formatter.format(1_010_999))
        Assert.assertEquals("1$MILLION_LABEL", formatter.format(1_010_999))
        Assert.assertEquals("1$MILLION_LABEL", formatter.format(1_049_000))
        Assert.assertEquals("1$MILLION_LABEL", formatter.format(1_050_000))
        Assert.assertEquals("1$MILLION_LABEL", formatter.format(1_051_000))
        Assert.assertEquals("1,1$MILLION_LABEL", formatter.format(1_100_999))

        // проверяем значение 10_001_000, округления не должно произойти
        Assert.assertEquals("10$MILLION_LABEL", formatter.format(10_001_999))
        Assert.assertEquals("10$MILLION_LABEL", formatter.format(10_010_999))
        Assert.assertEquals("10$MILLION_LABEL", formatter.format(10_049_000))
        Assert.assertEquals("10$MILLION_LABEL", formatter.format(10_050_000))
        Assert.assertEquals("10$MILLION_LABEL", formatter.format(10_051_000))
        Assert.assertEquals("10,1$MILLION_LABEL", formatter.format(10_100_999))

        // проверить на экстремально высокие значения
        Assert.assertEquals("100$MILLION_LABEL", formatter.format(100_000_000))
        Assert.assertEquals("155,9$MILLION_LABEL", formatter.format(155_999_999))
        Assert.assertEquals("199,9$MILLION_LABEL", formatter.format(199_999_999))
        Assert.assertEquals("499,9$MILLION_LABEL", formatter.format(499_999_999))
        Assert.assertEquals("500$MILLION_LABEL", formatter.format(500_000_000))
        Assert.assertEquals("999$MILLION_LABEL", formatter.format(999_099_999))
        Assert.assertEquals("999,9$MILLION_LABEL", formatter.format(999_999_999))
        Assert.assertEquals("1000$MILLION_LABEL", formatter.format(1000_000_000))
    }
}
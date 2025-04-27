package com.numplates.nomera3.modules.holidays.ui

import android.text.SpannableStringBuilder
import android.widget.TextView
import com.meera.core.extensions.addClick
import com.meera.core.extensions.color
import com.meera.core.extensions.doAsyncOnView
import com.meera.core.extensions.makeProperEnd
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayInfo
import com.numplates.nomera3.presentation.view.ui.MeeraTextViewWithImages
import com.numplates.nomera3.presentation.view.ui.TextViewWithImages
import javax.inject.Inject

fun TextViewWithImages.processHolidayText(isVip: Boolean, clickAction: () -> Unit) {
    val processor = XmasTextSpannableProcessor()

    if (!processor.holidayInfoHelper.isHolidayExistAndMatches() ||
            processor.holidayInfoHelper.currentHoliday().code != HolidayInfo.HOLIDAY_NEW_YEAR) return
    val stringBuilder = strBuilder
    if (stringBuilder.isNullOrEmpty()) {
        processor.processTextForString(
                isVip = isVip,
                textView = this,
                text = this.text.toString(),
                clickAction = clickAction
        )
    } else {
        processor.processTextForSpannable(
                isVip = isVip,
                textView = this,
                text = stringBuilder,
                clickAction = clickAction
        )
    }
}

fun MeeraTextViewWithImages.processHolidayText(isVip: Boolean, clickAction: () -> Unit) {
    val processor = XmasTextSpannableProcessor()

    if (!processor.holidayInfoHelper.isHolidayExistAndMatches() ||
        processor.holidayInfoHelper.currentHoliday().code != HolidayInfo.HOLIDAY_NEW_YEAR) return
    val stringBuilder = strBuilder
    if (stringBuilder.isNullOrEmpty()) {
        processor.processTextForString(
            isVip = isVip,
            textView = this,
            text = this.text.toString(),
            clickAction = clickAction
        )
    } else {
        processor.processTextForSpannable(
            isVip = isVip,
            textView = this,
            text = stringBuilder,
            clickAction = clickAction
        )
    }
}

class XmasTextSpannableProcessor {

    @Inject
    lateinit var holidayInfoHelper: HolidayInfoHelper

    init {
        App.component.inject(this)
    }

    private fun getRegex(symbols: String): Regex =
            Regex("(?i)(?<!\\p{L})$symbols(?!\\p{L})", RegexOption.IGNORE_CASE)

    fun processTextForSpannable(
            isVip: Boolean = false,
            textView: TextView,
            text: SpannableStringBuilder,
            clickAction: () -> Unit,
    ) {
        textView.doAsyncOnView({
            val words = mutableListOf<IntRange>()

            if (text.isEmpty()) return@doAsyncOnView text

            listOfKeyWords.forEach { key ->
                getRegex(key).findAll(text).forEach {
                    words.add(it.range)
                }
            }

            val color =
                    if (isVip) textView.context.color(R.color.colorGoldHoliday)
                    else textView.context.color(R.color.ui_purple)

            words.forEach {
                text.addClick(it.makeProperEnd(), color, clickAction)
            }

            text
        }, {
            textView.text = it
        })
    }


    fun processTextForString(
            isVip: Boolean = false,
            textView: TextViewWithImages,
            text: String,
            clickAction: () -> Unit,
    ) {
        textView.doAsyncOnView({
            val words = mutableListOf<IntRange>()

            val str = SpannableStringBuilder(text)

            if (text.isEmpty()) return@doAsyncOnView str

            listOfKeyWords.forEach { key ->
                getRegex(key).findAll(text).forEach {
                    words.add(it.range)
                }
            }

            val color =
                    if (isVip) textView.context.color(R.color.colorGoldHoliday)
                    else textView.context.color(R.color.ui_purple)

            words.forEach {
                str.addClick(it.makeProperEnd(), color, clickAction)
            }

            str
        }, {
            textView.strBuilder = it
            textView.text = it
        })
    }

    fun processTextForString(
        isVip: Boolean = false,
        textView: MeeraTextViewWithImages,
        text: String,
        clickAction: () -> Unit,
    ) {
        textView.doAsyncOnView({
            val words = mutableListOf<IntRange>()

            val str = SpannableStringBuilder(text)

            if (text.isEmpty()) return@doAsyncOnView str

            listOfKeyWords.forEach { key ->
                getRegex(key).findAll(text).forEach {
                    words.add(it.range)
                }
            }

            val color =
                if (isVip) textView.context.color(R.color.colorGoldHoliday)
                else textView.context.color(R.color.ui_purple)

            words.forEach {
                str.addClick(it.makeProperEnd(), color, clickAction)
            }

            str
        }, {
            textView.strBuilder = it
            textView.text = it
        })
    }

    private val listOfKeyWords = listOf(
            "Новый год",
            "С новым годом",
            "С нг",
            "нг",
            "С наступающим новым годом",
            "С наступающим 2022",
            "С новым 2022 годом",
            "С Новым годом и рождеством",
            "С наступлением нового года",
            "С рождеством",
            "С новым 2022",
            "С наступающим года тигра",
            "С Новым годом, с новым счастьем",
            "С Новым годом с новым счастьем",
            "С новогодними праздниками",
            "С окончанием 2021",
            "С годом тигра",
            "2022 год",
            "новым годом",
            "наступающим",
            "новогоднее",
            "новогоднему",
            "год тигра",
            "new year",
            "Xmas",
            "merry christmas")
}

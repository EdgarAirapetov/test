package com.numplates.nomera3.presentation.view.fragments.userprofileinfo

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import com.numplates.nomera3.MAX_USER_AGE
import com.numplates.nomera3.MIN_USER_AGE
import com.numplates.nomera3.R
import java.util.Calendar

object BirthdayDatePickerDialog {
    fun show(context: Context, previousSelectedDate: Long?, onDateSelected: (calendar: Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        if (previousSelectedDate != null) {
            calendar.timeInMillis = previousSelectedDate
        }

        val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val newCalendar = Calendar.getInstance()
            newCalendar.set(Calendar.YEAR, year)
            newCalendar.set(Calendar.MONTH, month)
            newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            onDateSelected(newCalendar)
        }

        DatePickerDialog(
            context,
            listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = Calendar
                .getInstance()
                .apply { add(Calendar.YEAR, -MAX_USER_AGE) }
                .time
                .time

            datePicker.maxDate = Calendar
                .getInstance()
                .apply { add(Calendar.YEAR, -MIN_USER_AGE) }
                .time
                .time

            setButton(
                BUTTON_POSITIVE,
                context.getString(R.string.birthday_date_picker_dialog_positive_button_text),
                this
            )
            setButton(
                BUTTON_NEGATIVE,
                context.getString(R.string.birthday_date_picker_dialog_negative_button_text),
                this
            )
            setTitle(R.string.profile_your_birthday)
            setOnDismissListener {

            }
        }.show()
    }
}

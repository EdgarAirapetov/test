package com.numplates.nomera3.presentation.view.fragments.userprofileinfo

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import com.numplates.nomera3.MAX_USER_AGE
import com.numplates.nomera3.MIN_USER_AGE
import com.numplates.nomera3.R
import java.util.Calendar

object MeeraBirthdayDatePickerDialog {
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
            R.style.AppTheme_DatePickerDialog,
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
                context.getString(R.string.map_events_time_picker_action),
                this
            )
            setButton(
                BUTTON_NEGATIVE,
                context.getString(R.string.cancel),
                this
            )
            setTitle(R.string.date_birth)
            setOnDismissListener {

            }
        }.show()
    }
}

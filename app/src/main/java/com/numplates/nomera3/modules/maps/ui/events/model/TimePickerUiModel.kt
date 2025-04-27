package com.numplates.nomera3.modules.maps.ui.events.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalTime

@Parcelize
data class TimePickerUiModel(
    val minimumTime: LocalTime?,
    val selectedTime: LocalTime,
    val isInUserTimezone: Boolean
): Parcelable

package com.numplates.nomera3.modules.holidays.ui.calendar

import androidx.core.content.res.ResourcesCompat
import com.meera.core.extensions.empty
import com.meera.uikit.widgets.buttons.ButtonType

data class MeeraHolidayCalendarBottomData(
    val header: String = String.empty(),
    val headerRes: Int = ResourcesCompat.ID_NULL,
    val description: String = String.empty(),
    val descriptionRes: Int = ResourcesCompat.ID_NULL,
    val currentDay: String = String.empty(),
    val totalDay: String = String.empty(),
    val imageRes: Int = ResourcesCompat.ID_NULL,
    val confirmBtnText: String = String.empty(),
    val confirmBtnTextRes: Int = ResourcesCompat.ID_NULL,
    val confirmBtnType: ButtonType? = null,
    val confirmBtnClicked: () -> Unit = {},
)

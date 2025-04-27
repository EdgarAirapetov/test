package com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.model

import android.net.Uri
import androidx.annotation.StringRes

sealed class MeeraPickerUiEffect {

    data class ShowMediaAlert(@StringRes val messageRes: Int) : MeeraPickerUiEffect()

    data class ShowTooLongVideoAlert(val uri: Uri) : MeeraPickerUiEffect()

    data object UpdateListTiles : MeeraPickerUiEffect()
}

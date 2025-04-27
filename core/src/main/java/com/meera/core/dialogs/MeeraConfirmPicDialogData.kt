package com.meera.core.dialogs

import androidx.core.content.res.ResourcesCompat
import com.meera.core.extensions.empty
import com.meera.uikit.widgets.buttons.ButtonType

data class MeeraConfirmPicDialogData(
    val header: String = String.empty(),
    val headerRes: Int = ResourcesCompat.ID_NULL,
    val descriptionFirst: String = String.empty(),
    val descriptionFirstRes: Int = ResourcesCompat.ID_NULL,
    val descriptionSecond: String = String.empty(),
    val descriptionSecondRes: Int = ResourcesCompat.ID_NULL,
    val image: Int = ResourcesCompat.ID_NULL,
    val topBtnText: String = String.empty(),
    val topBtnTextRes: Int = ResourcesCompat.ID_NULL,
    val topBtnType: ButtonType? = null,
    val bottomBtnText: String = String.empty(),
    val bottomBtnTextRes: Int = ResourcesCompat.ID_NULL,
    val topBtnClicked: () -> Unit = {},
    val bottomBtnClicked: () -> Unit = {},
    val dialogCancelled: () -> Unit = {},
)

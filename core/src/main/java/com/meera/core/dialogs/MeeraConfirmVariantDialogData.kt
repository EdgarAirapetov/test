package com.meera.core.dialogs

import androidx.core.content.res.ResourcesCompat
import com.meera.core.extensions.empty

data class MeeraConfirmVariantDialogData(
    val header: String = String.empty(),
    val headerRes: Int = ResourcesCompat.ID_NULL,
    val description: String = String.empty(),
    val descriptionRes: Int = ResourcesCompat.ID_NULL,
    val firstCellText: String = String.empty(),
    val firstCellTextRes: Int = ResourcesCompat.ID_NULL,
    val firstCellIconRes: Int = ResourcesCompat.ID_NULL,
    val secondCellText: String = String.empty(),
    val secondCellTextRes: Int = ResourcesCompat.ID_NULL,
    val secondCellIconRes: Int = ResourcesCompat.ID_NULL,
    val thirdCellText: String = String.empty(),
    val thirdCellTextRes: Int = ResourcesCompat.ID_NULL,
    val thirdCellIconRes: Int = ResourcesCompat.ID_NULL,
    val selectOption: MeeraConfirmVariantType? = null,
    val variantCellClicked: (value: MeeraConfirmVariantType) -> Unit = {},
    val dialogCancelled: () -> Unit = {},
    val dialogBack: (() -> Unit)? = null,
)

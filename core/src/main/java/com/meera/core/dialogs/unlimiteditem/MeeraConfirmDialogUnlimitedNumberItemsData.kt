package com.meera.core.dialogs.unlimiteditem

import androidx.core.content.res.ResourcesCompat


data class MeeraConfirmDialogUnlimitedNumberData(
    val header: Int = ResourcesCompat.ID_NULL,
    val items: List<MeeraConfirmDialogUnlimitedNumberItemsData> = listOf(),
    val itemsWithMargins: Boolean = true,
    val listener: (action: MeeraConfirmDialogUnlimitedNumberItemsAction) -> Unit = {},
    val dismiss: (() -> Unit)? = null
)

data class MeeraConfirmDialogUnlimitedNumberItemsData(
    val name: Int,
    val icon: Int,
    val contentColor: Int? = null,
    val action: MeeraConfirmDialogUnlimitedNumberItemsAction
)

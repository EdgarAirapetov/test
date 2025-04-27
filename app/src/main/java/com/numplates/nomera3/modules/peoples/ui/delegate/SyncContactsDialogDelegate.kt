package com.numplates.nomera3.modules.peoples.ui.delegate

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.meera.core.utils.IS_APP_REDESIGNED
import com.numplates.nomera3.modules.peoples.ui.dialog.ContactsSyncBottomSheetDialog
import com.numplates.nomera3.modules.peoples.ui.dialog.MeeraContactsSyncBottomSheetDialog

class SyncContactsDialogDelegate(
    private val fragmentManager: FragmentManager
) {

    fun showSyncContactsDialog(
        @StringRes labelRes: Int,
        @StringRes descriptionRes: Int,
        @StringRes positiveButtonRes: Int? = null,
        positiveButtonAction: (() -> Unit)? = null,
        @DrawableRes iconRes: Int,
        closeDialogDismissListener: (() -> Unit)? = null,
        @StringRes negativeButtonRes: Int? = null,
        negativeButtonAction: (() -> Unit)? = null,
        isAppRedesigned: Boolean = IS_APP_REDESIGNED
    ) {
        if (isAppRedesigned) {
            val builder = MeeraContactsSyncBottomSheetDialog.Builder()
                .setLabel(labelRes)
                .setDescription(descriptionRes)
                .setIcon(iconRes)

            positiveButtonRes?.let { builder.setPositiveButton(positiveButtonRes, positiveButtonAction) }
            negativeButtonRes?.let { builder.setNegativeButton(negativeButtonRes, negativeButtonAction) }
            closeDialogDismissListener?.let { builder.setOnCloseDialogDismissListener(closeDialogDismissListener) }
            builder.createAndShow(fragmentManager)
        } else {
            val builder = ContactsSyncBottomSheetDialog.Builder()
                .setLabel(labelRes)
                .setDescription(descriptionRes)
                .setIcon(iconRes)

            positiveButtonRes?.let { builder.setPositiveButton(positiveButtonRes, positiveButtonAction) }
            negativeButtonRes?.let { builder.setNegativeButton(negativeButtonRes, negativeButtonAction) }
            closeDialogDismissListener?.let { builder.setOnCloseDialogDismissListener(closeDialogDismissListener) }
            builder.createAndShow(fragmentManager)
        }
    }

}

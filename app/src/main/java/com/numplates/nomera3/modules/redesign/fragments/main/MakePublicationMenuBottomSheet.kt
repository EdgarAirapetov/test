package com.numplates.nomera3.modules.redesign.fragments.main

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraPublicationMenuDialogBinding

class MakePublicationMenuBottomSheet : UiKitBottomSheetDialog<MeeraPublicationMenuDialogBinding>() {

    private var actionListener: ((PublicationType) -> Unit)? = null
    private var hideDismissListener: ((dialog: DialogInterface) -> Unit)? = null

    fun setActionListener(listener: (PublicationType) -> Unit) {
        actionListener = listener
    }

    fun setHideDismissListener(listener: (dialog: DialogInterface) -> Unit) {
        hideDismissListener = listener
    }


    override fun onCancel(dialog: DialogInterface) {
        hideDismissListener?.invoke(dialog)
        super.onCancel(dialog)
    }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraPublicationMenuDialogBinding
        get() = MeeraPublicationMenuDialogBinding::inflate

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.moments_carousel_create))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        contentBinding?.apply {
            cellCreatePost.setThrottledClickListener {
                actionListener?.invoke(PublicationType.POST)
                dismiss()
            }
            cellCreateMoment.setThrottledClickListener {
                actionListener?.invoke(PublicationType.MOMENT)
                dismiss()
            }
            cellCreateEvent.setThrottledClickListener {
                actionListener?.invoke(PublicationType.EVENT)
                dismiss()
            }
        }
    }

    companion object {
        enum class PublicationType {
            POST,
            MOMENT,
            EVENT
        }
    }
}

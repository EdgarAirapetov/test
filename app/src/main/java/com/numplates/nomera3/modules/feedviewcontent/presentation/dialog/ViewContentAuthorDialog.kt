package com.numplates.nomera3.modules.feedviewcontent.presentation.dialog

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

const val AUTHOR_DIALOG_TAG = "view_content_author_dialog"

class ViewContentAuthorDialog(
    val activityContext: Context?
) : MeeraMenuBottomSheet(activityContext) {

    private var listener: ContentBottomSheetDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ContentBottomSheetDialogListener) {
            listener = parentFragment as? ContentBottomSheetDialogListener
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    fun createAuthorDialog() {
        addItem(
            title = R.string.save_to_device,
            icon = R.drawable.ic_download_new
        ) { listener?.onClickDownloadContent() }
    }

    companion object {

        fun ViewContentAuthorDialog.showDialog(fragmentManager: FragmentManager) {
            showWithTag(fragmentManager, AUTHOR_DIALOG_TAG)
        }
    }
}

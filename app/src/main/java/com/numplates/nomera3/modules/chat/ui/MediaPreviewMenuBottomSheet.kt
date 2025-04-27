package com.numplates.nomera3.modules.chat.ui

import android.content.Context
import android.view.WindowManager
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

class MediaPreviewMenuBottomSheet(
    activityContext: Context?
) : MeeraMenuBottomSheet(activityContext) {

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

}

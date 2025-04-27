package com.numplates.nomera3.modules.redesign.dialog

import android.view.View
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.numplates.nomera3.R


// Использовать для независимых экранов без переходов
open class MeeraBaseBottomSheet : BottomSheetDialogFragment() {

    override fun onStart() {
        super.onStart()

        (dialog as BottomSheetDialog?)?.findViewById<View>(R.id.design_bottom_sheet)?.let { bottomSheet ->
            // set the bottom sheet height to match_parent
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            bottomSheet.layoutParams = layoutParams
        }
    }
}

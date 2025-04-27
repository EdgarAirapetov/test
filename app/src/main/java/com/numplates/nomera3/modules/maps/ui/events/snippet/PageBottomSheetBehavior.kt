package com.numplates.nomera3.modules.maps.ui.events.snippet

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PageBottomSheetBehavior<V : View>(context: Context) : BottomSheetBehavior<V>(context, null) {
    override fun onRestoreInstanceState(parent: CoordinatorLayout, child: V, state: Parcelable) = Unit
}

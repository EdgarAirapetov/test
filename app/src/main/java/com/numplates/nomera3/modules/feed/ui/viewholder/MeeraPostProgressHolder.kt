package com.numplates.nomera3.modules.feed.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.ui.MeeraLoaderView

class MeeraPostProgressHolder(private val view: View): RecyclerView.ViewHolder(view) {

    private val loaderView: MeeraLoaderView? = view.findViewById(R.id.lv_progress_view)

    fun bind() {
       loaderView?.show()
    }
}

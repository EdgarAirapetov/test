package com.numplates.nomera3.modules.comments.ui.viewholder

import android.view.ViewGroup
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.notifications.ui.viewholder.BaseViewHolder
import com.numplates.nomera3.presentation.view.ui.MeeraLoaderView

class MeeraCommentProgressViewHolder(
    viewGroup: ViewGroup
) : BaseViewHolder(viewGroup, R.layout.item_comment_progress) {

    private val loaderView: MeeraLoaderView = itemView.findViewById(R.id.lv_progress_indicator)

    fun bind() {
        loaderView.show()
    }
}

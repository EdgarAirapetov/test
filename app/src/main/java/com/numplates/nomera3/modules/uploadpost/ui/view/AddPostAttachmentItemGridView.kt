package com.numplates.nomera3.modules.uploadpost.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemAddPostAttachmentGridBinding

class AddPostAttachmentItemGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.item_add_post_attachment_grid, this, false)
            .apply(::addView)
            .let(ItemAddPostAttachmentGridBinding::bind)
    }
}

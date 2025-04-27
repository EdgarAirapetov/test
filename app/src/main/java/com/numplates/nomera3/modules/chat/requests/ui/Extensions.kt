package com.numplates.nomera3.modules.chat.requests.ui

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.HorizontalLineDivider

private const val HORIZONTAL_PADDING = 16

fun RecyclerView.addDividerDecorator() {
    val drawable = ContextCompat.getDrawable(
        context, R.drawable.drawable_friend_list_divider_decoration
    ) ?: return
    addItemDecoration(
        HorizontalLineDivider(dividerDrawable = drawable, horizontalPaddingDp = HORIZONTAL_PADDING.dp)
    )
}

fun RecyclerView.addDividerDecoratorLeftPadding(value: Int = HORIZONTAL_PADDING) {
    val drawable = ContextCompat.getDrawable(
        context, R.drawable.drawable_friend_list_divider_decoration
    ) ?: return
    addItemDecoration(
        HorizontalLineDivider(dividerDrawable = drawable, paddingLeft = value.dp)
    )
}

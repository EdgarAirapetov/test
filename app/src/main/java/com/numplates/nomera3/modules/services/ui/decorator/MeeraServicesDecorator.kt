package com.numplates.nomera3.modules.services.ui.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesContentType

private const val USER_MARGIN = 16
private const val BUTTONS_MARGIN = 32
private const val RECENT_USERS_MARGIN = 20
private const val RECOMMENDED_PEOPLE_MARGIN = 8
private const val COMMUNITIES_MARGIN = 12
private const val BOTTOM_MARGIN = 148

class MeeraServicesDecorator : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        when (parent.adapter?.getItemViewType(position)) {
            MeeraServicesContentType.USER.ordinal -> {
                outRect.top = USER_MARGIN.dp
            }
            MeeraServicesContentType.BUTTONS.ordinal -> {
                outRect.top = BUTTONS_MARGIN.dp
            }
            MeeraServicesContentType.RECENT_USERS.ordinal -> {
                outRect.top = RECENT_USERS_MARGIN.dp
            }
            MeeraServicesContentType.RECOMMENDED_PEOPLE.ordinal -> {
                outRect.top = RECOMMENDED_PEOPLE_MARGIN.dp
            }
            MeeraServicesContentType.COMMUNITIES.ordinal -> {
                outRect.top = COMMUNITIES_MARGIN.dp
                outRect.bottom = BOTTOM_MARGIN.dp
            }
            MeeraServicesContentType.COMMUNITIES_PLACEHOLDER.ordinal -> {
                outRect.top = COMMUNITIES_MARGIN.dp
                outRect.bottom = BOTTOM_MARGIN.dp
            }
        }
    }

}

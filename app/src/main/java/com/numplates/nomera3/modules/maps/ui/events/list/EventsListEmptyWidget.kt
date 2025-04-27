package com.numplates.nomera3.modules.maps.ui.events.list

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventsListEmptyBinding
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListEmptyUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction

class EventsListEmptyWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): LinearLayout(context, attrs, defStyle) {

    var uiActionListener: ((MapUiAction.EventsListUiAction) -> Unit)? = null

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_events_list_empty, this)
        .let(ViewEventsListEmptyBinding::bind)

    init {
        orientation = VERTICAL
        this.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
    }

    fun setUiModel(uiModel: EventsListEmptyUiModel) {
        binding.tvEventsListEmptyTitle.text = uiModel.title
        binding.tvEventsListEmptyMessage.text = uiModel.message
        binding.tvEventsListEmptyAction.text = uiModel.actionText
        if (uiModel.action != null) {
            binding.tvEventsListEmptyAction.setThrottledClickListener {
                val action = if(uiModel.action == MapUiAction.EventsListUiAction.ShowNearbyPage) {
                    MapUiAction.EventsListUiAction.ShowNearbyPageWithRefresh
                } else {
                    uiModel.action
                }
                uiActionListener?.invoke(action)
            }
        } else {
            binding.tvEventsListEmptyAction.setOnClickListener(null)
        }
    }
}

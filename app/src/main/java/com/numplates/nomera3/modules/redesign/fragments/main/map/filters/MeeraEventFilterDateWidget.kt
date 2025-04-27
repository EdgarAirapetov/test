package com.numplates.nomera3.modules.redesign.fragments.main.map.filters

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.meera.uikit.widgets.radio_buttons.UiKitCheckable
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewEventFilterDateBinding
import com.numplates.nomera3.modules.maps.domain.events.model.FilterEventDate
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterDateUiModel

class MeeraEventFilterDateWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): ConstraintLayout(context, attrs, defStyle) {

    var filterChangeListener: ((EventFilterDateUiModel) -> Unit)? = null

    private val viewIdToEventDateMap = mapOf(
        R.id.ukccv_event_filters_event_date_all to FilterEventDate.ALL,
        R.id.ukccv_event_filters_event_date_today to FilterEventDate.TODAY,
        R.id.ukccv_event_filters_event_date_tomorrow to FilterEventDate.TOMORROW,
        R.id.ukccv_event_filters_event_date_this_week to FilterEventDate.THIS_WEEK
    )
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_event_filter_date, this, true)
        .let(MeeraViewEventFilterDateBinding::bind)
    private var uiModel: EventFilterDateUiModel = getDefaultUiModel()

    init {
        setUiModel(uiModel)
        binding.vgMapFiltersEventDates.children.forEach { eventDateView ->
            eventDateView.setOnClickListener {
                updateSelectedEventDate(eventDateView.id)
            }
        }
    }

    fun setUiModel(uiModel: EventFilterDateUiModel) {
        this.uiModel = uiModel
        val selectedEventDateViewId = mapSelectedEventDateViewId(uiModel.selectedFilterEventDate)
        binding.vgMapFiltersEventDates.children.forEach { eventDateView ->
            (eventDateView as? UiKitCheckable)?.checked = eventDateView.id == selectedEventDateViewId
        }
    }

    fun clear() = updateUiModel(getDefaultUiModel())

    fun getUiModel(): EventFilterDateUiModel = uiModel

    fun isDefault(): Boolean = uiModel == getDefaultUiModel()

    private fun updateSelectedEventDate(@IdRes selectedEventDateViewId: Int) {
        val updatedUiModel = uiModel.copy(selectedFilterEventDate = mapUpdatedEventDate(selectedEventDateViewId))
        updateUiModel(updatedUiModel)
    }

    private fun updateUiModel(updatedUiModel: EventFilterDateUiModel) {
        if (updatedUiModel != uiModel) {
            setUiModel(updatedUiModel)
            filterChangeListener?.invoke(updatedUiModel)
        }
    }

    private fun mapUpdatedEventDate(@IdRes selectedEventDateViewId: Int): FilterEventDate =
        viewIdToEventDateMap.getOrDefault(selectedEventDateViewId, FilterEventDate.ALL)

    @IdRes
    private fun mapSelectedEventDateViewId(filterEventDate: FilterEventDate): Int =
        viewIdToEventDateMap.entries
            .associate { it.value to it.key }
            .getOrDefault(filterEventDate, R.id.ukccv_event_filters_event_date_all)

    private fun getDefaultUiModel(): EventFilterDateUiModel = EventFilterDateUiModel(FilterEventDate.ALL)
}

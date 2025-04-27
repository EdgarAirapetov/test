package com.numplates.nomera3.modules.maps.ui.events.list.filters

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.meera.uikit.widgets.radio_buttons.UiKitCheckable
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventFilterTypeBinding
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterTypeUiModel

class EventFilterTypeWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): ConstraintLayout(context, attrs, defStyle) {

    var filterChangeListener: ((EventFilterTypeUiModel) -> Unit)? = null

    private val viewIdToEventTypeMap = mapOf(
        R.id.ukccv_event_filters_event_type_education to EventType.EDUCATION,
        R.id.ukccv_event_filters_event_type_art to EventType.ART,
        R.id.ukccv_event_filters_event_type_concert to EventType.CONCERT,
        R.id.ukccv_event_filters_event_type_sport to EventType.SPORT,
        R.id.ukccv_event_filters_event_type_tourism to EventType.TOURISM,
        R.id.ukccv_event_filters_event_type_games to EventType.GAMES,
        R.id.ukccv_event_filters_event_type_party to EventType.PARTY
    )
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_event_filter_type, this, true)
        .let(ViewEventFilterTypeBinding::bind)
    private var uiModel: EventFilterTypeUiModel = getDefaultUiModel()

    init {
        setUiModel(uiModel)
        binding.vgMapFiltersEventTypes.children.forEach { eventTypeView ->
            eventTypeView.setOnClickListener {
                updateSelectedEventType(eventTypeView.id)
            }
        }
    }

    fun clear() = updateUiModel(getDefaultUiModel())

    fun getUiModel(): EventFilterTypeUiModel = uiModel

    fun isDefault(): Boolean = uiModel == getDefaultUiModel()

    fun setUiModel(uiModel: EventFilterTypeUiModel) {
        this.uiModel = uiModel
        val selectedEventTypeViewIds = mapSelectedEventTypeViewIds(uiModel.selectedEventTypes)
        binding.vgMapFiltersEventTypes.children.forEach { eventTypeView ->
            (eventTypeView as? UiKitCheckable)?.checked = eventTypeView.id in selectedEventTypeViewIds
        }
    }

    private fun updateSelectedEventType(@IdRes selectedEventTypeViewId: Int) {
        val updatedUiModel = uiModel.copy(selectedEventTypes = mapUpdatedEventTypes(selectedEventTypeViewId))
        updateUiModel(updatedUiModel)
    }

    private fun updateUiModel(updatedUiModel: EventFilterTypeUiModel) {
        if (updatedUiModel != uiModel) {
            setUiModel(updatedUiModel)
            filterChangeListener?.invoke(updatedUiModel)
        }
    }

    private fun mapUpdatedEventTypes(
        @IdRes selectedEventTypeViewId: Int
    ): List<EventType> = when (val eventType = viewIdToEventTypeMap[selectedEventTypeViewId]) {
        null -> listOf()
        in uiModel.selectedEventTypes -> uiModel.selectedEventTypes.minus(eventType)
        else -> uiModel.selectedEventTypes.plus(eventType)
    }

    @IdRes
    private fun mapSelectedEventTypeViewIds(selectedEventTypes: List<EventType>): List<Int> = when {
        selectedEventTypes.isEmpty() -> listOf(R.id.ukccv_event_filters_event_type_all)
        else -> {
            val eventTypeToViewIdMap = viewIdToEventTypeMap.entries.associate { it.value to it.key }
            selectedEventTypes.mapNotNull { eventTypeToViewIdMap[it] }
        }
    }

    private fun getDefaultUiModel(): EventFilterTypeUiModel = EventFilterTypeUiModel(emptyList())
}

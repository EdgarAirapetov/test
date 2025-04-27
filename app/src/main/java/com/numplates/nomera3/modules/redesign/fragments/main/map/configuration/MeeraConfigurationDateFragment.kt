package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentConfigurationDateBinding
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationEvent
import com.numplates.nomera3.modules.maps.ui.events.model.EventDateItemUiModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.configuration.adapter.MeeraEventDateAdapter

private const val COUNT_DATA_COLUMNS = 3

class MeeraConfigurationDateFragment : MeeraBaseFragment(R.layout.fragment_configuration_date) {

    private var onEvent: ((EventConfigurationEvent) -> Unit)? = null
    private var eventDateAdapter: MeeraEventDateAdapter? = null

    private val binding by viewBinding(FragmentConfigurationDateBinding::bind)

    private var isFirstStart = true

    private val viewModel: MeeraMapViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvMapEventsConfigurationDates.apply {
            eventDateAdapter = MeeraEventDateAdapter { item ->
//                viewModel.eventsOnMap.setSelectedEventDate(item)
                onEvent?.invoke(EventConfigurationEvent.EventDateItemSelected(item))
            }
            layoutManager = GridLayoutManager(context, COUNT_DATA_COLUMNS)
            adapter = eventDateAdapter
            itemAnimator = null
        }
    }

    fun setCallback(onEvent: ((EventConfigurationEvent) -> Unit)?) {
        this.onEvent = onEvent
    }

    fun submitList(eventTypeItems: List<EventDateItemUiModel>) {
        eventDateAdapter?.submitList(eventTypeItems)
        if (isFirstStart) {
            eventTypeItems.firstOrNull()?.let {
                viewModel.eventsOnMap.setSelectedEventDate(it)
            }
            isFirstStart = false
        }
    }
}

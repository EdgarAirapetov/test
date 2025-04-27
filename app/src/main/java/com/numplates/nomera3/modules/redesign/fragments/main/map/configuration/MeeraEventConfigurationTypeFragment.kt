package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentEventConfigurationTypeBinding
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationEvent
import com.numplates.nomera3.modules.maps.ui.events.model.EventTypeItemUiModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.events.adapter.MeeraEventTypeAdapter

private const val COUNT_CATEGORY_COLUMNS = 3

class MeeraEventConfigurationTypeFragment : MeeraBaseFragment(R.layout.fragment_event_configuration_type) {


    private var onEvent: ((EventConfigurationEvent) -> Unit)? = null

    private var eventTypeAdapter: MeeraEventTypeAdapter? = null

    private val binding by viewBinding(FragmentEventConfigurationTypeBinding::bind)

    private val viewModel: MeeraMapViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventTypeAdapter = MeeraEventTypeAdapter(isOnboarding = false) { item ->
            viewModel.eventsOnMap.setSelectedEventType(item)
            onEvent?.invoke(EventConfigurationEvent.EventTypeItemSelected(item))
        }
        binding.rvMapEventsConfigurationTypes.apply {
            layoutManager = GridLayoutManager(context, COUNT_CATEGORY_COLUMNS)
            adapter = eventTypeAdapter
            itemAnimator = null
        }
    }

    fun setCallback(onEvent: ((EventConfigurationEvent) -> Unit)?) {
        this.onEvent = onEvent
    }

    fun submitList(eventTypeItems: List<EventTypeItemUiModel>) {
        eventTypeAdapter?.submitList(eventTypeItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onEvent = null
    }
}

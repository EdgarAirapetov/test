package com.numplates.nomera3.modules.redesign.fragments.main.map.events

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.model.MapDialogFragmentUiModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraMapDialogFragment


class MeeraCreateEventStubDialogFragment : MeeraMapDialogFragment() {

    override fun getUiModel(): MapDialogFragmentUiModel =
        MapDialogFragmentUiModel(
            titleResId = R.string.map_event_create_stub_title,
            messageResId = R.string.map_event_create_stub_message,
            actionResId = R.string.map_event_create_stub_action,
            imageResId = R.drawable.img_map_events_stub
        )
}

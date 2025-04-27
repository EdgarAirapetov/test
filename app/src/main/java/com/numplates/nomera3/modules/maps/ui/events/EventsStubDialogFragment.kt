package com.numplates.nomera3.modules.maps.ui.events

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.MapDialogFragment
import com.numplates.nomera3.modules.maps.ui.model.MapDialogFragmentUiModel


class EventsStubDialogFragment : MapDialogFragment() {

    override fun getUiModel(): MapDialogFragmentUiModel =
        MapDialogFragmentUiModel(
            titleResId = R.string.map_events_stub_title,
            messageResId = R.string.map_events_stub_message,
            actionResId = R.string.map_events_stub_action,
            imageResId = R.drawable.img_map_events_stub
        )
}

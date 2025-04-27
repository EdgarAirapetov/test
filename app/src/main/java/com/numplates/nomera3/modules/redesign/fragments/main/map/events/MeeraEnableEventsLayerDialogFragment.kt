
package com.numplates.nomera3.modules.redesign.fragments.main.map.events

import android.os.Bundle
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.layers.model.EnableEventsDialogConfirmAction
import com.numplates.nomera3.modules.maps.ui.model.MapDialogFragmentUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraMapDialogFragment


class MeeraEnableEventsLayerDialogFragment : MeeraMapDialogFragment() {

    override fun getUiModel(): MapDialogFragmentUiModel =
        MapDialogFragmentUiModel(
            titleResId = R.string.map_layers_enable_events_layer_title,
            messageResId = R.string.map_layers_enable_events_layer_message,
            actionResId = R.string.map_layers_enable_events_layer_action,
            imageResId = R.drawable.img_map_events_stub
        )

    override fun getConfirmMapUiAction(confirmed: Boolean): MapUiAction? =
        (arguments?.getSerializable(KEY_CONFIRM_ACTION) as? EnableEventsDialogConfirmAction)?.let { confirmAction ->
            MapUiAction.EnableEventsLayerDialogClosed(
                enableLayerRequested = confirmed,
                confirmAction = confirmAction
            )
        }

    companion object {
        fun newInstance(action: EnableEventsDialogConfirmAction): MeeraEnableEventsLayerDialogFragment =
            MeeraEnableEventsLayerDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_CONFIRM_ACTION, action)
                }
            }
        private const val KEY_CONFIRM_ACTION = "KEY_CONFIRM_ACTION"
    }
}

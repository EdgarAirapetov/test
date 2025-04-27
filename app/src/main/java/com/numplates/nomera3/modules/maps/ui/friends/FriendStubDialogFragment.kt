package com.numplates.nomera3.modules.maps.ui.friends

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.MapDialogFragment
import com.numplates.nomera3.modules.maps.ui.model.MapDialogFragmentUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.redesign.fragments.main.map.MainMapFragment
import com.numplates.nomera3.presentation.view.fragments.MapFragment

class FriendStubDialogFragment : MapDialogFragment() {

    override fun getUiModel(): MapDialogFragmentUiModel =
        MapDialogFragmentUiModel(
            titleResId = R.string.settings_friends,
            messageResId = R.string.map_friends_stub_subtitle_message,
            actionResId = R.string.map_friend_stub_action,
            imageResId = R.drawable.img_map_friends_stub
        )

    override fun getConfirmMapUiAction(confirmed: Boolean): MapUiAction {
        if (confirmed) {
            (parentFragment as? MapFragment)?.enableFriendLayer()
            (parentFragment as? MainMapFragment)?.enableFriendLayer()
        }
        return MapUiAction.MapDialogClosed
    }
}

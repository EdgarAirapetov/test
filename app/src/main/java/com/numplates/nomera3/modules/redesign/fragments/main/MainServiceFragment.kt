package com.numplates.nomera3.modules.redesign.fragments.main

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState

class MainServiceFragment :
    MeeraBaseDialogFragment(
        R.layout.meera_main_service_fragment,
        behaviourConfigState = ScreenBehaviourState.ScrollableFull()
    ) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view
}

package com.numplates.nomera3.modules.redesign.fragments.secondary

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState

class MeeraBottomInformationFragment :
    MeeraBaseDialogFragment(
        R.layout.meera_bottom_information_fragment,
        behaviourConfigState = ScreenBehaviourState.BottomScreens()
    ) {

    override val containerId: Int
        get() = R.id.fragment_second_container_view
}

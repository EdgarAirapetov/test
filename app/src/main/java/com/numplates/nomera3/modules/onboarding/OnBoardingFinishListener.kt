package com.numplates.nomera3.modules.onboarding

import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode

interface OnBoardingFinishListener {
    fun onBoardingFinished()
    fun onRegistrationFinished()
    fun getRoadMode() : MainRoadMode?
}

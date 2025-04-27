package com.numplates.nomera3.modules.baseCore.helper.amplitude.onboarding

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeOnBoarding {
    fun onEnterClicked(eventName: AmplitudeOnBoardingEventName, afterContinue: Boolean)
    fun onCloseBtnClicked(eventName: AmplitudeOnBoardingEventName, afterContinue: Boolean)
    fun onDownSwiped(eventName: AmplitudeOnBoardingEventName, afterContinue: Boolean)
    fun onAfterClicked(afterContinue: Boolean)
    fun onNextClicked(eventName: AmplitudeOnBoardingEventName, afterContinue: Boolean)
    fun onSwiped(eventName: AmplitudeOnBoardingEventName, afterContinue: Boolean)
    fun onContinueClicked()
}

class AmplitudeHelperOnBoardingImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeOnBoarding {

    override fun onEnterClicked(eventName: AmplitudeOnBoardingEventName, afterContinue: Boolean) {
        delegate.logEvent(
            eventName = eventName,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyOnBoardingActionType.ENTER)
                    addProperty(AmplitudePropertyOnBoardingConst.CONTINUE_CLICKED, afterContinue)
                }
            }
        )
    }

    override fun onCloseBtnClicked(
        eventName: AmplitudeOnBoardingEventName,
        afterContinue: Boolean
    ) {
        delegate.logEvent(
            eventName = eventName,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyOnBoardingActionType.CLOSE)
                    addProperty(AmplitudePropertyOnBoardingConst.CONTINUE_CLICKED, afterContinue)
                }
            }
        )
    }

    override fun onDownSwiped(eventName: AmplitudeOnBoardingEventName, afterContinue: Boolean) {
        delegate.logEvent(
            eventName = eventName,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyOnBoardingActionType.CLOSE_SWIPE)
                    addProperty(AmplitudePropertyOnBoardingConst.CONTINUE_CLICKED, afterContinue)
                }
            }
        )
    }

    override fun onAfterClicked(afterContinue: Boolean) {
        delegate.logEvent(
            eventName = AmplitudeOnBoardingEventName.JOIN,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyOnBoardingActionType.AFTER)
                    addProperty(AmplitudePropertyOnBoardingConst.CONTINUE_CLICKED, afterContinue)
                }
            }
        )
    }

    override fun onNextClicked(
        eventName: AmplitudeOnBoardingEventName,
        afterContinue: Boolean
    ) {
        delegate.logEvent(
            eventName = eventName,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyOnBoardingActionType.NEXT)
                    addProperty(AmplitudePropertyOnBoardingConst.CONTINUE_CLICKED, afterContinue)
                }
            }
        )
    }

    override fun onSwiped(eventName: AmplitudeOnBoardingEventName, afterContinue: Boolean) {
        delegate.logEvent(
            eventName = eventName,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyOnBoardingActionType.SWIPE)
                    addProperty(AmplitudePropertyOnBoardingConst.CONTINUE_CLICKED, afterContinue)
                }
            }
        )
    }

    override fun onContinueClicked() =
        delegate.logEvent(eventName = AmplitudeOnBoardingEventName.CONTINUE)
}

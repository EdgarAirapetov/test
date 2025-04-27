package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.domain.interactornew.CheckSwipeDownToShowChatSearchTooltipRequiredUseCase
import com.numplates.nomera3.domain.interactornew.ConfirmSwipeDownToShowChatTooltipShownUseCase
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RoomsContainerEvents {
    object ShowSwipeToShowChatSearchEvent : RoomsContainerEvents
}

sealed interface RoomsContainerAction {
    object CheckSwipeDownToShowChatSearch : RoomsContainerAction
    object ConfirmSwipeDownToShowChatTooltip : RoomsContainerAction
    data class LogScreenForFragment(val screenName: String) : RoomsContainerAction
}

class RoomsContainerViewModel @Inject constructor(
    private val fbAnalytic: FireBaseAnalytics,
    private val checkSwipeDownToShowChatSearchTooltipRequiredUseCase: CheckSwipeDownToShowChatSearchTooltipRequiredUseCase,
    private val confirmSwipeDownToShowChatTooltipShownUseCase: ConfirmSwipeDownToShowChatTooltipShownUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer,
) : ViewModel() {

    private val _roomsEvents = MutableSharedFlow<RoomsContainerEvents>()
    val roomsEvents = _roomsEvents.asSharedFlow()

    fun handleViewAction(action: RoomsContainerAction) {
        when (action) {
            is RoomsContainerAction.CheckSwipeDownToShowChatSearch -> checkSwipeDownToShowChatSearchTooltip()
            is RoomsContainerAction.ConfirmSwipeDownToShowChatTooltip -> confirmSwipeDownToShowChatTooltip()
            is RoomsContainerAction.LogScreenForFragment -> logScreenForFragment(action.screenName)
        }
    }

    private fun checkSwipeDownToShowChatSearchTooltip() {
        val isChatSearchEnabled = featureTogglesContainer.chatSearchFeatureToggle.isEnabled
        val isTooltipRequired = checkSwipeDownToShowChatSearchTooltipRequiredUseCase.invoke()
        if (isTooltipRequired && isChatSearchEnabled) {
            viewModelScope.launch {
                _roomsEvents.emit(RoomsContainerEvents.ShowSwipeToShowChatSearchEvent)
            }
        }
    }

    private fun confirmSwipeDownToShowChatTooltip() {
        confirmSwipeDownToShowChatTooltipShownUseCase.invoke()
    }

    private fun logScreenForFragment(screenName: String) = fbAnalytic.logScreenForFragment(screenName)
}

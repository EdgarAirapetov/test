package com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.data.websocket.STATUS_OK
import com.numplates.nomera3.modules.feed.domain.usecase.SetSubscriptionIndicatorIncludeGroupUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.phoenixframework.Message
import javax.inject.Inject

class RoadFilterSubscriptionsViewModel : BaseViewModel() {

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var setSubscriptionIndicatorIncludeGroupUseCase: SetSubscriptionIndicatorIncludeGroupUseCase

    private var job: Job? = null

    val isGroupSubscribedState = MutableLiveData<Boolean>()

    val event = SingleLiveEvent<RoadFilterSubscriptionEvent>()

    init {
        App.component.inject(this)
        isGroupSubscribedState.value = getCurrentFilterMyGroups()
    }

    fun setFilterMyGroups(isEnabled: Boolean) {
        val oldState = isGroupSubscribedState.value

        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            appSettings.writeMyGroupsFilter(isEnabled)
            val message = setSubscriptionIndicatorIncludeGroupUseCase.execute(isEnabled)
            withContext(Dispatchers.Main) {
                val resultState = if (message.isSuccess()) {
                    isEnabled
                } else {
                    event.value = RoadFilterSubscriptionEvent.Error()

                    oldState ?: getCurrentFilterMyGroups()
                }

                isGroupSubscribedState.value = resultState
            }
        }
    }

    private fun Message?.isSuccess(): Boolean {
        return this?.status == STATUS_OK
    }

    fun getCurrentFilterMyGroups() = appSettings.readMyGroupsFilter()

}

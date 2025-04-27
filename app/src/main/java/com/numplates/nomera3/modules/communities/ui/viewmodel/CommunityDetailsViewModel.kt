package com.numplates.nomera3.modules.communities.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCaseParams
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityViewEvent
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommunityDetailsViewModel : ViewModel() {

    @Inject
    lateinit var getCommunityInfoUseCase: GetCommunityInformationUseCase

    val liveViewEvent = MutableLiveData<CommunityViewEvent>()

    init {
        App.component.inject(this)
    }

    fun getData(groupId: Int?) {
        groupId?.let { id ->
            getCommunityInfo(id)
        }
    }

    private fun getCommunityInfo(groupId: Int) {
        Timber.d("Get community INFO: grId: $groupId")
        viewModelScope.launch {
            getCommunityInfoUseCase.execute(
                params = GetCommunityInformationUseCaseParams(groupId),
                success = {
                    Timber.d("RESPONSE Get community INFO: ${Gson().toJson(it)}")
                    val data = it?.community
                    if (data != null) {
                        liveViewEvent.value = CommunityViewEvent.CommunityData(data, null)
                    } else {
                        liveViewEvent.value = CommunityViewEvent.FailureGetCommunityInfo
                    }
                },
                fail = {
                    Timber.e("ERROR: get community INFO: ${it.localizedMessage}")
                    it.printStackTrace()
                    liveViewEvent.value = CommunityViewEvent.FailureGetCommunityInfo
                }
            )
        }
    }
}
package com.numplates.nomera3.modules.peoples.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.numplates.nomera3.modules.peoples.ui.entity.PeoplesCommunitiesContainerState

class PeoplesCommunitiesContainerViewModel : ViewModel() {

    private val _fragmentContainerState = MutableLiveData<PeoplesCommunitiesContainerState>()
    val fragmentContainerState: LiveData<PeoplesCommunitiesContainerState> = _fragmentContainerState

    init {
        _fragmentContainerState.value = PeoplesCommunitiesContainerState.PeoplesState
    }

    fun setPeopleScreenState() {
        _fragmentContainerState.postValue(PeoplesCommunitiesContainerState.PeoplesState)
    }

    fun setCommunityState() {
        _fragmentContainerState.postValue(PeoplesCommunitiesContainerState.CommunitiesState)
    }
}

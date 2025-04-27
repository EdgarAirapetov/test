package com.numplates.nomera3.modules.tags.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.domain.mappers.toUITagEntity
import com.numplates.nomera3.modules.tags.domain.usecase.SearchByUniqueNameUseCase
import com.numplates.nomera3.modules.tags.domain.usecase.SearchUniqueNameParams
import com.numplates.nomera3.modules.tags.ui.TagViewEvent
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

// todo will be replaced by TagMenuViewModelNew
class TagMenuViewModel : BaseViewModel() {

    @Inject
    lateinit var searchByUniqueName: SearchByUniqueNameUseCase

    val liveTags: MutableLiveData<List<UITagEntity>> = MutableLiveData()
    val liveViewEventsTagMenu: MutableLiveData<TagViewEvent> = MutableLiveData()

    init {
        App.component.inject(this)
    }

    fun getTags(
            text: String,
            type: SuggestionsMenuType = SuggestionsMenuType.ROAD
    ) {
        if (type == SuggestionsMenuType.ROAD) {
            requestRoadTags(text)
        }
    }

    private fun requestRoadTags(text: String) {
        if (text.isEmpty()) {
            liveViewEventsTagMenu.postValue(TagViewEvent.HideMenu)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val searchParams = SearchUniqueNameParams(
                    query = text
            )

            searchByUniqueName.execute(
                    params = searchParams,
                    success = {
                        val res = mutableListOf<UITagEntity>()
                        it.users?.forEach { userSimple -> res.add(userSimple.toUITagEntity()) }
                        if (res.isNotEmpty()) liveTags.postValue(res)
                        else liveViewEventsTagMenu.postValue(TagViewEvent.HideMenu)
                    },
                    fail = { liveViewEventsTagMenu.postValue(TagViewEvent.HideMenu) }
            )
        }
    }

    fun getUniqueNameSuggestionsInGroupChat(
            text: String,
            chatRoomId: Int,
            type: SuggestionsMenuType = SuggestionsMenuType.ROAD
    ) {
        if (type == SuggestionsMenuType.ROAD) {
            requestRoadTags(text, chatRoomId)
        }
    }

    private fun requestRoadTags(text: String, chatRoomId: Int) {
        if (text.isEmpty()) {
            liveViewEventsTagMenu.postValue(TagViewEvent.HideMenu)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val searchParams = SearchUniqueNameParams(
                        query = text, roomId = chatRoomId
                )

                searchByUniqueName.execute(
                        params = searchParams,
                        success = {
                            val res = mutableListOf<UITagEntity>()
                            it.users?.forEach { userSimple -> res.add(userSimple.toUITagEntity()) }
                            if (res.isNotEmpty()) liveTags.postValue(res)
                            else liveViewEventsTagMenu.postValue(TagViewEvent.HideMenu)
                        },
                        fail = { liveViewEventsTagMenu.postValue(TagViewEvent.HideMenu) }
                )
            }
        }
    }
}

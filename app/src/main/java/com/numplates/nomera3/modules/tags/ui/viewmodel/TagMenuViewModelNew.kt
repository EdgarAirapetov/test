package com.numplates.nomera3.modules.tags.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.data.entity.HashtagTagListModel
import com.numplates.nomera3.modules.tags.domain.mappers.TagModelMapper
import com.numplates.nomera3.modules.tags.domain.usecase.GetSuggestedHashtagListUseCase
import com.numplates.nomera3.modules.tags.domain.usecase.SearchByUniqueNameUseCase
import com.numplates.nomera3.modules.tags.domain.usecase.SearchUniqueNameParams
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel.HashtagUIModel
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel.UniqueNameUIModel
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew.TagMenuEvent.OnSuggestedHashtagListLoaded
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew.TagMenuEvent.OnSuggestedUniqueNameListLoaded
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class TagMenuViewModelNew : BaseViewModel() {

    @Inject
    lateinit var searchByUniqueName: SearchByUniqueNameUseCase

    @Inject
    lateinit var suggestedHashtagListUseCase: GetSuggestedHashtagListUseCase

    val events: LiveData<TagMenuEvent>
        get() = _events

    var suggestedTagListMenuType: SuggestionsMenuType
        get() = _suggestedTagListMenuType
        set(value) {
            _suggestedTagListMenuType = value
        }

    private val _events: MutableLiveData<TagMenuEvent> = MutableLiveData()
    private var _suggestedTagListMenuType: SuggestionsMenuType = SuggestionsMenuType.ROAD

    init {
        App.component.inject(this)
    }

    fun getSuggestedHashtagList(hashtag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            suggestedHashtagListUseCase.execute(
                suggestedHashtagListUseCase.Params(hashtag),
                { response: HashtagTagListModel ->
                    TagModelMapper
                        .createHashtagUIModelList(response)
                        .let { OnSuggestedHashtagListLoaded(it) }
                        .also { _events.postValue(it) }
                },
                { error: Exception ->
                    error.printStackTrace()
                }
            )
        }
    }

    fun getSuggestedUniqueNameList(text: String) {
        if (_suggestedTagListMenuType == SuggestionsMenuType.ROAD) {
            getSuggestedUniqueNameListInGroupChat(text, null)
        }
    }

    fun getSuggestedUniqueNameListInGroupChat(text: String, chatRoomId: Int?) {
        if (_suggestedTagListMenuType == SuggestionsMenuType.ROAD) {
            getSuggestedUniqueNameTagList(text, chatRoomId)
        }
    }

    private fun getSuggestedUniqueNameTagList(text: String, chatRoomId: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val searchParams = if (chatRoomId != null) {
                SearchUniqueNameParams(query = text, roomId = chatRoomId)
            } else {
                SearchUniqueNameParams(query = text)
            }

            searchByUniqueName.execute(
                params = searchParams,
                success = { (users: List<UserSimple>?) ->
                    TagModelMapper
                        .createUniqueNameUIModelList(users)
                        .also { _events.postValue(OnSuggestedUniqueNameListLoaded(it)) }
                },
                fail = {
                    _events.postValue(TagMenuEvent.OnErrorLoad)
                }
            )
        }
    }

    sealed class TagMenuEvent {
        object OnErrorLoad : TagMenuEvent()
        class OnSuggestedHashtagListLoaded(val suggestedHashtagList: List<HashtagUIModel>) : TagMenuEvent()
        class OnSuggestedUniqueNameListLoaded(val suggestedUniqueNameList: List<UniqueNameUIModel>) : TagMenuEvent()
    }

    sealed class TagListItem {
        class HashtagItem(val data: UITagEntity) : TagListItem()
        class UniqueNameItem(val data: UITagEntity) : TagListItem()
    }
}

package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.meera.core.extensions.doOnUIThread
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.domain.interactornew.CheckMainFilterRecommendedUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.RepostUseCase
import com.numplates.nomera3.domain.interactornew.SearchUserUseCase
import com.numplates.nomera3.domain.interactornew.SharePostUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.ResourceManager
import com.numplates.nomera3.modules.baseCore.domain.repository.EMPTY_VIDEO_AMPLITUDE_VALUE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyBottomSheetCloseMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommentsSettings
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPublicType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereSent
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPostShare
import com.numplates.nomera3.modules.communities.data.entity.CommunitiesListItemEntity
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunitiesAllowedToRepostUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.SearchGroupsUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.SearchGroupsUseCaseParams
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.maps.ui.events.model.EventLabelUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.share.domain.usecase.GetShareItemsParams
import com.numplates.nomera3.modules.share.domain.usecase.GetShareItemsUseCase
import com.numplates.nomera3.modules.share.domain.usecase.SearchShareItemsParams
import com.numplates.nomera3.modules.share.domain.usecase.SearchShareItemsUseCase
import com.numplates.nomera3.modules.share.ui.entity.ShareItemTypeEnum
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.numplates.nomera3.modules.share.ui.entity.toUIShareItems
import com.numplates.nomera3.modules.share.ui.mapper.ShareEventLabelUiMapper
import com.numplates.nomera3.modules.tracker.ITrackerActions
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDialogType
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePlaceHolderEnum
import com.numplates.nomera3.presentation.viewmodel.viewevents.SharePostViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.ViewStateLiveData
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SharePostViewModel : BaseViewModel() {

    @Inject
    lateinit var sharePostUseCase: SharePostUseCase

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var searchUserUseCase: SearchUserUseCase

    @Inject
    lateinit var searchGroup: SearchGroupsUseCase

    @Inject
    lateinit var getGroupsAllowedToRepostUseCase: GetCommunitiesAllowedToRepostUseCase

    @Inject
    lateinit var getFriends: GetFriendsUseCase

    @Inject
    lateinit var getShareItemsUseCase: GetShareItemsUseCase

    @Inject
    lateinit var searchShareItemsUseCase: SearchShareItemsUseCase

    @Inject
    lateinit var repost: RepostUseCase

    @Inject
    lateinit var myTracker: ITrackerActions

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var eventLabelUiMapper: ShareEventLabelUiMapper

    @Inject
    lateinit var featureTogglesContainer: FeatureTogglesContainer

    @Inject
    lateinit var checkMainFilterRecommendedUseCase: CheckMainFilterRecommendedUseCase

    private val disposables = CompositeDisposable()
    private val searchDisposable = CompositeDisposable()

    val liveMyGroups: ViewStateLiveData<MutableList<CommunitiesListItemEntity>> = ViewStateLiveData()
    val liveMyFriends: ViewStateLiveData<MutableList<FriendModel>> = ViewStateLiveData()

    private val _liveShareItems = MutableLiveData<List<UIShareItem>>()
    val liveShareItems: LiveData<List<UIShareItem>> = _liveShareItems

    private val _liveCheckedCount = MutableLiveData<Int>()
    val liveCheckedCount: LiveData<Int> = _liveCheckedCount

    private val _sharePostLiveEvent = MutableLiveData<SharePostViewEvent>()
    val sharePostLiveEvent: LiveData<SharePostViewEvent> = _sharePostLiveEvent

    val isEventsOnMapEnabled get() = featureTogglesContainer.mapEventsFeatureToggle.isEnabled

    private var selectedItems = mutableMapOf<String, UIShareItem>()

    private var isLoadingFriends = false
    private var isLastFriend = false

    private var isLastGroup = false
    private var isLoadingGroups = false

    private var isLoadingUserSearch = false
    private var isLastUserSearch = false

    private var isLoadingGroupSearch = false
    private var isLastGroupSearch = false

    init {
        App.component.inject(this)
        _liveCheckedCount.value = 0
    }

    fun mapEventLabel(eventUiModel: EventUiModel): EventLabelUiModel =
        eventLabelUiMapper.mapEventLabelUiModel(eventUiModel)

    fun getOwnUserId() = getUserUidUseCase.invoke()

    fun getMyGroups(isShowLoading: Boolean, isShowError: Boolean, startIndex: Int, limit: Int) {
        isLoadingGroups = true
        requestCallback({
            sharePostUseCase.getGroupsAllowedToRepost(startIndex, limit)
        }, {
            onSuccess {
                it.communityEntities?.let { groupEntities ->
                    val searchData = mutableListOf<CommunitiesListItemEntity>()
                    groupEntities.forEach { entity ->
                        if (entity?.royalty != 1 || entity.isModerator == 1) {
                            searchData.add(CommunitiesListItemEntity(entity))
                        }
                    }
                    if(searchData.isEmpty()) {
                        showPlaceholder(SharePlaceHolderEnum.EMPTY_SHARE_GROUPS)
                        isLastGroup = true
                    }

                    liveMyGroups.setSuccess(searchData)
                }
            }

            onProgress {
                liveMyGroups.setProgress(it)
            }

            onError { isShowError, error ->
                isLoadingGroups = false
                liveMyGroups.setError(isShowError, error)
            }
        }, isShowLoading, isShowError)
    }


    fun itemSearchChecked(item: UIShareItem, checked: Boolean) {
        val shareItems = mutableListOf<UIShareItem>()
        _liveShareItems.value?.let { shareItems.addAll(it) }
        val index = shareItems.indexOf(item)
        if (index == -1) return
        shareItems[index] = item.copy(isChecked = checked)

        if (checked) {
            if (selectedItems.containsKey(item.id).not()) {
                selectedItems[item.id] = shareItems[index]
            }
        } else {
            selectedItems.remove(item.id)
        }

        _liveShareItems.postValue(shareItems)
        updateCheckedCount()
    }

    fun canBeChecked(): Boolean {
        _liveCheckedCount.value?.let {
            return it <= 9
        }
        return true
    }

    private fun updateCheckedCount() =
        _liveCheckedCount.postValue(selectedItems.keys.size)


    private var jobGetItems: Job? = null
    fun getShareItems(shareDialogType: ShareDialogType?) {
        isLoadingFriends = true
        val lastId = getLastId()
        val selectedUserId = if (shareDialogType is ShareDialogType.ShareMoment) {
            shareDialogType.moment.userId
        } else {
            null
        }

        jobSearch?.cancel()
        jobGetItems?.cancel()
        jobGetItems = viewModelScope.launch {
            getShareItemsUseCase.execute(
                params = GetShareItemsParams(
                    lastId = lastId,
                    selectUsedId = selectedUserId
                ),
                success = {
                    if (lastId == null && it.isEmpty()) {
                        showPlaceholder(SharePlaceHolderEnum.EMPTY_SHARE_ITEMS)
                    } else {
                        showPlaceholder(SharePlaceHolderEnum.OK)
                    }
                    updateShareItems(it.toUIShareItems(resourceManager))
                    if (it.isEmpty()) isLastFriend = true
                    isLoadingFriends = false
                },
                fail = {
                    isLoadingFriends = false
                    liveMyFriends.setError(true, null)
                    if (_liveShareItems.value?.isNullOrEmpty() == true) showPlaceholder(SharePlaceHolderEnum.ERROR_SHARE_ITEMS)
                    else showPlaceholder(SharePlaceHolderEnum.OK)

                }
            )
        }
    }

    private fun showPlaceholder(type: SharePlaceHolderEnum) {
        _sharePostLiveEvent.value = (SharePostViewEvent.PlaceHolderShareEvent(type))
    }

    private fun getLastId(): String? =
        try {
            _liveShareItems.value?.last()?.id
        } catch (e: Exception) {
            Timber.e(e)
            null
        }


    private fun updateShareItems(items: List<UIShareItem>) {
        val checkedItems = items.map { item ->
            if (selectedItems.keys.contains(item.id)) item.copy(isChecked = true) else item
        }
        _liveShareItems.postValue(checkedItems)
    }

    fun clearShareItemsForSearch() {
        _liveShareItems.value = mutableListOf()
        _liveCheckedCount.value = selectedItems.keys.size

        resetFlags()
    }

    fun setShareItems() {
        _liveShareItems.value = selectedItems.values.toList()
        _liveCheckedCount.value = selectedItems.keys.size

        resetFlags()
    }

    private fun resetFlags() {
        isLastFriend = false
        isLastUserSearch = false
        isLoadingFriends = false
        isLoadingUserSearch = false
    }

    fun clearSelectedItems() {
        selectedItems.clear()
    }

    private var jobSearch: Job? = null
    fun searchShareItems(query: String) {
        isLoadingUserSearch = true
        val lastId = getLastId()
        jobSearch?.cancel()
        jobGetItems?.cancel()
        jobSearch = viewModelScope.launch {
            searchShareItemsUseCase.execute(
                params = SearchShareItemsParams(lastId, query),
                success = {
                    if (lastId == null && it.isEmpty()) showPlaceholder(SharePlaceHolderEnum.EMPTY_SEARCH)
                    else showPlaceholder(SharePlaceHolderEnum.OK)
                    updateShareItems(it.toUIShareItems(resourceManager))
                    if (it.isEmpty()) isLastUserSearch = true
                    isLoadingUserSearch = false
                },
                fail = {
                    if (_liveShareItems.value?.isNullOrEmpty() == true) showPlaceholder(SharePlaceHolderEnum.ERROR_SEARCH)
                    else showPlaceholder(SharePlaceHolderEnum.OK)
                    isLoadingUserSearch = false
                    liveMyFriends.setError(true, null)
                }
            )
        }
    }

    fun sendRepost(post: Post, comment: String, commentSettings: Int) {
        val checkedItems = selectedItems.values.toList()
        if (checkedItems.isNotEmpty()) {
            sendToChat(
                checkedItems = checkedItems,
                comment = comment,
                postId = post.id,
            )
        } else {
            sendToRoad(
                comment = comment,
                post = post,
                commentSettings = commentSettings,
            )
        }
    }

    private fun sendToRoad(comment: String, post: Post, commentSettings: Int) {
        requestCallback({
            repost.repostRoadType(
                postId = post.id,
                comment = comment,
                commentSettings = commentSettings,
            )
        }, {
            onSuccess {
                logAmplitudeRepost(
                    post = post,
                    comment = comment,
                    commentSetting = commentSettings,
                )
                myTracker.trackRepostRoad()
                _sharePostLiveEvent.value = SharePostViewEvent.onSuccessRoadTypeRepost
            }
            onError { _, error ->
                _sharePostLiveEvent.postValue(SharePostViewEvent.OnErrorMessageRepost(error?.message))
            }
        })
    }

    private fun logAmplitudeRepost(
        post: Post,
        comment: String,
        commentSetting: Int,
        isCommunity: Boolean = false
    ) {
        // Amplitude post created log
        val where =
            if (isCommunity) AmplitudePropertyWhere.COMMUNITY else AmplitudePropertyWhere.SELF_FEED

        val postType = AmplitudePropertyPostType.REPOST

        var i = 0
        if (comment.isNotBlank()) i += 1
        if (post.mediaEntity != null) i += 1
        if (post.image != null) i += 1
        if (post.hasPostVideo()) i += 1
        if (post.hasPostGif()) i += 1

        val contentType = AmplitudePropertyContentType.NONE

        val commentSettings = when (commentSetting) {
            0 -> AmplitudePropertyCommentsSettings.NOBODY
            1 -> AmplitudePropertyCommentsSettings.FOR_ALL
            else -> AmplitudePropertyCommentsSettings.FRIENDS
        }

        amplitudeHelper.logPostOtherEvents(
            postId = post.id,
            authorId = getUserUidUseCase.invoke(),
            where = where,
            postType = postType,
            postContentType = contentType,
            haveText = comment.isNotBlank(),
            havePic = post.image != null,
            haveVideo = post.hasPostVideo(),
            haveGif = post.hasPostGif(),
            commentsSettings = commentSettings,
            haveMusic = post.mediaEntity != null,
            videoDurationSec = post.videoDurationInSeconds ?: EMPTY_VIDEO_AMPLITUDE_VALUE
        )
    }

    fun logPostShareOpen(
        postId: Long,
        momentId: Long,
        authorId :Long,
        where: AmplitudePropertyWhere,
        publicType: AmplitudePropertyPublicType
    ) {
        amplitudeHelper.logPostShareOpen(
            postId = postId,
            authorId = authorId,
            momentId = momentId,
            where = where,
            recFeed = checkMainFilterRecommendedUseCase.invoke(),
            publicType = publicType
        )
    }

    fun logPostShare(
        analyticsPostShare: AnalyticsPostShare
    ) {
        val updatedAnalyticsPostShare = if (analyticsPostShare.whereSent == AmplitudePropertyWhereSent.CHAT) {
            val checkedItems = _liveShareItems.value?.filter { it.isChecked } ?: listOf()
            val chatCount = checkedItems.count { !it.isGroupChat }
            val groupCount = checkedItems.count { it.isGroupChat }
            analyticsPostShare.copy(
                chatCount = chatCount,
                groupCount = groupCount
            )
        } else {
            analyticsPostShare
        }
        amplitudeHelper.logPostShare(
            analyticsPostShare = updatedAnalyticsPostShare,
            recFeed = checkMainFilterRecommendedUseCase.invoke()
        )
    }

    fun logPostShareClose(bottomSheetCloseMethod: AmplitudePropertyBottomSheetCloseMethod) {
        amplitudeHelper.logPostShareClose(bottomSheetCloseMethod)
    }

    fun logPostShareSettingsTap(where: AmplitudePropertyWhere) {
        amplitudeHelper.logPostShareSettingsTap(where)
    }

    fun getSelectedItemsUser(): MutableList<Long> {
        val listIdUser = mutableListOf<Long>()
        val items = selectedItems.values.filter { it.isChecked && it.type != ShareItemTypeEnum.ROOM }
        items.forEach { listIdUser.add(it.idResend) }
        return listIdUser
    }

    fun getSelectedItemsRooms(): MutableList<Long> {
        val listIdRooms = mutableListOf<Long>()
        val items = selectedItems.values.filter { it.isChecked && it.type == ShareItemTypeEnum.ROOM }
        items.forEach { listIdRooms.add(it.idResend) }
        return listIdRooms
    }

    fun getSelectedGroupsCount() =
        _liveShareItems.value?.filter { it.isChecked && it.isGroupChat }?.size ?: 0


    private fun sendToChat(checkedItems: List<UIShareItem>, comment: String, postId: Long) {
        val listIdUser = mutableListOf<Long>()
        val listIdRooms = mutableListOf<Long>()
        checkedItems.forEach { item ->
            when (item.type) {
                ShareItemTypeEnum.ROOM -> listIdRooms.add(item.idResend)
                ShareItemTypeEnum.FRIEND,
                ShareItemTypeEnum.SUBSCRIPTION -> listIdUser.add(item.idResend)
            }
        }
        _sharePostLiveEvent.postValue(SharePostViewEvent.BlockSendBtn)
        requestCallback({
            repost.repostMessage(postId, comment, listIdUser, listIdRooms)
        }, {
            onSuccess {
                myTracker.trackRepostChat()
                _sharePostLiveEvent.postValue(SharePostViewEvent.onSuccessMessageRepost(listIdRooms.size + listIdUser.size))
            }
            onError { _, error ->
                doOnUIThread {
                    _sharePostLiveEvent.value = SharePostViewEvent.UnBlockSendBtn
                    _sharePostLiveEvent.value = SharePostViewEvent.OnErrorMessageRepost(error?.message)
                }
            }
        })
    }


    fun searchGroupRequest(query: String, limit: Int = 10, offset: Int = 0) {
        isLoadingGroupSearch = true
        searchDisposable.clear()
        viewModelScope.launch {
            //group_type = 1 -> юзер автор, group_type = 2 -> юзер состоит в группе
            searchGroup.execute(
                params = SearchGroupsUseCaseParams(query, offset, 2, limit, true),
                success = {
                    val searchData = mutableListOf<CommunitiesListItemEntity>()
                    it.communityEntities?.forEach { entity ->
                        searchData.add(CommunitiesListItemEntity(entity))
                    }
                    isLastGroupSearch = it.communityEntities?.isEmpty() == true
                    if (searchData.isEmpty()) {
                        showPlaceholder(SharePlaceHolderEnum.EMPTY_SEARCH)
                    }
                    liveMyGroups.setSuccess(searchData)
                },
                fail = {
                    Timber.e(it)
                    isLoadingGroupSearch = false
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun isLoadingFriends() = isLoadingFriends
    fun isLastFriend() = isLastFriend

    fun isLoadingGroups() = isLoadingGroups
    fun isLastGroup() = isLastGroup

    fun isLoadingUserSearch() = isLoadingUserSearch
    fun isLastUserSearch() = isLastUserSearch

    fun isLoadingGroupSearch() = isLoadingGroupSearch
    fun isLastGroupSearch() = isLastGroupSearch

}

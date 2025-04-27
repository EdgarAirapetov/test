package com.numplates.nomera3.modules.communities.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCanWrite
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommunityType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommunityWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHavePhoto
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.data.states.CommunityNotFoundException
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.notifications.CommunityNotificationsUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.notifications.SubscribeCommunityNotificationsUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.notifications.UnsubscribeCommunityNotificationsUseCase
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityViewEvent
import com.numplates.nomera3.modules.communities.ui.viewevent.GetCommunityLinkAction
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityModelMapper.Companion.getMemberRole
import com.numplates.nomera3.modules.share.domain.usecase.GetCommunityLinkParams
import com.numplates.nomera3.modules.share.domain.usecase.GetCommunityLinkUseCase
import com.numplates.nomera3.modules.upload.domain.UploadStatus
import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import com.numplates.nomera3.modules.user.domain.usecase.UserPermissionParams
import com.numplates.nomera3.modules.user.domain.usecase.UserPermissionsUseCase
import com.numplates.nomera3.modules.user.ui.entity.UserPermissions
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommunityRoadViewModel : BaseViewModel() {

    @Inject
    lateinit var communityInfoUseCase: GetCommunityInformationUseCase

    @Inject
    lateinit var subscribeNotificationsUseCase: SubscribeCommunityNotificationsUseCase

    @Inject
    lateinit var unsubscribeNotificationsUseCase: UnsubscribeCommunityNotificationsUseCase

    @Inject
    lateinit var getLinkUseCase: GetCommunityLinkUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var userPermissions: UserPermissionsUseCase

    @Inject
    lateinit var getAppInfo: GetAppInfoAsyncUseCase

    @Inject
    lateinit var uploadRepository: UploadRepository

    val liveViewEvent = MutableLiveData<CommunityViewEvent?>()

    val communityInfoLiveEvent = MutableLiveData<CommunityViewEvent?>()

    private var communityEntity: CommunityEntity? = null
    private var permissions: UserPermissions? = null
    private val ADMIN_CONST = "admin_support_id"

    init {
        App.component.inject(this)
        uploadRepository.getState()
            .filter { it.status is UploadStatus.Success }
            .onEach { liveViewEvent.postValue(CommunityViewEvent.RefreshCommunityRoad) }
            .launchIn(viewModelScope)
    }

    private fun requestUserPermissions(readyCallback: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            userPermissions.execute(
                params = UserPermissionParams(),
                success = {
                    permissions = it
                    readyCallback()
                },
                fail = {
                    readyCallback()
                }
            )
        }
    }

    fun getCommunityInfo(groupId: Int?) {
        communityInfoLiveEvent.value = CommunityViewEvent.CommunityDataProgress(true)
        requestUserPermissions {
            groupId?.let { id ->
                viewModelScope.launch {
                    communityInfoUseCase.execute(
                        params = GetCommunityInformationUseCaseParams(id),
                        success = {
                            communityInfoLiveEvent.value = CommunityViewEvent.CommunityDataProgress(false)
                            val data = it?.community
                            if (data != null) {
                                communityEntity = data
                                communityInfoLiveEvent.value = CommunityViewEvent.CommunityData(data, permissions)
                                liveViewEvent.value = CommunityViewEvent.BaseLoadPosts
                            } else {
                                communityInfoLiveEvent.value = CommunityViewEvent.FailureGetCommunityInfo
                            }
                        },
                        fail = { exception ->
                            communityInfoLiveEvent.value = CommunityViewEvent.CommunityDataProgress(false)
                            if (exception is CommunityNotFoundException) {
                                communityInfoLiveEvent.value = CommunityViewEvent.FailureCommunityNotFound
                            } else {
                                communityInfoLiveEvent.value = CommunityViewEvent.FailureGetCommunityInfo
                            }
                        }
                    )
                }
            }
        }
    }

    fun subscribeNotifications(groupId: Int?) {
        Timber.d("subscribeNotifications: grId: $groupId")
        liveViewEvent.value = CommunityViewEvent.CommunityNotificationsProgress(true)
        groupId?.let { id ->
            viewModelScope.launch {
                subscribeNotificationsUseCase.execute(
                    params = CommunityNotificationsUseCaseParams(id),
                    success = {
                        Timber.d("RESPONSE subscribeNotifications: ${Gson().toJson(it)}")
                        liveViewEvent.value =
                            CommunityViewEvent.CommunityNotificationsProgress(false)
                        liveViewEvent.value =
                            CommunityViewEvent.SuccessSubscribedToNotifications(groupId)
                    },
                    fail = {
                        Timber.e("ERROR: subscribeNotifications: ${it.localizedMessage}")
                        liveViewEvent.value =
                            CommunityViewEvent.CommunityNotificationsProgress(false)
                        liveViewEvent.value =
                            CommunityViewEvent.FailedSubscribeToNotifications(groupId)
                    }
                )
            }
        }
    }

    fun unsubscribeNotifications(groupId: Int?) {
        Timber.d("unsubscribeNotifications: grId: $groupId")
        liveViewEvent.value = CommunityViewEvent.CommunityNotificationsProgress(true)
        groupId?.let { id ->
            viewModelScope.launch {
                unsubscribeNotificationsUseCase.execute(
                    params = CommunityNotificationsUseCaseParams(id),
                    success = {
                        Timber.d("RESPONSE unsubscribeNotifications: ${Gson().toJson(it)}")
                        liveViewEvent.value =
                            CommunityViewEvent.CommunityNotificationsProgress(false)
                        liveViewEvent.value =
                            CommunityViewEvent.SuccessUnsubscribedFromNotifications(groupId)
                    },
                    fail = {
                        Timber.e("ERROR: unsubscribeNotifications: ${it.localizedMessage}")
                        liveViewEvent.value =
                            CommunityViewEvent.CommunityNotificationsProgress(false)
                        liveViewEvent.value =
                            CommunityViewEvent.FailedUnsubscribeFromNotifications(groupId)
                    }
                )
            }
        }
    }

    fun getCommunity() = communityEntity

    fun getCommunityListItemUIModel() = CommunityListItemUIModel(
        id = communityEntity?.groupId,
        coverImage = communityEntity?.avatar ?: "",
        isPrivate = communityEntity?.private == 1,
        isMember = communityEntity?.isSubscribed == 1,
        isCreator = communityEntity?.isAuthor == 1,
        isModerator = communityEntity?.isModerator == 1,
        name = communityEntity?.name ?: "",
        memberCount = communityEntity?.users ?: 0,
        isUserApproved = communityEntity?.userStatus != CommunityEntity.USER_STATUS_NOT_YET_APPROVED,
        memberRole = getMemberRole(communityEntity),
        userStatus = communityEntity?.userStatus ?: -1
    )

    fun getCommunityLink(groupId: Int?, action: GetCommunityLinkAction) {
        groupId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                getLinkUseCase.execute(
                    params = GetCommunityLinkParams(id),
                    success = { response ->
                        liveViewEvent.postValue(
                            CommunityViewEvent
                                .SuccessGetCommunityLink(response.deeplinkUrl, action)
                        )
                    },
                    fail = {
                        liveViewEvent.postValue(CommunityViewEvent.FailGetCommunityLink)
                    }
                )
            }
        }
    }

    fun onWriteToTechSupportClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settingsAwaitJob = getAppInfo.executeAsync()
                val setting = settingsAwaitJob.await().appInfo.findLast { it.name == ADMIN_CONST }

                setting?.value?.let {
                    val adminId = try {
                        it.toLong()
                    } catch (e: Exception) {
                        Timber.e(e)
                        null
                    } ?: return@let

                    liveViewEvent.postValue(CommunityViewEvent.OpenSupportAdminChat(adminId))
                }
            } catch (exception: Throwable) {
                Timber.e(exception)
            }
        }
    }

    fun logCommunityShare(
        where: AmplitudePropertyCommunityWhere,
    ) {
        val groupId = communityEntity?.groupId?.toLong() ?: -1L
        val isClosed = communityEntity?.private?.isTrue() ?: false
        val canWrite = communityEntity?.royalty?.toBoolean() ?: false
        val havePhoto =
            (communityEntity?.avatar.isNullOrEmpty() || communityEntity?.avatarBig.isNullOrEmpty()).not()
        val isClosedType =
            if (isClosed) AmplitudePropertyCommunityType.CLOSED else AmplitudePropertyCommunityType.OPEN
        val canWriteType =
            if (canWrite) AmplitudePropertyCanWrite.ALL else AmplitudePropertyCanWrite.ADMIN
        val havePhotoType =
            if (havePhoto) AmplitudePropertyHavePhoto.YES else AmplitudePropertyHavePhoto.NO
        amplitudeHelper.logCommunityShare(where, groupId, isClosedType, canWriteType, havePhotoType)
    }

    fun clearEvents() {
        liveViewEvent.value = null
    }
}

package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.doOnUIThread
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.RepostUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.domain.repository.EMPTY_VIDEO_AMPLITUDE_VALUE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommentsSettings
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.chat.messages.domain.usecase.ForwardChatMessageParams
import com.numplates.nomera3.modules.chat.messages.domain.usecase.ForwardChatMessageUseCase
import com.numplates.nomera3.modules.moments.show.data.MomentUserMessageException
import com.numplates.nomera3.modules.moments.show.data.mapper.MomentsUiMapper
import com.numplates.nomera3.modules.moments.show.domain.GetMomentLinkUseCase
import com.numplates.nomera3.modules.moments.show.domain.ShareMomentUseCase
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.share.domain.usecase.GetPostLinkParams
import com.numplates.nomera3.modules.share.domain.usecase.GetPostLinkUseCase
import com.numplates.nomera3.modules.share.domain.usecase.ShareCommunityParams
import com.numplates.nomera3.modules.share.domain.usecase.ShareCommunityUseCase
import com.numplates.nomera3.modules.share.domain.usecase.ShareUserProfileParams
import com.numplates.nomera3.modules.share.domain.usecase.ShareUserProfileUseCase
import com.numplates.nomera3.modules.share.ui.entity.UIShareMessageEntity
import com.numplates.nomera3.modules.tracker.ITrackerActions
import com.numplates.nomera3.presentation.viewmodel.viewevents.SharePostViewEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RepostViewModel : BaseViewModel() {

    @Inject
    lateinit var repost: RepostUseCase

    @Inject
    lateinit var shareUserProfileUseCase: ShareUserProfileUseCase

    @Inject
    lateinit var shareMomentUseCase: ShareMomentUseCase

    @Inject
    lateinit var getMomentLinkUseCase: GetMomentLinkUseCase

    @Inject
    lateinit var momentMapper: MomentsUiMapper

    @Inject
    lateinit var shareCommunityUseCase: ShareCommunityUseCase

    @Inject
    lateinit var forwardChatMessageUseCase: ForwardChatMessageUseCase

    @Inject
    lateinit var getPostLinkUseCase: GetPostLinkUseCase

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase


    @Inject
    lateinit var myTracker: ITrackerActions

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    val liveEvent = MutableLiveData<SharePostViewEvent>()

    init {
        App.component.inject(this)
    }

    fun shareUserProfile(
        userId: Long,
        userIds: List<Long>,
        roomIds: List<Long>,
        message: String
    ) {
        liveEvent.postValue(SharePostViewEvent.BlockSendBtn)
        viewModelScope.launch(Dispatchers.IO) {
            shareUserProfileUseCase.execute(
                params = ShareUserProfileParams(
                    userId = userId,
                    userIds = userIds,
                    roomIds = roomIds,
                    comment = message
                ),
                success = { response ->
                    if (response.data != null) {
                        liveEvent.postValue(SharePostViewEvent.OnSuccessShareUserProfile)
                    } else {
                        liveEvent.postValue(
                            SharePostViewEvent.OnErrorShareUserProfile(response.err.userMessage.orEmpty())
                        )
                    }
                },
                fail = { exception ->
                    Timber.e(exception)
                    doOnUIThread {
                        liveEvent.value = SharePostViewEvent.UnBlockSendBtn
                        liveEvent.value = SharePostViewEvent.OnFailShareUserProfile
                    }
                }
            )
        }
    }

    fun shareMoment(
        moment: MomentItemUiModel,
        userIds: List<Long>,
        roomIds: List<Long>,
        message: String
    ) {
        liveEvent.postValue(SharePostViewEvent.BlockSendBtn)
        viewModelScope.launch {
            runCatching {
                shareMomentUseCase.invoke(
                    momentId = moment.id,
                    userIds = userIds,
                    roomIds = roomIds,
                    comment = message
                )
            }.onSuccess { momentItemModel ->
                checkForAccessAndRepostMoment(moment.copy(repostsCount = momentItemModel.repostsCount))
            }.onFailure {
                Timber.e(it)
                doOnUIThread {
                    val errorMessage = when (it) {
                        is MomentUserMessageException -> it.userMessage
                        else -> null
                    }
                    liveEvent.value = SharePostViewEvent.UnBlockSendBtn
                    liveEvent.value = SharePostViewEvent.OnFailShareMoment(messageText = errorMessage)
                }
            }
        }
    }

    fun shareCommunity(
        groupId: Int,
        userIds: List<Long>,
        roomIds: List<Long>,
        message: String
    ) {
        liveEvent.postValue(SharePostViewEvent.BlockSendBtn)
        viewModelScope.launch(Dispatchers.IO) {
            shareCommunityUseCase.execute(
                params = ShareCommunityParams(groupId, userIds, roomIds, message),
                success = { response ->
                    if (response.data != null) {
                        liveEvent.postValue(SharePostViewEvent.OnSuccessShareCommunity)
                    } else {
                        liveEvent.postValue(
                            SharePostViewEvent.OnErrorShareCommunity(response.err.userMessage.orEmpty())
                        )
                    }
                },
                fail = { exception ->
                    Timber.e(exception)
                    doOnUIThread {
                        liveEvent.value = SharePostViewEvent.UnBlockSendBtn
                        liveEvent.value = SharePostViewEvent.OnFailShareCommunity
                    }
                }
            )
        }
    }

    private fun checkForAccessAndRepostMoment(uiModel: MomentItemUiModel?) {
        if (uiModel?.isAccessDenied == true || uiModel?.isDeleted == true || uiModel?.isActive == false) {
            doOnUIThread {
                liveEvent.value = SharePostViewEvent.UnBlockSendBtn
                liveEvent.value = SharePostViewEvent.OnFailShareMoment(R.string.no_action_moment_unavailable)
            }
        } else {
            liveEvent.postValue(SharePostViewEvent.OnSuccessShareMoment(uiModel))
        }
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
            where = where,
            postType = postType,
            authorId = getUserUidUseCase.invoke(),
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

    fun doGroupRepost(comment: String, post: Post, groupId: Long, commentSettings: Int) {
        requestCallback({
            repost.repostGroup(
                postId = post.id,
                comment = comment,
                groupId = groupId,
                commentSettings = commentSettings,
            )
        }, {
            onSuccess {
                logAmplitudeRepost(
                    post = post,
                    comment = comment,
                    commentSetting = commentSettings,
                    isCommunity = true,
                )
                myTracker.trackRepostGroup()
                liveEvent.value = SharePostViewEvent.onSuccessGroupRepost
            }
            onError { _, error ->
                liveEvent.postValue(SharePostViewEvent.onErrorGroupRepost(error?.userMessage.orEmpty()))
            }
        })
    }

    fun forwardChatMessage(
        message: UIShareMessageEntity?,
        selectedGroupsCount: Int,
        messageId: String,
        roomId: Long,
        userIds: List<Long>,
        roomIds: List<Long>,
        extraMessage: String
    ) {
        liveEvent.postValue(SharePostViewEvent.BlockSendBtn)
        viewModelScope.launch(Dispatchers.IO) {
            forwardChatMessageUseCase.execute(
                params = ForwardChatMessageParams(
                    messageId = messageId,
                    roomId = roomId,
                    userIds = userIds.ifEmpty { null },
                    roomIds = roomIds.ifEmpty { null },
                    message = extraMessage
                ),
                success = { response ->
                    if (response.data != null) {
                        val userNames = response.data.userNames
                            ?.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
                            ?.joinToString().orEmpty()
                        message?.let {
                            logMessageForwardSend(
                                message = it,
                                chatCount = userIds.size + roomIds.size,
                                groupCount = selectedGroupsCount,
                                haveAddedText = extraMessage.isNotEmpty()
                            )
                        }
                        liveEvent.postValue(SharePostViewEvent.OnSuccessForwardChatMessage(userNames))
                    } else {
                        liveEvent.postValue(
                            SharePostViewEvent.OnErrorForwardChatMessage(response.err.userMessage.orEmpty())
                        )
                    }
                },
                fail = { exception ->
                    Timber.e(exception)
                    doOnUIThread {
                        liveEvent.value = SharePostViewEvent.UnBlockSendBtn
                        liveEvent.value = SharePostViewEvent.OnFailForwardChatMessage
                    }
                }
            )
        }
    }

    private fun logMessageForwardSend(
        message: UIShareMessageEntity,
        chatCount: Int,
        groupCount: Int,
        haveAddedText: Boolean
    ) {
        val havePostText = !message.message.isNullOrEmpty()
        val havePicture = !message.images.isNullOrEmpty()
        val haveVideo = !message.video.isNullOrEmpty()
        val haveGif = message.images.any { it.endsWith(".gif") } || message.isGiphy
        val haveAudio = message.isAudio
        val haveMedia = haveAudio || havePicture || haveVideo
        amplitudeHelper.logForwardMessage(
            chatCount = chatCount,
            groupCount = groupCount,
            haveAddText = haveAddedText,
            havePostText = havePostText,
            havePostPic = havePicture,
            havePostGif = haveGif,
            havePostMedia = haveMedia,
            havePostMusic = haveAudio,
            havePostVideo = haveVideo
        )
    }

    fun getPostLink(postId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            getPostLinkUseCase.execute(
                params = GetPostLinkParams(postId),
                success = { response ->
                    liveEvent.postValue(SharePostViewEvent.onSuccessSharePostLink(response.deeplinkUrl))
                },
                fail = { exception ->
                    Timber.e(exception)
                    liveEvent.postValue(SharePostViewEvent.onErrorSharePostLink)
                }
            )
        }
    }

    fun getMomentLink(momentId: Long) {
        viewModelScope.launch {
            runCatching {
                getMomentLinkUseCase.invoke(momentId = momentId)
            }.onSuccess { linkModel ->
                liveEvent.postValue(SharePostViewEvent.onSuccessShareMomentLink(linkModel.deepLinkUrl))
            }.onFailure {
                Timber.e(it)
                liveEvent.postValue(SharePostViewEvent.onErrorShareMomentLink)
            }
        }
    }
}

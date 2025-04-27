package com.numplates.nomera3.modules.analytics.domain

import com.amplitude.api.Identify
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeRepository
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeShakeAnalyticRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudeCreatePostWhichButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionPlayStopMusic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionTypeChatRequest
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAnimatedAvatarFrom
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarDownloadFrom
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarPhotoType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyBottomSheetCloseMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCallCanceller
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCallType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCallsSettings
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCanWrite
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCandyCount
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCallSwitcherPosition
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatMediaKeyboardCategory
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatUserChatStatus
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyColor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommentsSettings
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommunityType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommunityWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyDuration
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyFrom
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyFullness
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyGiftSendBack
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveDescription
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveMedia
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHavePhoto
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveVIPBefore
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHowWasOpened
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyInputType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMapPrivacyListType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyOnboarding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyProfileShare
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPublicType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyReactionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRecognizedTextButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationAvatarHaveReferral
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationAvatarPhotoType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationBirthdayHide
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationGender
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationGenderHide
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationLocationAutocomplete
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationStep
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRoadType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySettingVisibility
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyTransportType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyVehicleType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWay
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityFollow
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereFriendsSearch
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereMapPrivacy
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereMusicPlay
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereOpenMap
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereReaction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPost
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPostShare
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsUser
import com.numplates.nomera3.modules.baseCore.helper.amplitude.ComplainExtraActions
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.ComplainType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.map.AmplitudeMap
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyWhosePost
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeHowProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakePositionProperty
import javax.inject.Inject

class AnalyticsInteractor @Inject constructor(
    private val amlitudeShakeAnalytic: AmplitudeShakeAnalyticRepository,
    private val amplitudeHelper: AmplitudeRepository,
    private val amplitudeMap: AmplitudeMap
) {
    fun logShakeSwitchChanged(
        shakePositionProperty: AmplitudeShakePositionProperty,
        userId: Long
    ) = amlitudeShakeAnalytic.logShakeSwitchChanged(
        shakePositionProperty = shakePositionProperty,
        userId = userId
    )

    fun logShakeResults(
        howCalled: AmplitudeShakeHowProperty,
        countMutualAudience: Int,
        countUserShake: Int,
        fromId: Long,
        toId: Long
    ) = amlitudeShakeAnalytic.logShakeResults(
        howCalled = howCalled,
        countMutualAudience = countMutualAudience,
        countUserShake = countUserShake,
        fromId = fromId,
        toId = toId,
    )

    fun setIsFromSms(isFromSms: Boolean) = amplitudeHelper.setIsFromSms(isFromSms)

    fun setUser(user: AnalyticsUser) = amplitudeHelper.setUser(user)

    fun logMapPrivacySettingsSetup(
        where: AmplitudePropertyWhereMapPrivacy,
        visibility: AmplitudePropertySettingVisibility
    ) = amplitudeHelper.logMapPrivacySettingsSetup(where = where, visibility = visibility)

    fun logMapPrivacySettingsClicked(where: AmplitudePropertyWhereMapPrivacy) =
        amplitudeHelper.logMapPrivacySettingsClicked(where)

    fun logUnlockChat(from: Long, to: Long) = amplitudeHelper.logUnlockChat(from = from, to = to)

    fun logForwardMessageClicked() = amplitudeHelper.logForwardMessageClicked()

    fun logForwardMessage(
        chatCount: Int,
        groupCount: Int,
        haveAddText: Boolean,
        havePostText: Boolean,
        havePostPic: Boolean,
        havePostVideo: Boolean,
        havePostGif: Boolean,
        havePostMusic: Boolean,
        havePostMedia: Boolean
    ) = amplitudeHelper.logForwardMessage(
        chatCount = chatCount,
        groupCount = groupCount,
        haveAddText = haveAddText,
        havePostText = havePostText,
        havePostPic = havePostPic,
        havePostVideo = havePostVideo,
        havePostGif = havePostGif,
        havePostMusic = havePostMusic,
        havePostMedia = havePostMedia,
    )

    fun logBottomBarChatClicked() = amplitudeHelper.logBottomBarChatClicked()

    fun logBottomBarCommunityClicked() = amplitudeHelper.logBottomBarCommunityClicked()

    fun logBottomBarProfileClicked() = amplitudeHelper.logBottomBarProfileClicked()

    fun logComplainExtraAction(actions: ComplainExtraActions) = amplitudeHelper.logComplainExtraAction(actions)

    fun logComplain(type: ComplainType, from: Long, to: Long) = amplitudeHelper.logComplain(
        type = type,
        from = from,
        to = to,
    )

    fun logBottomBarRoad(
        roadType: AmplitudePropertyRoadType,
        roadHowWasOpened: AmplitudePropertyHowWasOpened,
        recFeed: Boolean
    ) = amplitudeHelper.logBottomBarRoad(roadType, recFeed, roadHowWasOpened)

    fun logPlayStopMusic(
        actionType: AmplitudePropertyActionPlayStopMusic,
        where: AmplitudePropertyWhereMusicPlay
    ) = amplitudeHelper.logPlayStopMusic(actionType = actionType, where = where)

    fun logMusicAddPress() = amplitudeHelper.logMusicAddPress()

    fun logBackPressed() = amplitudeHelper.logBackPressed()

    fun logBackSwipe() = amplitudeHelper.logBackSwipe()

    fun reactionPanelOpen() = amplitudeHelper.reactionPanelOpen()

    fun reactionToComment(
        postId: Long,
        where: AmplitudePropertyWhereReaction,
        type: AmplitudePropertyReactionType,
        whence: AmplitudePropertyWhence,
        commentUserId: Long,
        postUserId: Long,
        momentId: Long,
    ) = amplitudeHelper.reactionToComment(
        postId = postId,
        where = where,
        type = type,
        whence = whence,
        commentUserId = commentUserId,
        publicationUserId = postUserId,
        momentId = momentId
    )

    fun identifyUserProperty(properties: (Identify) -> Identify) = amplitudeHelper.identifyUserProperty(properties)

    fun logFilterMainRoad(country: String, city: String, recFeed: Boolean) = amplitudeHelper.logFilterMainRoad(
        country = country,
        city = city,
        recFeed = recFeed
    )

    fun logClickSearchButtonOnMainRoad() = amplitudeHelper.logClickSearchButtonOnMainRoad()

    fun logSearchInput(
        type: AmplitudePropertySearchType,
        haveResult: AmplitudePropertyHaveResult,
        whereCommunitySearch: AmplitudePropertyWhereCommunitySearch,
        whereFriendsSearch: AmplitudePropertyWhereFriendsSearch = AmplitudePropertyWhereFriendsSearch.NONE
    ) = amplitudeHelper.logSearchInput(
        type = type,
        haveResult = haveResult,
        whereCommunitySearch = whereCommunitySearch,
        whereFriendsSearch = whereFriendsSearch,
    )

    fun logTapSearchAtSign() = amplitudeHelper.logTapSearchAtSign()

    fun logTapSearchByNumberButton() = amplitudeHelper.logTapSearchByNumberButton()

    fun logEmojiTap(emojiType: String) = amplitudeHelper.logEmojiTap(emojiType)

    fun logCommunityCreateMenuOpen() = amplitudeHelper.logCommunityCreateMenuOpen()

    fun logCommunityCreated(
        type: AmplitudePropertyCommunityType,
        whoCanWrite: AmplitudePropertyCanWrite,
        havePhoto: AmplitudePropertyHavePhoto
    ) = amplitudeHelper.logCommunityCreated(
        type = type,
        whoCanWrite = whoCanWrite,
        havePhoto = havePhoto,
    )

    fun logCommunityDeleted() = amplitudeHelper.logCommunityDeleted()

    fun logCommunityScreenOpened(where: AmplitudePropertyWhereCommunityOpen) =
        amplitudeHelper.logCommunityScreenOpened(where)

    fun logCommunityFollow(
        userId: Long,
        where: AmplitudePropertyWhereCommunityFollow,
        communityId: Int
    ) =
        amplitudeHelper.logCommunityFollow(
            userId = userId,
            where = where,
            communityId = communityId,
        )

    fun logCommunityUnFollow(userId: Long, communityId: Int) =
        amplitudeHelper.logCommunityUnfollow(userId, communityId)

    fun logChatOpen(chatType: AmplitudePropertyChatType, where: AmplitudePropertyWhere) = amplitudeHelper.logChatOpen(
        chatType = chatType,
        where = where,
    )

    fun logGroupChatCreate(
        havePhoto: AmplitudePropertyHavePhoto,
        haveDescription: AmplitudePropertyHaveDescription
    ) = amplitudeHelper.logGroupChatCreate(
        havePhoto = havePhoto,
        haveDescription = haveDescription,
    )

    fun logGroupChatDelete() = amplitudeHelper.logGroupChatDelete()

    fun logMessageSend(
        haveText: Boolean,
        havePic: Boolean,
        haveVideo: Boolean,
        haveGif: Boolean,
        haveAudio: Boolean,
        duration: Long?,
        haveMedia: AmplitudePropertyHaveMedia,
        isGroupChat: Boolean,
        from: Long,
        to: Long,
        mediaKeyboardCategory: AmplitudePropertyChatMediaKeyboardCategory,
        status: AmplitudePropertyChatUserChatStatus,
        messageId: String
    ) = amplitudeHelper.logMessageSend(
        haveText = haveText,
        havePic = havePic,
        haveVideo = haveVideo,
        haveAudio = haveAudio,
        haveMedia = haveMedia,
        isGroupChat = isGroupChat,
        from = from,
        to = to,
        status = status,
        messageId = messageId,
        duration = duration,
        haveGif = haveGif,
        mediaKeyboardCategory = mediaKeyboardCategory
    )

    ///////////////////////////////////////////////////////////////////////////
    // OLD
    ///////////////////////////////////////////////////////////////////////////

    fun logAvatarDownloaded(from: AmplitudePropertyFrom) = amplitudeHelper.logAvatarDownloaded(from)

    fun logCreatePostClick(where: AmplitudePropertyWhere, whichButton: AmplitudeCreatePostWhichButton) =
        amplitudeHelper.logCreatePostClick(where = where, whichButton = whichButton)

    fun logPostCreated(
        postId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        postType: AmplitudePropertyPostType,
        postContentType: AmplitudePropertyContentType,
        haveText: Boolean,
        havePic: Boolean,
        haveVideo: Boolean,
        haveGif: Boolean,
        commentsSettings: AmplitudePropertyCommentsSettings,
        haveMusic: Boolean,
        videoDurationSec: Int,
        haveBackground: Boolean,
        backgroundId: Int
    ) = amplitudeHelper.logPostCreated(
        postId = postId,
        authorId = authorId,
        where = where,
        postType = postType,
        postContentType = postContentType,
        haveText = haveText,
        havePic = havePic,
        haveVideo = haveVideo,
        haveGif = haveGif,
        commentsSettings = commentsSettings,
        haveMusic = haveMusic,
        videoDurationSec = videoDurationSec,
        haveBackground = haveBackground,
        backgroundId = backgroundId
    )

    fun logPostOtherEvents(
        postId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        postType: AmplitudePropertyPostType,
        postContentType: AmplitudePropertyContentType,
        haveText: Boolean,
        havePic: Boolean,
        haveVideo: Boolean,
        haveGif: Boolean,
        commentsSettings: AmplitudePropertyCommentsSettings,
        haveMusic: Boolean,
        videoDurationSec: Int,
    ) = amplitudeHelper.logPostOtherEvents(
        postId = postId,
        authorId = authorId,
        where = where,
        postType = postType,
        postContentType = postContentType,
        haveText = haveText,
        havePic = havePic,
        haveVideo = haveVideo,
        haveGif = haveGif,
        commentsSettings = commentsSettings,
        haveMusic = haveMusic,
        videoDurationSec = videoDurationSec
    )

    fun logPostEdited(
        postId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        textChange: Boolean,
        picChange: Boolean,
        videoChange: Boolean,
        musicChange: Boolean,
        backgroundChange: Boolean,
    ) = amplitudeHelper.logEditPost(
        postId = postId,
        authorId = authorId,
        where = where,
        textChange = textChange,
        picChange = picChange,
        videoChange = videoChange,
        musicChange = musicChange,
        backgroundChange = backgroundChange
    )

    fun logOpenCustomFeed() = amplitudeHelper.logOpenCustomFeed()

    fun logOpenMainFeed(recFeed: Boolean) = amplitudeHelper.logOpenMainFeed(recFeed)

    fun logOpenFollowFeed(hasPosts: Boolean) = amplitudeHelper.logOpenFollowFeed(hasPosts)

    fun logTransportAdd(vehicleName: AmplitudePropertyVehicleType) = amplitudeHelper.logTransportAdd(vehicleName)

    fun logUpdateBtnClicked() = amplitudeHelper.logUpdateBtnClicked()

    fun logUpdateBtnShown() = amplitudeHelper.logUpdateBtnShown()

    fun logPressMoreText(
        postId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        postType: AmplitudePropertyPostType,
        openType: AmplitudePropertyOpenType
    ) = amplitudeHelper.logPressMoreText(
        postId = postId,
        authorId = authorId,
        where = where,
        postType = postType,
        openType = openType,
    )

    fun logShareProfile(userId: Long?, shareType: AmplitudePropertyProfileShare) = amplitudeHelper.logShareProfile(
        userId = userId,
        shareType = shareType
    )

    fun logSendGift(
        productId: String,
        fromId: String,
        toId: String,
        sendBack: AmplitudePropertyGiftSendBack,
        where: AmplitudePropertyWhere
    ) = amplitudeHelper.logSendGift(
        productId = productId,
        fromId = fromId,
        toId = toId,
        sendBack = sendBack,
        where = where,
    )

    fun logAvatarPickerOpen() = amplitudeHelper.logAvatarPickerOpen()

    fun logBuyVipStatus(
        color: AmplitudePropertyColor,
        duration: AmplitudePropertyDuration,
        expirationDate: String,
        haveVipBefore: AmplitudePropertyHaveVIPBefore,
        way: AmplitudePropertyWay
    ) = amplitudeHelper.logBuyVipStatus(
        color = color,
        duration = duration,
        expirationDate = expirationDate,
        haveVipBefore = haveVipBefore,
        way = way,
    )

    fun logCallsPermission(whoCanCall: AmplitudePropertyCallsSettings) = amplitudeHelper.logCallsPermission(whoCanCall)

    fun logRegistrationCompleted(
        regType: String,
        countryNumber: String,
        age: Int,
        hideAge: Boolean,
        gender: AmplitudePropertyRegistrationGender,
        hideGender: Boolean,
        country: String,
        city: String,
        photoType: AmplitudePropertyRegistrationAvatarPhotoType,
        uniqueNameChange: Boolean,
        haveReferral: Boolean,
        inviterId: Int
    ) = amplitudeHelper.logRegistrationCompleted(
        regType = regType,
        countryNumber = countryNumber,
        age = age,
        hideAge = hideAge,
        gender = gender,
        hideGender = hideGender,
        country = country,
        city = city,
        photoType = photoType,
        uniqueNameChange = uniqueNameChange,
        haveReferral = haveReferral,
        inviterId = inviterId
    )

    fun logCodeEnter(
        inputTime: String,
        incorrectCount: Int,
        requestCount: Int,
        country: String? = null,
        number: String? = null,
        email: String? = null
    ) = amplitudeHelper.logCodeEnter(
        inputTime = inputTime,
        incorrectCount = incorrectCount,
        requestCount = requestCount,
        country = country ?: "0",
        number = number ?: "0",
        email = email ?: "0"
    )

    fun logLoginFinished() = amplitudeHelper.logLoginFinished()

    fun logRegistration(
        inputType: AmplitudePropertyInputType,
        country: String? = null,
        number: String? = null,
        email: String? = null
    ) = amplitudeHelper.logRegistration(inputType, country ?: "0", number ?: "0", email ?: "0")

    fun logOnboarding(onboarding: AmplitudePropertyOnboarding) = amplitudeHelper.logOnboarding(onboarding)

    fun logFirstTimeOpen() = amplitudeHelper.logFirstTimeOpen()

    fun logOpenMap(where: AmplitudePropertyWhereOpenMap) = amplitudeMap.logOpenMap(where)

    fun logNotificationDelete() = amplitudeHelper.logNotificationDelete()

    fun logNotificationEnabled(isEnabled: Boolean) = amplitudeHelper.logNotificationEnabled(isEnabled)

    fun logAllNotificationsDeleted() = amplitudeHelper.logAllNotificationsDeleted()

    fun logPostMenuAction(
        actionType: AmplitudePropertyMenuAction,
        authorId: Long,
        where: AmplitudePropertyWhere,
        whosePost: AmplitudePropertyWhosePost,
        whence: AmplitudePropertyWhence,
        saveType: AmplitudePropertySaveType,
        recFeed: Boolean
    ) = amplitudeHelper.logPostMenuAction(
        actionType = actionType,
        authorId = authorId,
        where = where,
        whosePost = whosePost,
        whence = whence,
        saveType = saveType,
        recFeed = recFeed
    )

    fun logNumberSearch(
        transportType: AmplitudePropertyTransportType,
        country: String,
        fullness: AmplitudePropertyFullness,
        charCount: String,
        haveResult: AmplitudePropertyHaveResult
    ) = amplitudeHelper.logNumberSearch(
        transportType = transportType,
        country = country,
        fullness = fullness,
        charCount = charCount,
        haveResult = haveResult,
    )

    fun logFeedScroll(roadType: AmplitudePropertyRoadType, recFeed: Boolean) =
        amplitudeHelper.logFeedScroll(roadType, recFeed)

    fun logForceUpdate(actionType: AmplitudePropertyActionType) = amplitudeHelper.logForceUpdate(actionType)

    fun logSendGiftBack() = amplitudeHelper.logSendGiftBack()

    fun logNewYearCandyCount(count: AmplitudePropertyCandyCount) = amplitudeHelper.logNewYearCandyCount(count)

    fun logChatGifButtonPress() = amplitudeHelper.logChatGifButtonPress()

    fun logPrivacySettings(property: AmplitudeProperty) = amplitudeHelper.logPrivacySettings(property)

    fun logAddFriend(
        from: Long,
        to: Long,
        type: FriendAddAction
    ) = amplitudeHelper.logAddFriend(
        from = from,
        to = to,
        type = type,
    )

    fun logDelFriend(from: Long, to: Long) = amplitudeHelper.logDelFriend(
        from = from,
        to = to
    )

    fun logUnsubscribeUser(from: Long, to: Long) = amplitudeHelper.logUnsubscribeUser(
        from = from,
        to = to
    )

    fun logUserStatus(empty: Boolean) = amplitudeHelper.logUserStatus(empty)

    fun logAnimatedAvatarOpen(property: AmplitudePropertyAnimatedAvatarFrom) =
        amplitudeHelper.logAnimatedAvatarOpen(property)

    fun logAnimatedAvatarCreated(property: AmplitudePropertyAnimatedAvatarFrom) =
        amplitudeHelper.logAnimatedAvatarCreated(property)

    fun logPhotoSelection(type: AmplitudePropertyAvatarType, avatarCreationTime: String?) =
        amplitudeHelper.logPhotoSelection(
            type = type,
            avatarCreationTime = avatarCreationTime,
        )

    fun logCommunityShare(
        where: AmplitudePropertyCommunityWhere,
        groupId: Long,
        communityType: AmplitudePropertyCommunityType,
        canWrite: AmplitudePropertyCanWrite,
        havePhoto: AmplitudePropertyHavePhoto
    ) = amplitudeHelper.logCommunityShare(
        where = where,
        groupId = groupId,
        communityType = communityType,
        canWrite = canWrite,
        havePhoto = havePhoto,
    )

    fun logAvatarDownloaded(
        from: AmplitudePropertyAvatarDownloadFrom,
        type: AmplitudePropertyAvatarPhotoType
    ) = amplitudeHelper.logAvatarDownloaded(
        from = from,
        type = type
    )

    fun logVoiceMessageRecognitionTap(
        messageId: String,
        type: AmplitudePropertyRecognizedTextButton,
        duration: Long
    ) = amplitudeHelper.logVoiceMessageRecognitionTap(
        messageId = messageId,
        type = type,
        duration = duration,
    )

    fun logRegistrationHelpPressed(property: AmplitudePropertyHelpPressedWhere) =
        amplitudeHelper.logRegistrationHelpPressed(property)

    fun logRegistrationClose(step: AmplitudePropertyRegistrationStep) = amplitudeHelper.logRegistrationClose(step)

    fun logRegistrationNameEntered() = amplitudeHelper.logRegistrationNameEntered()

    fun logRegistrationBirthdayEntered(age: Int, hideAge: AmplitudePropertyRegistrationBirthdayHide) =
        amplitudeHelper.logRegistrationBirthdayEntered(age = age, hideAge = hideAge)

    fun logRegistrationGenderSelected(
        gender: AmplitudePropertyRegistrationGender,
        hideGender: AmplitudePropertyRegistrationGenderHide
    ) = amplitudeHelper.logRegistrationGenderSelected(gender = gender, hideGender = hideGender)

    fun logRegistrationLocationSelected(autocomplete: AmplitudePropertyRegistrationLocationAutocomplete) =
        amplitudeHelper.logRegistrationLocationSelected(autocomplete)

    fun logRegistrationPhotoUniqueName(
        photoType: AmplitudePropertyRegistrationAvatarPhotoType,
        avatarTime: String,
        haveReferral: AmplitudePropertyRegistrationAvatarHaveReferral
    ) = amplitudeHelper.logRegistrationPhotoUniqueName(
        photoType = photoType,
        avatarTime = avatarTime,
        haveReferral = haveReferral,
    )

    fun logUserProfileDelete(userId: Long, reasonId: Int) =
        amplitudeHelper.logUserProfileDelete(userId = userId, reasonId = reasonId)

    fun logUserProfileRestore(userId: Long) = amplitudeHelper.logUserProfileRestore(userId)

    fun logHashTagPress(
        where: AmplitudePropertyWhere,
        postId: Long = -1L,
        authorId: Long = -1L,
    ) = amplitudeHelper.logHashTagPress(where, postId, authorId)

    fun logUnderstandablyPress() = amplitudeHelper.logUnderstandablyPress()

    fun logGroupDescriptionChange() = amplitudeHelper.logGroupDescriptionChange()

    fun logGroupTitleChange() = amplitudeHelper.logGroupTitleChange()

    fun logPostDeleted(
        postItem: AnalyticsPost,
        whereFrom: AmplitudePropertyWhere,
    ) = amplitudeHelper.logPostDeleted(postItem = postItem, whereFrom = whereFrom)

    fun logUserExit(userId: Long) = amplitudeHelper.logUserExit(userId)

    fun logBlockUser(userId: Long, blockedUserId: Long) = amplitudeHelper.logBlockUser(
        userId = userId,
        blockedUserId = blockedUserId,
    )

    fun logUnblockUser(userId: Long, unBlockedUserId: Long) = amplitudeHelper.logUnblockUser(
        userId = userId,
        unBlockedUserId = unBlockedUserId,
    )

    fun logTetATetChatCreated(
        userId: Long,
        companionUserId: Long,
        status: AmplitudePropertyChatUserChatStatus,
        where: AmplitudePropertyChatCreatedFromWhere
    ) = amplitudeHelper.logTetATetChatCreated(
        userId = userId,
        companionUserId = companionUserId,
        status = status,
        where = where,
    )

    fun logTogglePress(
        userId: Long,
        companionUserId: Long,
        state: AmplitudePropertyChatCallSwitcherPosition
    ) = amplitudeHelper.logTogglePress(
        userId = userId,
        companionUserId = companionUserId,
        state = state,
    )

    fun logPushAnswerTap(userId: Long, senderId: Long) =
        amplitudeHelper.logPushAnswerTap(userId = userId, senderId = senderId)

    fun logPostShareOpen(
        postId: Long,
        authorId: Long,
        momentId: Long,
        where: AmplitudePropertyWhere,
        recFeed: Boolean,
        publicType: AmplitudePropertyPublicType
    ) = amplitudeHelper.logPostShareOpen(
        postId = postId,
        authorId = authorId,
        momentId = momentId,
        where = where,
        recFeed = recFeed,
        publicType = publicType
    )

    fun logPostShare(
        analyticsPostShare: AnalyticsPostShare,
        recFeed: Boolean
    ) = amplitudeHelper.logPostShare(
        analyticsPostShare = analyticsPostShare,
        recFeed = recFeed
    )

    fun logPostShareClose(bottomSheetCloseMethod: AmplitudePropertyBottomSheetCloseMethod) =
        amplitudeHelper.logPostShareClose(bottomSheetCloseMethod)

    fun logPostShareSettingsTap(where: AmplitudePropertyWhere) = amplitudeHelper.logPostShareSettingsTap(where)

    fun logMapPrivacySettingsBlacklist(
        where: AmplitudePropertyWhereMapPrivacy,
        addCount: Int,
        deleteCount: Int
    ) = amplitudeHelper.logMapPrivacySettingsBlacklist(
        where = where,
        addCount = addCount,
        deleteCount = deleteCount
    )

    fun logMapPrivacySettingsWhitelist(
        where: AmplitudePropertyWhereMapPrivacy,
        addCount: Int,
        deleteCount: Int
    ) = amplitudeHelper.logMapPrivacySettingsWhitelist(
        where = where,
        addCount = addCount,
        deleteCount = deleteCount
    )

    fun logMapPrivacySettingsDeleteAll(count: Int, listType: AmplitudePropertyMapPrivacyListType) =
        amplitudeHelper.logMapPrivacySettingsDeleteAll(
            count = count,
            listType = listType
        )

    fun logCall(haveVideo: Boolean, duration: String, callType: AmplitudePropertyCallType) =
        amplitudeHelper.logCall(
            haveVideo = haveVideo,
            duration = duration,
            callType = callType,
        )

    fun logCallCancel(who: AmplitudePropertyCallCanceller) = amplitudeHelper.logCallCancel(who)

    fun logChatRequest(
        fromUid: Long,
        toUid: Long,
        actionType: AmplitudePropertyActionTypeChatRequest
    ) = amplitudeHelper.logChatRequest(
        fromUid = fromUid,
        toUid = toUid,
        actionType = actionType,
    )

    //TODO: удалить после тестирования https://nomera.atlassian.net/browse/BR-21050
    fun logFirebaseOnNewToken(token: String) = amplitudeHelper.logFirebaseOnNewToken(token)

    //TODO: удалить после тестирования https://nomera.atlassian.net/browse/BR-21050
    fun logFirebaseGetMessage(msg: String) = amplitudeHelper.logFirebaseGetMessage(msg)
}

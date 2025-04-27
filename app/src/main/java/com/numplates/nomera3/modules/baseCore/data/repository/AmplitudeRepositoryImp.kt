package com.numplates.nomera3.modules.baseCore.data.repository

import android.annotation.SuppressLint
import com.amplitude.api.AmplitudeClient
import com.amplitude.api.Identify
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudeCreatePostWhichButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudeEventName
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionPlayStopMusic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionTypeChatRequest
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAnimatedAvatarFrom
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarDownloadFrom
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarPhotoType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyBackType
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
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyDeleteProfileReason
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyDuration
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyFrom
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyFullness
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyGeoEnabled
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
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.ACTION_TYPE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.AGE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.AVATAR_CREATION_TIME
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.COMMUNITY_ID
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.COUNTRY
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.COUNTRY_NUMBER
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.EMAIL
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.HIDE_AGE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.HIDE_GENDER
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.INVITER_ID
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.NUMBER
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.PHOTO_TYPE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.REGISTRATION_PHOTO_HAVE_REFERRAL
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.REG_TYPE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.UNIQUE_NAME_CHANGE
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
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereReaction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPost
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPostShare
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsUser
import com.numplates.nomera3.modules.baseCore.helper.amplitude.CITY
import com.numplates.nomera3.modules.baseCore.helper.amplitude.ComplainExtraActions
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.GENDER
import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.ComplainType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyWhosePost
import com.numplates.nomera3.modules.baseCore.helper.amplitude.toAmplitudeUser
import com.numplates.nomera3.modules.baseCore.helper.amplitude.toAppMetricaUser
import com.numplates.nomera3.modules.newroads.fragments.CustomRoadFragment
import com.yandex.metrica.YandexMetrica
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class AmplitudeRepositoryImpl @Inject constructor(
    private val client: AmplitudeClient
) : AmplitudeRepository {

    override fun setUser(user: AnalyticsUser) {
        val amplitudeUser = user.toAmplitudeUser()
        Timber.d("Handle event: user = $amplitudeUser ")
        if (!BuildConfig.DEBUG) {
            client.userId = user.userId
            client.setUserProperties(amplitudeUser)
            if (!user.isAnon) {
                YandexMetrica.setUserProfileID(user.userId)
                YandexMetrica.reportUserProfile(user.toAppMetricaUser())
            }
        }
    }

    override fun logMapPrivacySettingsSetup(
        where: AmplitudePropertyWhereMapPrivacy,
        visibility: AmplitudePropertySettingVisibility
    ) {
        logEvent(
            eventName = AmplitudeEventName.PRIVACY_MAP_CHANGED,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(visibility)
                }
            }
        )
    }

    override fun logMapPrivacySettingsClicked(where: AmplitudePropertyWhereMapPrivacy) {
        logEvent(
            eventName = AmplitudeEventName.PRIVACY_MAP_CLICK,
            properties = { it.apply { addProperty(where) } }
        )
    }

    override fun logUnlockChat(from: Long, to: Long) {
        logEvent(
            eventName = AmplitudeEventName.CHAT_UNLOCK,
            properties = {
                AmplitudePropertyNameConst.FROM
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, from)
                    addProperty(AmplitudePropertyNameConst.TO, to)
                }
            }
        )
    }

    override fun logForwardMessageClicked() {
        logEvent(eventName = AmplitudeEventName.MESSAGE_FORWARD_TAP)
    }

    override fun logForwardMessage(
        chatCount: Int,
        groupCount: Int,
        haveAddText: Boolean,
        havePostText: Boolean,
        havePostPic: Boolean,
        havePostVideo: Boolean,
        havePostGif: Boolean,
        havePostMusic: Boolean,
        havePostMedia: Boolean
    ) {
        logEvent(
            eventName = AmplitudeEventName.MESSAGE_FORWARD_SEND,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.HAVE_ADD_TEXT, haveAddText)
                    addProperty(AmplitudePropertyNameConst.HAVE_TEXT, havePostText)
                    addProperty(AmplitudePropertyNameConst.HAVE_PIC, havePostPic)
                    addProperty(AmplitudePropertyNameConst.HAVE_VIDEO, havePostVideo)
                    addProperty(AmplitudePropertyNameConst.HAVE_GIF, havePostGif)
                    addProperty(AmplitudePropertyNameConst.HAVE_MEDIA, havePostMedia)
                    addProperty(AmplitudePropertyNameConst.HAVE_AUDIO, havePostMusic)
                    addProperty(AmplitudePropertyNameConst.CHAT_COUNT, chatCount)
                    addProperty(AmplitudePropertyNameConst.GROUP_COUNT, groupCount)
                }
            }
        )
    }

    override fun logBottomBarChatClicked() {
        logEvent(eventName = AmplitudeEventName.BOTTOM_BAR_COMMUNICATION)
    }

    override fun logBottomBarCommunityClicked() {
        logEvent(eventName = AmplitudeEventName.BOTTOM_BAR_COMMUNITY)
    }

    override fun logBottomBarProfileClicked() {
        logEvent(eventName = AmplitudeEventName.BOTTOM_BAR_PROFILE)
    }

    override fun logComplainExtraAction(actions: ComplainExtraActions) {
        logEvent(
            eventName = AmplitudeEventName.REPORT_SECOND_ACTION,
            properties = {
                it.apply {
                    addProperty(actions)
                }
            }
        )
    }

    override fun logComplain(type: ComplainType, from: Long, to: Long) {
        logEvent(
            eventName = AmplitudeEventName.COMPLAIN,
            properties = {
                it.apply {
                    addProperty(type)
                    addProperty(AmplitudePropertyNameConst.REPORT_FROM, from)
                    addProperty(AmplitudePropertyNameConst.REPORT_TO, to)
                }
            }
        )
    }

    @SuppressLint("BinaryOperationInTimber")
    override fun logBottomBarRoad(
        roadType: AmplitudePropertyRoadType,
        recFeed: Boolean,
        roadHowWasOpened: AmplitudePropertyHowWasOpened
    ) {
        logEvent(
            eventName = AmplitudeEventName.BOTTOM_BAR_ROAD,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FEED_TYPE, roadType.property)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                    addProperty(AmplitudePropertyNameConst.REC_FEED_HOW, roadHowWasOpened.property)
                }
            }
        )
    }

    override fun logPlayStopMusic(
        actionType: AmplitudePropertyActionPlayStopMusic,
        where: AmplitudePropertyWhereMusicPlay
    ) {
        logEvent(
            eventName = AmplitudeEventName.MUSIC_PLAY,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(where)
                }
            }
        )
    }

    override fun logMusicAddPress() {
        logEvent(eventName = AmplitudeEventName.MUSIC_ADD_PRESS)
    }

    override fun logBackPressed() {
        logEvent(eventName = AmplitudeEventName.TEST_BACK_PRESS,
            properties = {
                it.apply {
                    addProperty(ACTION_TYPE, AmplitudePropertyBackType.BACK.property)
                }
            }
        )
    }

    override fun logBackSwipe() {
        logEvent(eventName = AmplitudeEventName.TEST_BACK_PRESS,
            properties = {
                it.apply {
                    addProperty(ACTION_TYPE, AmplitudePropertyBackType.SWIPE.property)
                }
            }
        )
    }

    override fun reactionPanelOpen() {
        logEvent(
            eventName = AmplitudeEventName.REACTION_PANEL_OPEN
        )
    }

    override fun reactionToComment(
        postId: Long,
        where: AmplitudePropertyWhereReaction,
        type: AmplitudePropertyReactionType,
        whence: AmplitudePropertyWhence,
        commentUserId: Long,
        publicationUserId: Long,
        momentId: Long
    ) {
        logEvent(
            eventName = AmplitudeEventName.REACTION_TO_COMMENT,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, postId)
                    addProperty(where)
                    addProperty(type)
                    addProperty(whence)
                    addProperty(AmplitudePropertyNameConst.COMMENTOR_ID, commentUserId)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, publicationUserId)
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, momentId)
                }
            }
        )
    }

    override fun identifyUserProperty(properties: (Identify) -> Identify) {
        client.identify(properties(Identify()))
    }

    override fun logFilterMainRoad(country: String, city: String, recFeed: Boolean) {
        logEvent(
            eventName = AmplitudeEventName.FEED_FILTER_OPEN,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.COUNTRY_TYPE, country)
                    addProperty(AmplitudePropertyNameConst.CITY_TYPE, city)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                }
            }
        )
    }

    override fun logClickSearchButtonOnMainRoad() {
        logEvent(eventName = AmplitudeEventName.SEARCH_MENU_OPEN)
    }

    override fun logSearchInput(
        type: AmplitudePropertySearchType,
        haveResult: AmplitudePropertyHaveResult,
        whereCommunitySearch: AmplitudePropertyWhereCommunitySearch,
        whereFriendsSearch: AmplitudePropertyWhereFriendsSearch
    ) {
        logEvent(
            eventName = AmplitudeEventName.SEARCH_INPUT,
            properties = {
                it.apply {
                    addProperty(type)
                    addProperty(haveResult)
                    addProperty(whereCommunitySearch)
                    addProperty(whereFriendsSearch)
                }
            }
        )
    }

    override fun logTapSearchAtSign() {
        logEvent(eventName = AmplitudeEventName.SEARCH_AT_SIGN)
    }

    override fun logTapSearchByNumberButton() {
        logEvent(eventName = AmplitudeEventName.SEARCH_BY_NUMBER_BUTTON_TAP)
    }

    override fun logEmojiTap(emojiType: String) {
        logEvent(
            eventName = AmplitudeEventName.EMOJI_TAP,
            properties = {
                it.apply { addProperty(AmplitudePropertyNameConst.EMOJI_TYPE, emojiType) }
            }
        )
    }

    override fun logCommunityCreateMenuOpen() {
        logEvent(eventName = AmplitudeEventName.COMMUNITY_CREATE_MENU_OPEN)
    }

    override fun logCommunityCreated(
        type: AmplitudePropertyCommunityType,
        whoCanWrite: AmplitudePropertyCanWrite,
        havePhoto: AmplitudePropertyHavePhoto
    ) {
        logEvent(
            eventName = AmplitudeEventName.COMMUNITY_CREATED,
            properties = {
                it.apply {
                    addProperty(type)
                    addProperty(whoCanWrite)
                    addProperty(havePhoto)
                }
            }
        )
    }

    override fun logCommunityDeleted() {
        logEvent(eventName = AmplitudeEventName.COMMUNITY_DELETED)
    }

    override fun logCommunityScreenOpened(where: AmplitudePropertyWhereCommunityOpen) {
        logEvent(
            eventName = AmplitudeEventName.COMMUNITY_OPEN,
            properties = {
                it.apply { addProperty(where) }
            }
        )
    }

    override fun logCommunityFollow(
        userId: Long,
        where: AmplitudePropertyWhereCommunityFollow,
        communityId: Int
    ) {
        logEvent(
            eventName = AmplitudeEventName.COMMUNITY_FOLLOW,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(where)
                    addProperty(COMMUNITY_ID, communityId)
                }
            }
        )
    }

    override fun logCommunityUnfollow(userId: Long, communityId: Int) {
        logEvent(
            eventName = AmplitudeEventName.COMMUNITY_UNFOLLOW,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(COMMUNITY_ID, communityId)
                }
            }
        )
    }

    override fun logChatOpen(chatType: AmplitudePropertyChatType, where: AmplitudePropertyWhere) {
        logEvent(
            eventName = AmplitudeEventName.CHAT_OPEN,
            properties = {
                it.apply { addProperty(where) }
                it.apply { addProperty(chatType) }
            }
        )
    }

    override fun logGroupChatCreate(
        havePhoto: AmplitudePropertyHavePhoto,
        haveDescription: AmplitudePropertyHaveDescription
    ) {
        logEvent(
            eventName = AmplitudeEventName.GROUP_CHAT_CREATE,
            properties = {
                it.apply {
                    addProperty(havePhoto)
                    addProperty(haveDescription)
                }
            }
        )
    }

    override fun logGroupChatDelete() {
        logEvent(eventName = AmplitudeEventName.GROUP_CHAT_DELETE)
    }

    override fun logMessageSend(
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
        status: AmplitudePropertyChatUserChatStatus,
        mediaKeyboardCategory: AmplitudePropertyChatMediaKeyboardCategory,
        messageId: String
    ) {
        logEvent(
            eventName = AmplitudeEventName.MESSAGE_SEND,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.HAVE_TEXT, haveText)
                    addProperty(AmplitudePropertyNameConst.HAVE_PIC, havePic)
                    addProperty(AmplitudePropertyNameConst.HAVE_VIDEO, haveVideo)
                    addProperty(AmplitudePropertyNameConst.HAVE_AUDIO, haveAudio)
                    addOrSkipProperty(AmplitudePropertyNameConst.DURATION, duration)
                    addProperty(AmplitudePropertyNameConst.HAVE_GIF, haveGif)
                    addProperty(haveMedia)
                    addProperty(AmplitudePropertyNameConst.GROUP_CHAT, isGroupChat)
                    addProperty(AmplitudePropertyNameConst.FROM, from)
                    addProperty(AmplitudePropertyNameConst.TO, to)
                    addProperty(status)
                    addProperty(AmplitudePropertyNameConst.MESSAGE_ID_SPACING, messageId)
                    addProperty(mediaKeyboardCategory)
                }
            }
        )
    }


    ///////////////////////////////////////////////////////////////////////////
    // OLD
    ///////////////////////////////////////////////////////////////////////////

    override fun logAvatarDownloaded(from: AmplitudePropertyFrom) {
        logEvent(
            eventName = AmplitudeEventName.AVATAR_DOWNLOADED,
            properties = {
                it.apply { addProperty(from) }
            }
        )
    }

    override fun logCreatePostClick(where: AmplitudePropertyWhere, whichButton: AmplitudeCreatePostWhichButton) {
        logEvent(
            eventName = AmplitudeEventName.BUTTON_POST_CREATE_TAP,
            properties = {
                it.apply {
                    addProperty(whichButton)
                    addProperty(where)
                }
            }
        )
    }

    override fun logPostCreated(
        postId: Long,
        where: AmplitudePropertyWhere,
        postType: AmplitudePropertyPostType,
        postContentType: AmplitudePropertyContentType,
        authorId: Long,
        haveText: Boolean,
        havePic: Boolean,
        haveVideo: Boolean,
        haveGif: Boolean,
        commentsSettings: AmplitudePropertyCommentsSettings,
        haveMusic: Boolean,
        videoDurationSec: Int,
        haveBackground: Boolean,
        backgroundId: Int
    ) {
        logEvent(
            eventName = AmplitudeEventName.POST_CREATED,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, postId)
                    addProperty(where)
                    addProperty(postType)
                    addProperty(postContentType)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                    addProperty(AmplitudePropertyNameConst.HAVE_TEXT, haveText)
                    addProperty(AmplitudePropertyNameConst.HAVE_PIC, havePic)
                    addProperty(AmplitudePropertyNameConst.HAVE_VIDEO, haveVideo)
                    addProperty(AmplitudePropertyNameConst.HAVE_GIF, haveGif)
                    addProperty(AmplitudePropertyNameConst.VIDEO_DURATION, videoDurationSec)
                    addProperty(commentsSettings)
                    addProperty(AmplitudePropertyNameConst.HAVE_MUSIC, haveMusic)
                    addProperty(AmplitudePropertyNameConst.HAVE_BACKGROUND, haveBackground)
                    addProperty(AmplitudePropertyNameConst.BACKGROUND_ID, backgroundId)
                }
            }
        )
    }

    override fun logPostOtherEvents(
        postId: Long,
        where: AmplitudePropertyWhere,
        postType: AmplitudePropertyPostType,
        postContentType: AmplitudePropertyContentType,
        authorId: Long,
        haveText: Boolean,
        havePic: Boolean,
        haveVideo: Boolean,
        haveGif: Boolean,
        commentsSettings: AmplitudePropertyCommentsSettings,
        haveMusic: Boolean,
        videoDurationSec: Int
    ) {
        logEvent(
            // TODO BR-24356 https://nomera.atlassian.net/browse/BR-26247
            //  [Android] Прорисовка таксономии событий по задаче "Post background"
            //  Уточнить у аналитиков, так как событие вызывается для любого действия поста
            eventName = AmplitudeEventName.POST_CREATED,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, postId)
                    addProperty(where)
                    addProperty(postType)
                    addProperty(postContentType)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                    addProperty(AmplitudePropertyNameConst.HAVE_TEXT, haveText)
                    addProperty(AmplitudePropertyNameConst.HAVE_PIC, havePic)
                    addProperty(AmplitudePropertyNameConst.HAVE_VIDEO, haveVideo)
                    addProperty(AmplitudePropertyNameConst.HAVE_GIF, haveGif)
                    addProperty(AmplitudePropertyNameConst.VIDEO_DURATION, videoDurationSec)
                    addProperty(commentsSettings)
                    addProperty(AmplitudePropertyNameConst.HAVE_MUSIC, haveMusic)
                }
            }
        )
    }

    override fun logEditPost(
        postId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        textChange: Boolean,
        picChange: Boolean,
        videoChange: Boolean,
        musicChange: Boolean,
        backgroundChange: Boolean
    ) {
        logEvent(
            eventName = AmplitudeEventName.POST_EDITED,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, postId)
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                    addProperty(AmplitudePropertyNameConst.TEXT_CHANGE, textChange)
                    addProperty(AmplitudePropertyNameConst.PIC_CHANGE, picChange)
                    addProperty(AmplitudePropertyNameConst.VIDEO_CHANGE, videoChange)
                    addProperty(AmplitudePropertyNameConst.MUSIC_CHANGE, musicChange)
                    addProperty(AmplitudePropertyNameConst.BACKGROUND_CHANGE, backgroundChange)
                }
            }
        )
    }

    override fun logOpenCustomFeed() {
        logEvent(eventName = CustomRoadFragment.ConfigConst.AMPLITUDE_SCREEN_OPEN)
    }

    override fun logOpenMainFeed(recFeed: Boolean) {
        logEvent(
            eventName = AmplitudeEventName.OPEN_MAIN_FEED,
            properties = { json ->
                json.apply {
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                }
            })
    }

    override fun logOpenFollowFeed(hasPosts: Boolean) {
        logEvent(
            eventName = AmplitudeEventName.OPEN_FOLLOW_FEED,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.HAVE_POSTS, hasPosts)
                }
            }
        )
    }

    override fun logTransportAdd(vehicleName: AmplitudePropertyVehicleType) {
        logEvent(
            eventName = AmplitudeEventName.TRANSPORT_ADD,
            properties = {
                it.apply {
                    addProperty(vehicleName)
                }
            }
        )
    }

    override fun logUpdateBtnClicked() {
        logEvent(eventName = AmplitudeEventName.UPDATE_BTN_CLICKED)
    }

    override fun logUpdateBtnShown() {
        logEvent(eventName = AmplitudeEventName.UPDATE_BTN_SHOWN)
    }

    override fun logPressMoreText(
        postId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        postType: AmplitudePropertyPostType,
        openType: AmplitudePropertyOpenType
    ) {
        logEvent(
            eventName = AmplitudeEventName.PRESS_MORE_POST,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, postId)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                    addProperty(where)
                    addProperty(postType)
                    addProperty(openType)
                }
            }
        )
    }

    override fun logShareProfile(userId: Long?, shareType: AmplitudePropertyProfileShare) {
        logEvent(
            eventName = AmplitudeEventName.PROFILE_SHARE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.ID, userId ?: 0L)
                    addProperty(shareType)
                }
            }
        )
    }

    override fun logSendGift(
        productId: String,
        fromId: String,
        toId: String,
        sendBack: AmplitudePropertyGiftSendBack,
        where: AmplitudePropertyWhere
    ) {
        logEvent(
            eventName = AmplitudeEventName.SEND_GIFT,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.GIFT_TYPE, productId)
                    addProperty(AmplitudePropertyNameConst.GIFT_FROM, fromId)
                    addProperty(AmplitudePropertyNameConst.GIFT_TO, toId)
                    addProperty(sendBack)
                    addProperty(where)
                }
            }
        )
    }

    override fun logAvatarPickerOpen() {
        logEvent(eventName = AmplitudeEventName.OPEN_PICKER_AVATAR_CHANGE)
    }

    override fun logBuyVipStatus(
        color: AmplitudePropertyColor,
        duration: AmplitudePropertyDuration,
        expirationDate: String,
        haveVipBefore: AmplitudePropertyHaveVIPBefore,
        way: AmplitudePropertyWay
    ) {
        logEvent(
            eventName = AmplitudeEventName.VIP_BUYING,
            properties = {
                it.apply {
                    addProperty(color)
                    addProperty(duration)
                    addProperty(haveVipBefore)
                    addProperty(AmplitudePropertyNameConst.EXPIRATION_DATE, expirationDate)
                    addProperty(way)
                }
            }
        )
    }

    override fun logCallsPermission(whoCanCall: AmplitudePropertyCallsSettings) {
        logEvent(
            eventName = AmplitudeEventName.CALLS_PERMISSION,
            properties = {
                it.apply {
                    addProperty(whoCanCall)
                }
            }
        )
    }

    override fun logRegistrationCompleted(
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
    ) {
        logEvent(eventName = AmplitudeEventName.REGISTRATION_COMPLETED, properties = {
            it.apply {
                addProperty(REG_TYPE, regType)
                addProperty(COUNTRY_NUMBER, countryNumber)
                addProperty(AGE, age)
                addProperty(HIDE_AGE, hideAge)
                addProperty(GENDER, gender)
                addProperty(HIDE_GENDER, hideGender)
                addProperty(COUNTRY, country)
                addProperty(CITY, city)
                addProperty(PHOTO_TYPE, photoType)
                addProperty(UNIQUE_NAME_CHANGE, uniqueNameChange)
                addProperty(REGISTRATION_PHOTO_HAVE_REFERRAL, haveReferral)
                addProperty(INVITER_ID, inviterId)
            }
        })
    }

    override fun logCodeEnter(
        inputTime: String,
        incorrectCount: Int,
        requestCount: Int,
        country: String,
        number: String,
        email: String
    ) {
        logEvent(
            eventName = AmplitudeEventName.CODE_ENTER,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.INPUT_TIME, inputTime)
                    addProperty(AmplitudePropertyNameConst.INCORRECT_COUNT, incorrectCount.toString())
                    addProperty(AmplitudePropertyNameConst.REQUEST_COUNT, requestCount)
                    addProperty(COUNTRY, country)
                    addProperty(NUMBER, number)
                    addProperty(EMAIL, email)
                }
            }
        )
    }

    //Проверяем был ли переход из экрана ввода кода
    private var isFromSmsFragment = false
    override fun logLoginFinished() {
        logEvent(eventName = AmplitudeEventName.LOGIN_FINISHED)
    }


    @Synchronized
    override fun setIsFromSms(isFromSms: Boolean) {
        isFromSmsFragment = isFromSms
    }

    override fun logRegistration(
        inputType: AmplitudePropertyInputType,
        country: String,
        number: String,
        email: String
    ) {
        logEvent(
            eventName = AmplitudeEventName.LOGIN,
            properties = {
                it.apply {
                    addProperty(inputType)
                    addProperty(COUNTRY, country)
                    addProperty(NUMBER, number)
                    addProperty(EMAIL, email)
                }
            }
        )
    }

    override fun logOnboarding(onboarding: AmplitudePropertyOnboarding) {
        logEvent(
            eventName = AmplitudeEventName.ONBOARDING,
            properties = {
                it.apply {
                    addProperty(onboarding)
                }
            }
        )
    }

    override fun logFirstTimeOpen() {
        logEvent(
            eventName = AmplitudeEventName.FIRST_TIME_OPEN
        )
    }

    override fun logNotificationDelete() {
        logEvent(
            eventName = AmplitudeEventName.DELETE_NOTIFICATION
        )
    }

    override fun logNotificationEnabled(isEnabled: Boolean) {
        val enabledValue = if (isEnabled) {
            AmplitudePropertyGeoEnabled.TRUE
        } else {
            AmplitudePropertyGeoEnabled.FALSE
        }

        logEvent(
            eventName = AmplitudeEventName.PUSH_TAP,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.PUSH_ENABLED, enabledValue)
                }
            }
        )
    }

    override fun logAllNotificationsDeleted() {
        logEvent(
            eventName = AmplitudeEventName.DELETE_ALL_NOTIFICATION
        )
    }

    override fun logPostMenuAction(
        actionType: AmplitudePropertyMenuAction,
        authorId: Long,
        where: AmplitudePropertyWhere,
        whosePost: AmplitudePropertyWhosePost,
        whence: AmplitudePropertyWhence,
        saveType: AmplitudePropertySaveType,
        recFeed: Boolean
    ) {
        logEvent(
            eventName = AmplitudeEventName.POST_MENU_ACTION,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                    addProperty(where)
                    addProperty(whosePost)
                    addProperty(whence)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                    addProperty(AmplitudePropertyNameConst.SAVE_TYPE, saveType)
                }
            }
        )
    }

    override fun logNumberSearch(
        transportType: AmplitudePropertyTransportType,
        country: String,
        fullness: AmplitudePropertyFullness,
        charCount: String,
        haveResult: AmplitudePropertyHaveResult
    ) {
        logEvent(
            eventName = AmplitudeEventName.SEARCH_BY_NUMBER,
            properties = {
                it.apply {
                    addProperty(transportType)
                    addProperty(fullness)
                    addProperty(haveResult)
                    addProperty(AmplitudePropertyNameConst.COUNTRY, country)
                    addProperty(AmplitudePropertyNameConst.CHAR_COUNT, charCount)
                }
            }
        )
    }

    override fun logFeedScroll(roadType: AmplitudePropertyRoadType, recFeed: Boolean) {
        logEvent(
            eventName = AmplitudeEventName.FEED_SCROLL,
            properties = {
                it.apply {
                    addProperty(roadType)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                }
            }
        )
    }

    override fun logForceUpdate(actionType: AmplitudePropertyActionType) {
        logEvent(
            eventName = AmplitudeEventName.POP_UP_SHOW,
            properties = {
                it.apply {
                    addProperty(actionType)
                }
            }
        )
    }

    override fun logSendGiftBack() {
        logEvent(eventName = AmplitudeEventName.SEND_GIFT_BACK_PRESS)
    }

    override fun logNewYearCandyCount(count: AmplitudePropertyCandyCount) {
        logEvent(
            eventName = AmplitudeEventName.NEW_YEAR_CANDY_COLLECT,
            properties = {
                it.apply {
                    addProperty(count)
                }
            }
        )
    }

    override fun logChatGifButtonPress() {
        logEvent(eventName = AmplitudeEventName.CHAT_GIF_BUTTON_PRESS)
    }

    override fun logPrivacySettings(property: AmplitudeProperty) {
        logEvent(
            eventName = AmplitudeEventName.PRIVACY_SETTINGS,
            properties = {
                it.apply {
                    addProperty(property)
                }
            }
        )
    }

    override fun logAddFriend(
        from: Long,
        to: Long,
        type: FriendAddAction
    ) {
        logEvent(
            eventName = AmplitudeEventName.FRIEND_ADD,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, from)
                    addProperty(AmplitudePropertyNameConst.TO, to)
                    addProperty(type)
                }
            }
        )
    }

    override fun logDelFriend(from: Long, to: Long) {
        logEvent(
            eventName = AmplitudeEventName.FRIEND_DEL,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, from)
                    addProperty(AmplitudePropertyNameConst.TO, to)
                }
            }
        )
    }

    override fun logUnsubscribeUser(from: Long, to: Long) {
        logEvent(
            eventName = AmplitudeEventName.UNSUBSCRIBE_USER,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, from)
                    addProperty(AmplitudePropertyNameConst.TO, to)
                }
            }
        )
    }

    override fun logUserStatus(empty: Boolean) {
        logEvent(
            eventName = AmplitudeEventName.CHANGE_STATUS,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.EMPTY, empty)
                }
            }
        )
    }

    override fun logAnimatedAvatarOpen(property: AmplitudePropertyAnimatedAvatarFrom) {
        logEvent(
            eventName = AmplitudeEventName.ANIMATED_AVATAR_OPENED,
            properties = {
                it.apply {
                    addProperty(property)
                }
            }
        )
    }

    override fun logAnimatedAvatarCreated(property: AmplitudePropertyAnimatedAvatarFrom) {
        logEvent(
            eventName = AmplitudeEventName.ANIMATED_AVATAR_CREATED,
            properties = {
                it.apply {
                    addProperty(property)
                }
            }
        )
    }

    override fun logPhotoSelection(type: AmplitudePropertyAvatarType, avatarCreationTime: String?) {
        logEvent(
            eventName = AmplitudeEventName.PHOTO_SELECTION,
            properties = {
                it.apply {
                    addProperty(type)
                    if (avatarCreationTime != null) addProperty(AVATAR_CREATION_TIME, avatarCreationTime)
                }
            }
        )
    }

    override fun logCommunityShare(
        where: AmplitudePropertyCommunityWhere,
        groupId: Long,
        communityType: AmplitudePropertyCommunityType,
        canWrite: AmplitudePropertyCanWrite,
        havePhoto: AmplitudePropertyHavePhoto
    ) {
        logEvent(
            eventName = AmplitudeEventName.COMMUNITY_SHARE,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(COMMUNITY_ID, groupId)
                    addProperty(communityType)
                    addProperty(canWrite)
                    addProperty(havePhoto)
                }
            }
        )
    }

    override fun logAvatarDownloaded(
        from: AmplitudePropertyAvatarDownloadFrom,
        type: AmplitudePropertyAvatarPhotoType
    ) {
        logEvent(
            eventName = AmplitudeEventName.AVATAR_DOWNLOADED,
            properties = {
                it.apply {
                    addProperty(from)
                    addProperty(type)
                }
            }
        )
    }

    override fun logVoiceMessageRecognitionTap(
        messageId: String,
        type: AmplitudePropertyRecognizedTextButton,
        duration: Long
    ) {
        logEvent(
            eventName = AmplitudeEventName.RECOGNITION_TAP,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.MESSAGE_ID, messageId)
                    addProperty(type)
                    addProperty(AmplitudePropertyNameConst.DURATION, duration)
                }
            }
        )
    }

    override fun logRegistrationHelpPressed(property: AmplitudePropertyHelpPressedWhere) {
        logEvent(
            eventName = AmplitudeEventName.REGISTRATION_HELP,
            properties = {
                it.apply {
                    addProperty(property)
                }
            }
        )
    }

    override fun logRegistrationClose(step: AmplitudePropertyRegistrationStep) {
        logEvent(
            eventName = AmplitudeEventName.REGISTRATION_CLOSE,
            properties = {
                it.apply {
                    addProperty(step)
                }
            }
        )
    }

    override fun logRegistrationNameEntered() {
        logEvent(eventName = AmplitudeEventName.REGISTRATION_NAME)
    }

    override fun logRegistrationBirthdayEntered(age: Int, hideAge: AmplitudePropertyRegistrationBirthdayHide) {
        logEvent(
            eventName = AmplitudeEventName.REGISTRATION_BIRTHDAY,
            properties = {
                it.apply {
                    addProperty(hideAge)
                    addProperty(AmplitudePropertyNameConst.REGISTRATION_BIRTHDAY_AGE, age)
                }
            }
        )
    }

    override fun logRegistrationGenderSelected(
        gender: AmplitudePropertyRegistrationGender,
        hideGender: AmplitudePropertyRegistrationGenderHide
    ) {
        logEvent(
            eventName = AmplitudeEventName.REGISTRATION_GENDER,
            properties = {
                it.apply {
                    addProperty(hideGender)
                    addProperty(gender)
                }
            }
        )
    }

    override fun logRegistrationLocationSelected(autocomplete: AmplitudePropertyRegistrationLocationAutocomplete) {
        logEvent(
            eventName = AmplitudeEventName.REGISTRATION_LOCATION,
            properties = {
                it.apply {
                    addProperty(autocomplete)
                }
            }
        )
    }

    override fun logRegistrationPhotoUniqueName(
        photoType: AmplitudePropertyRegistrationAvatarPhotoType,
        avatarTime: String,
        haveReferral: AmplitudePropertyRegistrationAvatarHaveReferral
    ) {
        logEvent(
            eventName = AmplitudeEventName.REGISTRATION_PHOTO,
            properties = {
                it.apply {
                    addProperty(photoType)
                    addProperty(AmplitudePropertyNameConst.REGISTRATION_PHOTO_AVATAR_TIME, avatarTime)
                    addProperty(haveReferral)
                }
            }
        )
    }

    override fun logUserProfileDelete(userId: Long, reasonId: Int) {
        val reason = AmplitudePropertyDeleteProfileReason.values().find { it.reasonId == reasonId }
            ?: AmplitudePropertyDeleteProfileReason.ANOTHER_REASON
        logEvent(
            eventName = AmplitudeEventName.PROFILE_DELETE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(reason)
                }
            }
        )
    }

    override fun logUserProfileRestore(userId: Long) {
        logEvent(
            eventName = AmplitudeEventName.PROFILE_RESTORE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun logHashTagPress(
        where: AmplitudePropertyWhere,
        postId: Long,
        authorId: Long
    ) {
        logEvent(
            eventName = AmplitudeEventName.HASH_TAG_PRESS,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.POST_ID, postId)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                }
            }
        )
    }

    override fun logUnderstandablyPress() {
        logEvent(eventName = AmplitudeEventName.UNDERSTANDABLY_PRESS)
    }

    override fun logGroupDescriptionChange() {
        logEvent(eventName = AmplitudeEventName.GROUP_DESCRIPTION_CHANGE)
    }

    override fun logGroupTitleChange() {
        logEvent(eventName = AmplitudeEventName.GROUP_TITLE_CHANGE)
    }

    private fun logEvent(eventName: AmplitudeEventName, properties: (JSONObject) -> JSONObject = { JSONObject() }) {
        val preparedProperties = properties(JSONObject())
        val isDebug = BuildConfig.DEBUG
        Timber.d("Handle event: isDebug = $isDebug  event = ${eventName.event}, property: $preparedProperties")
        if (!isDebug) {
            client.logEvent(eventName.event, preparedProperties)
            YandexMetrica.reportEvent(eventName.event, preparedProperties.toString())
        }
    }

    private fun JSONObject.addProperty(propertyName: String, value: Any) {
        put(propertyName, value)
    }

    private fun JSONObject.addProperty(property: AmplitudeProperty) {
        put(property._name, property._value)
    }

    private fun JSONObject.addOrSkipProperty(propertyName: String, value: Any?) {
        value?.let { data -> put(propertyName, data) }
    }

    override fun logPostDeleted(
        postItem: AnalyticsPost,
        whereFrom: AmplitudePropertyWhere,
    ) {
        logEvent(
            eventName = AmplitudeEventName.POST_DELETE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, postItem.postId)
                    addProperty(whereFrom)
                    addProperty(postItem.postType)
                    addProperty(postItem.contentProperty)
                    addProperty(AmplitudePropertyNameConst.HAVE_TEXT, postItem.haveText)
                    addProperty(AmplitudePropertyNameConst.HAVE_PIC, postItem.havePic)
                    addProperty(AmplitudePropertyNameConst.HAVE_VIDEO, postItem.haveVideo)
                    addProperty(AmplitudePropertyNameConst.HAVE_GIF, postItem.haveGif)
                    addProperty(postItem.commentsSettings)
                    addProperty(AmplitudePropertyNameConst.HAVE_MUSIC, postItem.haveMusic)
                    addProperty(AmplitudePropertyNameConst.VIDEO_DURATION, postItem.videoDuration)
                }
            }
        )
    }

    override fun logUserExit(userId: Long) {
        logEvent(
            eventName = AmplitudeEventName.USER_EXIT,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.ID, userId)
                }
            }
        )
    }

    override fun logBlockUser(userId: Long, blockedUserId: Long) {
        logEvent(
            eventName = AmplitudeEventName.BLOCK,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, userId)
                    addProperty(AmplitudePropertyNameConst.TO, blockedUserId)
                }
            }
        )
    }

    override fun logUnblockUser(userId: Long, unBlockedUserId: Long) {
        logEvent(
            eventName = AmplitudeEventName.UNBLOCK,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, userId)
                    addProperty(AmplitudePropertyNameConst.TO, unBlockedUserId)
                }
            }
        )
    }

    override fun logTetATetChatCreated(
        userId: Long,
        companionUserId: Long,
        status: AmplitudePropertyChatUserChatStatus,
        where: AmplitudePropertyChatCreatedFromWhere
    ) {
        logEvent(
            eventName = AmplitudeEventName.CHAT_CREATE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, userId)
                    addProperty(AmplitudePropertyNameConst.TO, companionUserId)
                    addProperty(status)
                    addProperty(where)
                }
            }
        )
    }

    override fun logTogglePress(
        userId: Long,
        companionUserId: Long,
        state: AmplitudePropertyChatCallSwitcherPosition
    ) {
        logEvent(
            eventName = AmplitudeEventName.TOGGLE_PRESS,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, userId)
                    addProperty(AmplitudePropertyNameConst.TO, companionUserId)
                    addProperty(state)
                }
            }
        )
    }

    override fun logPushAnswerTap(userId: Long, senderId: Long) {
        logEvent(
            eventName = AmplitudeEventName.PUSH_ANSWER_TAP,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, userId)
                    addProperty(AmplitudePropertyNameConst.TO, senderId)
                }
            }
        )
    }

    override fun logPostShareOpen(
        postId: Long,
        authorId: Long,
        momentId: Long,
        where: AmplitudePropertyWhere,
        recFeed: Boolean,
        publicType: AmplitudePropertyPublicType
    ) {
        logEvent(
            eventName = AmplitudeEventName.POST_SHARE_MENU_OPEN,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, postId)
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, momentId)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                    addProperty(publicType)
                }
            }
        )
    }

    override fun logPostShare(
        analyticsPostShare: AnalyticsPostShare,
        recFeed: Boolean
    ) {
        logEvent(
            eventName = AmplitudeEventName.POST_SHARE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, analyticsPostShare.postId)
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, analyticsPostShare.momentId)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, analyticsPostShare.authorId)
                    addProperty(analyticsPostShare.where)
                    addProperty(AmplitudePropertyNameConst.GROUP_COUNT, analyticsPostShare.groupCount)
                    addProperty(AmplitudePropertyNameConst.CHAT_COUNT, analyticsPostShare.chatCount)
                    addProperty(AmplitudePropertyNameConst.HAVE_ADD_TEXT, analyticsPostShare.textAdded)
                    addProperty(analyticsPostShare.whereSent)
                    addProperty(AmplitudePropertyNameConst.SEARCH, analyticsPostShare.search)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                    addProperty(analyticsPostShare.publicType)
                }
            }
        )
    }

    override fun logPostShareClose(bottomSheetCloseMethod: AmplitudePropertyBottomSheetCloseMethod) {
        logEvent(
            eventName = AmplitudeEventName.POST_SHARE_MENU_CLOSE,
            properties = {
                it.apply {
                    addProperty(bottomSheetCloseMethod)
                }
            }
        )
    }

    override fun logPostShareSettingsTap(where: AmplitudePropertyWhere) {
        logEvent(
            eventName = AmplitudeEventName.POST_SHARE_MENU_SETTINGS_TAP,
            properties = {
                it.apply {
                    addProperty(where)
                }
            }
        )
    }

    override fun logMapPrivacySettingsBlacklist(
        where: AmplitudePropertyWhereMapPrivacy,
        addCount: Int,
        deleteCount: Int
    ) {
        logEvent(
            eventName = AmplitudeEventName.PRIVACY_MAP_VISIBILITY_NEVER_TAP,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.ADD_COUNT, addCount)
                    addProperty(AmplitudePropertyNameConst.DELETE_COUNT, deleteCount)
                }
            }
        )
    }

    override fun logMapPrivacySettingsWhitelist(
        where: AmplitudePropertyWhereMapPrivacy,
        addCount: Int,
        deleteCount: Int
    ) {
        logEvent(
            eventName = AmplitudeEventName.PRIVACY_MAP_VISIBILITY_ALWAYS_TAP,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.ADD_COUNT, addCount)
                    addProperty(AmplitudePropertyNameConst.DELETE_COUNT, deleteCount)
                }
            }
        )
    }

    override fun logMapPrivacySettingsDeleteAll(count: Int, listType: AmplitudePropertyMapPrivacyListType) {
        logEvent(
            eventName = AmplitudeEventName.PRIVACY_MAP_DELETE_ALL_PRESS,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.COUNT, count)
                    addProperty(listType)
                }
            }
        )
    }

    override fun logCall(haveVideo: Boolean, duration: String, callType: AmplitudePropertyCallType) {
        logEvent(
            eventName = AmplitudeEventName.CALL,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.HAVE_VIDEO, haveVideo)
                    addProperty(AmplitudePropertyNameConst.DURATION, duration)
                    addProperty(callType)
                }
            }
        )
    }

    override fun logCallCancel(who: AmplitudePropertyCallCanceller) {
        logEvent(
            eventName = AmplitudeEventName.CALL_CANCEL,
            properties = {
                it.apply {
                    addProperty(who)
                }
            }
        )
    }

    override fun logChatRequest(
        fromUid: Long,
        toUid: Long,
        actionType: AmplitudePropertyActionTypeChatRequest
    ) {
        logEvent(
            eventName = AmplitudeEventName.CHAT_REQUEST_ACTION,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, fromUid)
                    addProperty(AmplitudePropertyNameConst.TO, toUid)
                    addProperty(AmplitudePropertyNameConst.ACTION_TYPE, actionType.property)
                }
            }
        )
    }

    //TODO: удалить после тестирования https://nomera.atlassian.net/browse/BR-21050
    override fun logFirebaseOnNewToken(token: String) {
        logEvent(
            eventName = AmplitudeEventName.FB_NEW_TOKEN,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FB_TOKEN, token)
                }
            }
        )
    }

    //TODO: удалить после тестирования https://nomera.atlassian.net/browse/BR-21050
    override fun logFirebaseGetMessage(msg: String) {
        logEvent(
            eventName = AmplitudeEventName.FB_NEW_MSG,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FB_MSG, msg)
                }
            }
        )
    }
}

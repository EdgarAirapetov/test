package com.numplates.nomera3.modules.baseCore.domain.repository

import com.amplitude.api.Identify
import com.meera.application_api.analytic.model.AmplitudeProperty
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
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereReaction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPost
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPostShare
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsUser
import com.numplates.nomera3.modules.baseCore.helper.amplitude.ComplainExtraActions
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.ComplainType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyWhosePost

const val EMPTY_VIDEO_AMPLITUDE_VALUE = 0

@Deprecated(
    "Если необходимо добавить аналитику для новой фичи," +
        " делать по примеру AmplitudeOnBoarding. Разделять на отдельные классы, файлы, пакеты," +
        " так как данный уже сильно большой"
)
interface AmplitudeRepository {
    /**
     * Два тестовых метода для отслеживания использования фичи свайп назад при уходе с экрана
     * Через 2 месяца можно будет удалить. Уточнить у Сергея Алонзова
     * */
    fun logBackPressed()

    fun logBackSwipe()

    fun reactionPanelOpen()

    fun reactionToComment(
        postId: Long,
        where: AmplitudePropertyWhereReaction,
        type: AmplitudePropertyReactionType,
        whence: AmplitudePropertyWhence,
        commentUserId: Long,
        publicationUserId: Long,
        momentId: Long
    )

    fun setUser(user: AnalyticsUser)

    fun identifyUserProperty(properties: (Identify) -> Identify)

    /**
     * 46) Выбраны фильтры стран / городов для главной дороги
     */
    fun logFilterMainRoad(
        country: String,
        city: String,
        recFeed: Boolean
    )

    /**
     * 51) Пользователь тапнул на кнопку поиска экрана дороги
     */
    @Deprecated(
        message = "Если нужно добавить новое событие, то используйте AmplitudeMainSearchAnalytics.logOpenMainSearch()"
    )
    fun logClickSearchButtonOnMainRoad()

    /**
     * 52) Пользователь ввёл значение в строку поиска
     */
    fun logSearchInput(
        type: AmplitudePropertySearchType,
        haveResult: AmplitudePropertyHaveResult,
        whereCommunitySearch: AmplitudePropertyWhereCommunitySearch,
        whereFriendsSearch: AmplitudePropertyWhereFriendsSearch = AmplitudePropertyWhereFriendsSearch.NONE
    )

    /**
     * Пользователь тапнул на кнопу @ в поиске
     */
    fun logTapSearchAtSign()

    /**
     * Пользователь тапнул на кнопку поиска по гос номеру
     */
    fun logTapSearchByNumberButton()

    /**
     * Пользователь тапнул на эмодзи в коментарии к посту
     */
    fun logEmojiTap(emojiType: String)

    /**
     * 63) Нажатие на кнопку создания сообщества
     */
    fun logCommunityCreateMenuOpen()

    /**
     * 64) Сообщество создано
     */
    fun logCommunityCreated(
        type: AmplitudePropertyCommunityType,
        whoCanWrite: AmplitudePropertyCanWrite,
        havePhoto: AmplitudePropertyHavePhoto
    )

    /**
     * 65) Сообщество удалено
     */
    fun logCommunityDeleted()

    /**
     * 66) Пользователь открыл экран сообщества
     */
    fun logCommunityScreenOpened(where: AmplitudePropertyWhereCommunityOpen)

    /**
     * 67) Пользователь подписался на сообщества
     * Откуда был переход
     */
    fun logCommunityFollow(userId: Long, where: AmplitudePropertyWhereCommunityFollow, communityId: Int)

    /**
     * 71) Пользователь открыл чат
     */
    fun logChatOpen(chatType: AmplitudePropertyChatType, where: AmplitudePropertyWhere)

    /**
     * 72) Рользователь создал групповой чат
     */
    fun logGroupChatCreate(
        havePhoto: AmplitudePropertyHavePhoto,
        haveDescription: AmplitudePropertyHaveDescription
    )

    /**
     * 73) Пользователь удалил групповой чат
     */
    fun logGroupChatDelete()

    /**
     * 74) Пользователь отправил сообщение
     */
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
        status: AmplitudePropertyChatUserChatStatus,
        mediaKeyboardCategory: AmplitudePropertyChatMediaKeyboardCategory,
        messageId: String
    )

    fun logAvatarDownloaded(from: AmplitudePropertyFrom)

    fun logCreatePostClick(
        where: AmplitudePropertyWhere,
        whichButton: AmplitudeCreatePostWhichButton
    )

    fun logPostCreated(
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
    )

    fun logPostOtherEvents(
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
    )

    fun logEditPost(
        postId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        textChange: Boolean,
        picChange: Boolean,
        videoChange: Boolean,
        musicChange: Boolean,
        backgroundChange: Boolean,
    )

    fun logOpenCustomFeed()

    fun logOpenMainFeed(recFeed: Boolean)

    fun logOpenFollowFeed(hasPosts: Boolean)

    fun logPressMoreText(
        postId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        postType: AmplitudePropertyPostType,
        openType: AmplitudePropertyOpenType
    )

    fun logShareProfile(userId: Long?, shareType: AmplitudePropertyProfileShare)

    /**
     *  102) Отмечаем, когда пользователь добавил транспортное средство в гараж
     * */
    fun logTransportAdd(vehicleName: AmplitudePropertyVehicleType)

    /**
     * 101) Событие нажатия на кнопку обновления приложения
     * */
    fun logUpdateBtnClicked()

    /**
     * 100) Событие показа плашки обновления
     * */
    fun logUpdateBtnShown()


    /**
     * 99) Событие покупки подарка
     * */
    fun logSendGift(
        productId: String,
        fromId: String,
        toId: String,
        sendBack: AmplitudePropertyGiftSendBack,
        where: AmplitudePropertyWhere
    )

    /**
     * 98) Событие покупки випа
     * */
    fun logBuyVipStatus(
        color: AmplitudePropertyColor,
        duration: AmplitudePropertyDuration,
        expirationDate: String,
        haveVipBefore: AmplitudePropertyHaveVIPBefore,
        way: AmplitudePropertyWay
    )


    /**
     * 97) Событие открытие меню замены аватара профиля
     * */
    fun logAvatarPickerOpen()

    /**
     * 94) Разрешение на звонки
     * */
    fun logCallsPermission(whoCanCall: AmplitudePropertyCallsSettings)

    /**
     * 93) Логин успешно завершен без регистрации
     * */
    fun logLoginFinished()

    fun setIsFromSms(isFromSms: Boolean)

    /**
     * 92) Завершение регистрации
     * */
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
    )

    /**
     * 90) Ввод проверочного кода
     * */
    fun logCodeEnter(
        inputTime: String,
        incorrectCount: Int,
        requestCount: Int,
        country: String,
        number: String,
        email: String
    )

    /**
     * 89) Отмечаем когда пользователь ввел номер телефона или почту и нажал "далее"
     * */
    fun logRegistration(inputType: AmplitudePropertyInputType, country: String, number: String, email: String)

    /**
     * 88) Просмотр окна приветствия
     * */
    fun logOnboarding(onboarding: AmplitudePropertyOnboarding)

    /**
     * 86) Открытие приложения в первый раз
     * */
    fun logFirstTimeOpen()

    /**
     * 79) Пользователь удалил уведомление
     * */
    fun logNotificationDelete()

    /**
     * Пользователь включил уведомление
     * */
    fun logNotificationEnabled(isEnabled: Boolean)

    /**
     * 78) Пользователь удалил все уведомления
     */
    fun logAllNotificationsDeleted()

    /**
     * 50) Пользователь совершил действие в меню поста
     * */
    fun logPostMenuAction(
        actionType: AmplitudePropertyMenuAction,
        authorId: Long,
        where: AmplitudePropertyWhere,
        whosePost: AmplitudePropertyWhosePost,
        whence: AmplitudePropertyWhence,
        saveType: AmplitudePropertySaveType,
        recFeed: Boolean
    )

    /**
     * 56) Поиск по гос номеру
     */
    fun logNumberSearch(
        transportType: AmplitudePropertyTransportType,
        country: String,
        fullness: AmplitudePropertyFullness,
        charCount: String,
        haveResult: AmplitudePropertyHaveResult
    )

    fun logFeedScroll(
        roadType: AmplitudePropertyRoadType,
        recFeed: Boolean
    )

    fun logForceUpdate(
        actionType: AmplitudePropertyActionType
    )

    fun logMusicAddPress()

    fun logPlayStopMusic(
        actionType: AmplitudePropertyActionPlayStopMusic,
        where: AmplitudePropertyWhereMusicPlay
    )

    fun logBottomBarCommunityClicked()

    fun logBottomBarChatClicked()

    fun logBottomBarProfileClicked()

    fun logBottomBarRoad(
        roadType: AmplitudePropertyRoadType,
        recFeed: Boolean,
        roadHowWasOpened: AmplitudePropertyHowWasOpened
    )

    fun logSendGiftBack()

    fun logNewYearCandyCount(count: AmplitudePropertyCandyCount)

    fun logChatGifButtonPress()

    fun logComplainExtraAction(actions: ComplainExtraActions)

    fun logComplain(type: ComplainType, from: Long, to: Long)

    fun logPrivacySettings(property: AmplitudeProperty)

    @Deprecated(
        message = "Используйте AmplitudeAddFriendAnalytic.logAddFriend()"
    )
    fun logAddFriend(
        from: Long,
        to: Long,
        type: FriendAddAction = FriendAddAction.OTHER
    )

    fun logDelFriend(from: Long, to: Long)

    @Deprecated(
        message = "Используйте FollowButtonAnalytic.logUnfollowAction()"
    )
    fun logUnsubscribeUser(from: Long, to: Long)

    fun logUserStatus(empty: Boolean)

    fun logAnimatedAvatarOpen(property: AmplitudePropertyAnimatedAvatarFrom)

    fun logAnimatedAvatarCreated(property: AmplitudePropertyAnimatedAvatarFrom)

    fun logAvatarDownloaded(from: AmplitudePropertyAvatarDownloadFrom, type: AmplitudePropertyAvatarPhotoType)

    fun logPhotoSelection(type: AmplitudePropertyAvatarType, avatarCreationTime: String?)

    fun logCommunityShare(
        where: AmplitudePropertyCommunityWhere,
        groupId: Long,
        communityType: AmplitudePropertyCommunityType,
        canWrite: AmplitudePropertyCanWrite,
        havePhoto: AmplitudePropertyHavePhoto
    )


    fun logRegistrationHelpPressed(property: AmplitudePropertyHelpPressedWhere)

    fun logRegistrationClose(step: AmplitudePropertyRegistrationStep)

    fun logRegistrationNameEntered()

    fun logRegistrationBirthdayEntered(age: Int, hideAge: AmplitudePropertyRegistrationBirthdayHide)

    fun logRegistrationGenderSelected(
        gender: AmplitudePropertyRegistrationGender,
        hideGender: AmplitudePropertyRegistrationGenderHide
    )

    fun logRegistrationLocationSelected(autocomplete: AmplitudePropertyRegistrationLocationAutocomplete)

    fun logRegistrationPhotoUniqueName(
        photoType: AmplitudePropertyRegistrationAvatarPhotoType,
        avatarTime: String,
        haveReferral: AmplitudePropertyRegistrationAvatarHaveReferral
    )

    fun logVoiceMessageRecognitionTap(
        messageId: String,
        type: AmplitudePropertyRecognizedTextButton,
        duration: Long
    )

    fun logUserProfileDelete(userId: Long, reasonId: Int)

    fun logUserProfileRestore(userId: Long)

    fun logHashTagPress(
        where: AmplitudePropertyWhere,
        postId: Long,
        authorId: Long,
    )

    fun logUnderstandablyPress()

    fun logGroupDescriptionChange()

    fun logGroupTitleChange()

    fun logPostDeleted(
        postItem: AnalyticsPost,
        whereFrom: AmplitudePropertyWhere
    )

    fun logUserExit(userId: Long)

    fun logBlockUser(userId: Long, blockedUserId: Long)

    fun logUnblockUser(userId: Long, unBlockedUserId: Long)

    fun logTetATetChatCreated(
        userId: Long,
        companionUserId: Long,
        status: AmplitudePropertyChatUserChatStatus,
        where: AmplitudePropertyChatCreatedFromWhere
    )

    fun logTogglePress(userId: Long, companionUserId: Long, state: AmplitudePropertyChatCallSwitcherPosition)

    fun logPushAnswerTap(userId: Long, senderId: Long)

    fun logForwardMessageClicked()

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
    )

    fun logUnlockChat(from: Long, to: Long)

    fun logPostShareOpen(
        postId: Long,
        authorId: Long,
        momentId: Long,
        where: AmplitudePropertyWhere,
        recFeed: Boolean,
        publicType: AmplitudePropertyPublicType
    )

    fun logPostShare(
        analyticsPostShare: AnalyticsPostShare,
        recFeed: Boolean
    )

    fun logPostShareClose(bottomSheetCloseMethod: AmplitudePropertyBottomSheetCloseMethod)

    fun logPostShareSettingsTap(where: AmplitudePropertyWhere)

    fun logMapPrivacySettingsClicked(where: AmplitudePropertyWhereMapPrivacy)

    fun logMapPrivacySettingsSetup(
        where: AmplitudePropertyWhereMapPrivacy,
        visibility: AmplitudePropertySettingVisibility
    )

    fun logMapPrivacySettingsBlacklist(where: AmplitudePropertyWhereMapPrivacy, addCount: Int, deleteCount: Int)

    fun logMapPrivacySettingsWhitelist(where: AmplitudePropertyWhereMapPrivacy, addCount: Int, deleteCount: Int)

    fun logMapPrivacySettingsDeleteAll(count: Int, listType: AmplitudePropertyMapPrivacyListType)

    fun logCall(haveVideo: Boolean, duration: String, callType: AmplitudePropertyCallType)

    fun logCallCancel(who: AmplitudePropertyCallCanceller)

    fun logChatRequest(
        fromUid: Long,
        toUid: Long,
        actionType: AmplitudePropertyActionTypeChatRequest
    )

    // TODO: удалить после тестирования https://nomera.atlassian.net/browse/BR-21050
    fun logFirebaseOnNewToken(token: String)

    fun logFirebaseGetMessage(msg: String)

    fun logCommunityUnfollow(userId: Long, communityId: Int)
}

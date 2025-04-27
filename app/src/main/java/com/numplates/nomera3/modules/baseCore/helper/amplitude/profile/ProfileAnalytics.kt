package com.numplates.nomera3.modules.baseCore.helper.amplitude.profile

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySettingVisibility
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.COUNT_MUTUAL_AUDIENCE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.HAVE_VISIBILITY_MUTUAL_AUDIENCE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.onboarding.AmplitudeOnBoardingEventName
import com.numplates.nomera3.modules.baseCore.helper.amplitude.onboarding.AmplitudePropertyOnBoardingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.onboarding.AmplitudePropertyOnBoardingConst
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum
import javax.inject.Inject

interface AmplitudeProfile {
    fun photoActionSave(
        userId: Long,
        authorId: Long,
        where: AmplitudePhotoActionValuesWhere
    )

    fun photoActionPhotoChange(
        userId: Long,
        authorId: Long,
        where: AmplitudePhotoActionValuesWhere
    )

    fun photoActionAvatarCreate(
        userId: Long,
        authorId: Long,
        where: AmplitudePhotoActionValuesWhere
    )

    fun photoActionMakeTheMain(
        userId: Long,
        authorId: Long,
        where: AmplitudePhotoActionValuesWhere
    )

    fun photoActionDelete(
        userId: Long,
        authorId: Long,
        where: AmplitudePhotoActionValuesWhere
    )

    fun mainPhotoChangesDownloadNewPhoto(userId: Long)
    fun mainPhotoChangesAvatarCreate(userId: Long)
    fun mainPhotoChangesChooseFromAbout(userId: Long)
    fun mainPhotoChangesChooseFromAvatars(userId: Long)

    fun alertPostWithNewAvatarAction(
        actionType: AmplitudeAlertPostWithNewAvatarValuesActionType,
        feedType: AmplitudeAlertPostWithNewAvatarValuesFeedType,
        toggle: Boolean,
        userId: Long
    )

    fun privacyPostWithNewAvatarChangeSettings(publishSettings: AmplitudePrivacyPostWithNewAvatarChangeValuesPublishSettings)
    fun privacyPostWithNewAvatarChangeAlert(createAvatarPost: Int)

    fun logProfileEntrance(
        where: AmplitudePropertyWhere,
        fromId: Long,
        toId: Long,
        relationship: FriendRelationshipProperty = FriendRelationshipProperty.NONE,
        amplitudeInfluencerProperty: AmplitudeInfluencerProperty = AmplitudeInfluencerProperty.NONE,
        countMutualAudience: Int = 0,
        haveVisibilityMutualAudience: Boolean = true
    )

    fun logUserCardHide(
        where: AmplitudeUserCardHideWhereProperty,
        fromId: Long,
        toId: Long,
        section: AmplitudeUserCardHideSectionProperty
    )

    fun logSelfFeedVisibilityChange(
        where: AmplitudeSelfFeedVisibilityChangeWhereProperty,
        visibility: AmplitudePropertySettingVisibility,
        userId: Long
    )

    fun logProfileEditTap(
        userId: Long,
        where: AmplitudeProfileEditTapProperty
    )
}


class AmplitudeProfileImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeProfile {

    fun onEnterClicked(eventName: AmplitudeOnBoardingEventName, afterContinue: Boolean) {
        delegate.logEvent(
            eventName = eventName,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyOnBoardingActionType.ENTER)
                    addProperty(AmplitudePropertyOnBoardingConst.CONTINUE_CLICKED, afterContinue)
                }
            }
        )
    }

    override fun photoActionSave(userId: Long, authorId: Long, where: AmplitudePhotoActionValuesWhere) =
        logPhotoActionEvents(AmplitudePhotoActionValuesActionType.SAVE, userId, authorId, where)


    override fun photoActionPhotoChange(userId: Long, authorId: Long, where: AmplitudePhotoActionValuesWhere) =
        logPhotoActionEvents(AmplitudePhotoActionValuesActionType.PHOTO_CHANGE, userId, authorId, where)


    override fun photoActionAvatarCreate(userId: Long, authorId: Long, where: AmplitudePhotoActionValuesWhere) =
        logPhotoActionEvents(AmplitudePhotoActionValuesActionType.AVATAR_CREATE, userId, authorId, where)


    override fun photoActionMakeTheMain(userId: Long, authorId: Long, where: AmplitudePhotoActionValuesWhere) =
        logPhotoActionEvents(AmplitudePhotoActionValuesActionType.MAKE_THE_MAIN, userId, authorId, where)


    override fun photoActionDelete(userId: Long, authorId: Long, where: AmplitudePhotoActionValuesWhere) =
        logPhotoActionEvents(AmplitudePhotoActionValuesActionType.DELETE, userId, authorId, where)


    private fun logPhotoActionEvents(
        actionType: AmplitudePhotoActionValuesActionType,
        userId: Long,
        authorId: Long,
        where: AmplitudePhotoActionValuesWhere
    ) {
        delegate.logEvent(
            eventName = ProfileEvents.PHOTO_ACTION,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyPhotoAction.ACTION_TYPE, actionType)
                    addProperty(
                        AmplitudePropertyPhotoAction.WHOSE,
                        if (userId == authorId) AmplitudePhotoActionValuesWhose.MY else AmplitudePhotoActionValuesWhose.USER
                    )
                    addProperty(AmplitudePropertyPhotoAction.USER_ID.property, userId)
                    addProperty(AmplitudePropertyPhotoAction.AUTHOR_ID.property, authorId)
                    addProperty(AmplitudePropertyPhotoAction.WHERE, where)
                }
            }
        )
    }

    override fun mainPhotoChangesDownloadNewPhoto(userId: Long) =
        logMainPhotoChangeEvents(AmplitudeMainPhotoChangesValuesHow.DOWNLOAD_NEW_PHOTO, userId)


    override fun mainPhotoChangesAvatarCreate(userId: Long) =
        logMainPhotoChangeEvents(AmplitudeMainPhotoChangesValuesHow.AVATAR_CREATE, userId)


    override fun mainPhotoChangesChooseFromAbout(userId: Long) =
        logMainPhotoChangeEvents(AmplitudeMainPhotoChangesValuesHow.CHOOSE_FROM_ABOUT_ME, userId)


    override fun mainPhotoChangesChooseFromAvatars(userId: Long) =
        logMainPhotoChangeEvents(AmplitudeMainPhotoChangesValuesHow.CHOOSE_FROM_AVATARS, userId)


    private fun logMainPhotoChangeEvents(how: AmplitudeMainPhotoChangesValuesHow, userId: Long) {
        delegate.logEvent(
            eventName = ProfileEvents.MAIN_PHOTO_CHANGES,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyMainPhotoChanges.HOW, how)
                    addProperty(AmplitudePropertyMainPhotoChanges.USER_ID.property, userId)
                }
            }
        )
    }

    override fun alertPostWithNewAvatarAction(
        actionType: AmplitudeAlertPostWithNewAvatarValuesActionType,
        feedType: AmplitudeAlertPostWithNewAvatarValuesFeedType,
        toggle: Boolean,
        userId: Long
    ) {
        delegate.logEvent(
            eventName = ProfileEvents.ALERT_POST_WITH_NEW_AVATAR_ACTION,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyAlertPostWithNewAvatar.ACTION_TYPE, actionType)
                    addProperty(AmplitudePropertyAlertPostWithNewAvatar.FEED_TYPE, feedType)
                    addProperty(
                        AmplitudePropertyAlertPostWithNewAvatar.FEED_TYPE,
                        if (toggle) AmplitudeAlertPostWithNewAvatarValuesTogglePosition.ON
                        else AmplitudeAlertPostWithNewAvatarValuesTogglePosition.OFF
                    )
                    addProperty(AmplitudePropertyAlertPostWithNewAvatar.USER_ID.property, userId)
                }
            }
        )
    }

    override fun privacyPostWithNewAvatarChangeSettings(
        publishSettings: AmplitudePrivacyPostWithNewAvatarChangeValuesPublishSettings
    ) = logPrivacyPostWithNewAvatarChangeEvent(
            AmplitudePrivacyPostWithNewAvatarChangeValuesWhere.SETTINGS,
            publishSettings
        )


    override fun privacyPostWithNewAvatarChangeAlert(createAvatarPost: Int) {

        val amplitudePrivacyFeedType = when (createAvatarPost) {
            CreateAvatarPostEnum.PRIVATE_ROAD.state -> AmplitudePrivacyPostWithNewAvatarChangeValuesPublishSettings.SELF_FEED
            CreateAvatarPostEnum.MAIN_ROAD.state -> AmplitudePrivacyPostWithNewAvatarChangeValuesPublishSettings.MAIN_FEED
            else -> AmplitudePrivacyPostWithNewAvatarChangeValuesPublishSettings.NO_PUBLISH
        }

        logPrivacyPostWithNewAvatarChangeEvent(
            AmplitudePrivacyPostWithNewAvatarChangeValuesWhere.ALERT,
            amplitudePrivacyFeedType
        )
    }


    private fun logPrivacyPostWithNewAvatarChangeEvent(
        where: AmplitudePrivacyPostWithNewAvatarChangeValuesWhere,
        publishSettings: AmplitudePrivacyPostWithNewAvatarChangeValuesPublishSettings
    ) {
        delegate.logEvent(
            eventName = ProfileEvents.PRIVACY_POST_WITH_NEW_AVATAR_CHANGE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyPrivacyPostWithNewAvatarChange.WHERE, where)
                    addProperty(AmplitudePropertyPrivacyPostWithNewAvatarChange.PUBLISH_SETTINGS, publishSettings)

                }
            }
        )
    }

    override fun logProfileEntrance(
        where: AmplitudePropertyWhere,
        fromId: Long,
        toId: Long,
        relationship: FriendRelationshipProperty,
        amplitudeInfluencerProperty: AmplitudeInfluencerProperty,
        countMutualAudience: Int,
        haveVisibilityMutualAudience: Boolean
    ) {
        delegate.logEvent(
            eventName = AmplitudeProfileEntranceEventName.PROFILE_ENTRANCE,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.FROM, fromId)
                    addProperty(AmplitudePropertyNameConst.TO, toId)
                    addProperty(relationship)
                    addProperty(amplitudeInfluencerProperty)
                    addProperty(COUNT_MUTUAL_AUDIENCE, countMutualAudience)
                    addProperty(HAVE_VISIBILITY_MUTUAL_AUDIENCE, haveVisibilityMutualAudience)
                }
            }
        )
    }

    override fun logUserCardHide(
        where: AmplitudeUserCardHideWhereProperty,
        fromId: Long,
        toId: Long,
        section: AmplitudeUserCardHideSectionProperty
    ) {
        delegate.logEvent(
            eventName = ProfileEvents.USER_CARD_HIDE,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.FROM, fromId)
                    addProperty(AmplitudePropertyNameConst.TO, toId)
                    addProperty(section)
                }
            }
        )
    }

    override fun logSelfFeedVisibilityChange(
        where: AmplitudeSelfFeedVisibilityChangeWhereProperty,
        visibility: AmplitudePropertySettingVisibility,
        userId: Long
    ) {
        delegate.logEvent(
            eventName = ProfileEvents.SELF_VISIBILITY_FEED_CHANGE,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(visibility)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun logProfileEditTap(
        userId: Long,
        where: AmplitudeProfileEditTapProperty
    ) {
        delegate.logEvent(
            eventName = ProfileEvents.PROFILE_EDIT_TAP,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(where)
                }
            }
        )
    }
}

package com.numplates.nomera3.modules.peoples.ui.delegate

import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeShakeAnalyticRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend.AmplitudeAddFriendAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudePropertyType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleContentCardProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleSectionChangeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeInfluencerProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeUserCardHideSectionProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeUserCardHideWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.search.AmplitudeMainSearchAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.search.AmplitudeSearchOpenWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeHowProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.AmplitudeSyncContactsActionTypeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.AmplitudeSyncContactsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsSuccessActionTypeProperty
import javax.inject.Inject

class PeopleAnalyticDelegate @Inject constructor(
    private val getUserUidUseCase: GetUserUidUseCase,
    private val amplitudePeopleAnalytics: AmplitudePeopleAnalytics,
    private val amplitudeMainSearchAnalytics: AmplitudeMainSearchAnalytics,
    private val amplitudeAddFriendAnalytics: AmplitudeAddFriendAnalytic,
    private val friendInviteTapAnalytics: FriendInviteTapAnalytics,
    private val amplitudeFollowButton: AmplitudeFollowButton,
    private val profileAnalytic: AmplitudeProfile,
    private val amplitudeShakeAnalytic: AmplitudeShakeAnalyticRepository,
    private val syncContactsAnalytic: SyncContactsAnalytic
) {

    fun logAddToFriendAmplitude(
        userId: Long,
        influencer: AmplitudeInfluencerProperty,
        type: FriendAddAction
    ) {
        amplitudeAddFriendAnalytics.logAddFriend(
            fromId = getUserUidUseCase.invoke(),
            toId = userId,
            type = type,
            influencer = influencer
        )
    }

    fun logContentCardAmplitude(
        cardWhereAction: AmplitudePeopleContentCardProperty,
        haveVideo: Boolean,
        havePhoto: Boolean,
        postId: Long,
        userId: Long,
        authorId: Long
    ) {
        amplitudePeopleAnalytics.setContentCard(
            cardWhereAction = cardWhereAction,
            haveVideo = haveVideo,
            havePhoto = havePhoto,
            postId = postId,
            userId = userId,
            authorId = authorId
        )
    }

    fun logFollowActionAmplitude(
        userId: Long,
        amplitudeInfluencerProperty: AmplitudeInfluencerProperty,
        where: AmplitudeFollowButtonPropertyWhere
    ) {
        amplitudeFollowButton.followAction(
            fromId = getUserUidUseCase.invoke(),
            toId = userId,
            where = where,
            type = AmplitudePropertyType.OTHER,
            amplitudeInfluencerProperty = amplitudeInfluencerProperty
        )
    }

    fun logUnfollowAmplitude(
        userId: Long,
        amplitudeInfluencerProperty: AmplitudeInfluencerProperty,
        where: AmplitudeFollowButtonPropertyWhere
    ) {
        amplitudeFollowButton.logUnfollowAction(
            fromId = getUserUidUseCase.invoke(),
            toId = userId,
            where = where,
            type = AmplitudePropertyType.OTHER,
            amplitudeInfluencerProperty = amplitudeInfluencerProperty
        )
    }

    fun logInvitePeople() {
        friendInviteTapAnalytics.logFiendInviteTap(FriendInviteTapProperty.PEOPLE)
    }

    fun logPeopleOpened(where: AmplitudePeopleWhereProperty) {
        amplitudePeopleAnalytics.setPeopleSelected(
            where = where,
            which = AmplitudePeopleWhich.PEOPLE
        )
    }

    fun logSearchOpen() {
        amplitudeMainSearchAnalytics.logOpenMainSearch(AmplitudeSearchOpenWhereProperty.PEOPLE)
    }

    fun logCommunitySection() {
        amplitudePeopleAnalytics.setSectionChanged(AmplitudePeopleSectionChangeProperty.COMMUNITY)
    }

    fun logStartSyncContacts() {
        syncContactsAnalytic.logSyncContactsStart(
            where = AmplitudeSyncContactsWhereProperty.PEOPLE,
            userId = getUserUidUseCase.invoke()
        )
    }

    fun logSyncContactsFinished(type: SyncContactsSuccessActionTypeProperty, syncCount: Int) {
        syncContactsAnalytic.logContactsSyncFinished(
            actionTypeProperty = type,
            userId = getUserUidUseCase.invoke(),
            syncCount = syncCount
        )
    }

    fun logUserCardHide(toId: Long, section: AmplitudeUserCardHideSectionProperty) {
        profileAnalytic.logUserCardHide(
            where = AmplitudeUserCardHideWhereProperty.POSSIBLE_FRIENDS,
            fromId = getUserUidUseCase.invoke(),
            toId = toId,
            section = section
        )
    }

    fun logSyncContactsAction(
        where: AmplitudeSyncContactsWhereProperty,
        numberOfPopup: Int,
        typeProperty: AmplitudeSyncContactsActionTypeProperty,

    ) {
        syncContactsAnalytic.logSyncContactsAction(
            actionType = typeProperty,
            numberOfPopup = numberOfPopup,
            where = where,
            userId = getUserUidUseCase.invoke()
        )
    }

    fun logShakeOpenedByButton(whereProperty: AmplitudeShakeWhereProperty) {
        amplitudeShakeAnalytic.logShakeTap(
            howCalled = AmplitudeShakeHowProperty.BUTTON,
            userId = getUserUidUseCase.invoke(),
            where = whereProperty
        )
    }
}

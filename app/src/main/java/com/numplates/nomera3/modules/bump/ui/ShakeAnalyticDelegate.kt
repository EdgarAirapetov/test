package com.numplates.nomera3.modules.bump.ui

import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeShakeAnalyticRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend.AmplitudeAddFriendAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeFriendRequest
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeFriendRequestPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeInfluencerProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeHowProperty
import javax.inject.Inject

class ShakeAnalyticDelegate @Inject constructor(
    private val amplitudeAddFriendAnalytic: AmplitudeAddFriendAnalytic,
    private val amplitudeFriendRequest: AmplitudeFriendRequest,
    private val amplitudeShakeAnalytic: AmplitudeShakeAnalyticRepository
) {

    fun logFriendRequestDenied(
        fromId: Long,
        toId: Long,
    ) {
        amplitudeFriendRequest.onRequestDenied(
            fromId = fromId,
            toId = toId,
            where = AmplitudeFriendRequestPropertyWhere.SHAKE
        )
    }

    fun logConfirmFriendRequest(
        fromId: Long,
        toId: Long
    ) {
        amplitudeFriendRequest.onRequestAccepted(
            fromId = fromId,
            toId = toId,
            where = AmplitudeFriendRequestPropertyWhere.SHAKE
        )
    }

    fun logAddToFriends(
        fromId: Long,
        toId: Long,
        influencer: AmplitudeInfluencerProperty
    ) {
        amplitudeAddFriendAnalytic.logAddFriend(
            fromId = fromId,
            toId = toId,
            type = FriendAddAction.SHAKE,
            influencer = influencer
        )
    }

    fun logShakeResults(
        shakeCalled: ShakeOpenedTypeEnum,
        countMutualAudience: Int,
        countUserShake: Int,
        fromId: Long,
        toId: Long
    ) {
        val howCalled =
            if (shakeCalled == ShakeOpenedTypeEnum.SHAKE_OPENED_BY_SENSOR) AmplitudeShakeHowProperty.SHAKE else
                AmplitudeShakeHowProperty.BUTTON
        amplitudeShakeAnalytic.logShakeResults(
            howCalled = howCalled,
            countMutualAudience = countMutualAudience,
            countUserShake = countUserShake,
            fromId = fromId,
            toId = toId
        )
    }
}

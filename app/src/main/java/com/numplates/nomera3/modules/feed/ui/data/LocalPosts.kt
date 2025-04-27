package com.numplates.nomera3.modules.feed.ui.data

import com.meera.referrals.ui.model.ReferralDataUIModel
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.FeatureData
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels

const val RATE_US_POST_ID = -2L
const val ADD_NEW_POST_ID = -3L
const val MOMENTS_POST_ID = -4L
private const val SYNC_CONTACTS_ID = -7L
private const val REFERRAL_ID = -5L
private const val SUGGESTS_ID = -6L
private const val POSTS_VIEWED_ID = -8L

private const val RATE_US_POSITION = 200
private const val ADD_NEW_POST_POSITION = 0
const val MOMENTS_POSITION_WITH_ADD_NEW_BLOCK = 1
const val MOMENTS_POSITION_WITHOUT_ADD_NEW_BLOCK = 0
const val MEERA_MOMENTS_POSITION = 0
private const val SYNC_CONTACTS_POSITION = 10
private const val REFERRAL_POSITION = 65
private const val SUGGESTS_POSITION = 35

object LocalPosts {

    fun getAddNew(): PostUIEntity {
        return PostUIEntity(
            feedType = FeedType.CREATE_POST,
            featureData = FeatureData(
                id = ADD_NEW_POST_ID,
                positions = listOf(ADD_NEW_POST_POSITION)
            )
        )
    }

    fun getRateUsPost(): PostUIEntity {
        return PostUIEntity(
            postId = RATE_US_POST_ID,
            feedType = FeedType.RATE_US,
            featureData = FeatureData(
                id = RATE_US_POST_ID,
                positions = listOf(RATE_US_POSITION)
            )
        )
    }

    fun getMoments(
        model: MomentInfoCarouselUiModel?,
        blockAvatar: String?,
        roadType: RoadTypesEnum?
    ): PostUIEntity {
        val positionIndex = if (roadType == RoadTypesEnum.MAIN) {
            MOMENTS_POSITION_WITH_ADD_NEW_BLOCK
        } else {
            MOMENTS_POSITION_WITHOUT_ADD_NEW_BLOCK
        }

        return PostUIEntity(
            postId = MOMENTS_POST_ID,
            feedType = FeedType.MOMENTS,
            moments = model,
            momentsBlockAvatar = blockAvatar,
            featureData = FeatureData(
                id = MOMENTS_POST_ID,
                positions = listOf(positionIndex)
            )
        )
    }

    fun getMeeraMoments(
        model: MomentInfoCarouselUiModel?,
        blockAvatar: String?
    ): PostUIEntity {
        return PostUIEntity(
            postId = MOMENTS_POST_ID,
            feedType = FeedType.MOMENTS,
            moments = model,
            momentsBlockAvatar = blockAvatar,
            featureData = FeatureData(
                id = MOMENTS_POST_ID,
                positions = listOf(MEERA_MOMENTS_POSITION)
            )
        )
    }

    fun getSyncContactsPost(): PostUIEntity {
        return PostUIEntity(
            postId = SYNC_CONTACTS_ID,
            feedType = FeedType.SYNC_CONTACTS,
            featureData = FeatureData(
                id = SYNC_CONTACTS_ID,
                positions = listOf(SYNC_CONTACTS_POSITION)
            )
        )
    }

    fun getReferralPost(referralDataUIModel: ReferralDataUIModel): PostUIEntity {
        return PostUIEntity(
            postId = REFERRAL_ID,
            feedType = FeedType.REFERRAL,
            featureData = FeatureData(
                id = REFERRAL_ID,
                positions = listOf(REFERRAL_POSITION),
                referralInfo = referralDataUIModel
            )
        )
    }

    fun getSuggestsPost(suggests: List<ProfileSuggestionUiModels>): PostUIEntity {
        return PostUIEntity(
            postId = SUGGESTS_ID,
            feedType = FeedType.SUGGESTIONS,
            featureData = FeatureData(
                id = SUGGESTS_ID,
                positions = listOf(SUGGESTS_POSITION),
                suggestions = suggests
            )
        )
    }

    fun getPostsViewedPost(roadType: DestinationOriginEnum?, isVip: Boolean): PostUIEntity? {
        val feedType = when {
            roadType == DestinationOriginEnum.OTHER_PROFILE && !isVip -> FeedType.POSTS_VIEWED_PROFILE
            roadType == DestinationOriginEnum.OTHER_PROFILE && isVip -> FeedType.POSTS_VIEWED_PROFILE_VIP
            roadType == DestinationOriginEnum.SUBSCRIPTIONS_ROAD -> FeedType.POSTS_VIEWED_ROAD
            else -> return null
        }
        return PostUIEntity(
            postId = POSTS_VIEWED_ID,
            feedType = feedType
        )
    }
}

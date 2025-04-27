package com.numplates.nomera3.modules.feed.ui.entity

import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.volume.domain.model.VolumeState

// item used to update post via payload
// TODO переделать в sealed
open class UIPostUpdate(
    open val postId: Long? = null,
    val repostCount: Int? = null,
    val commentCount: Int? = null,
    val reactions: List<ReactionEntity>? = null
) {

    data class UpdateReaction(
        override val postId: Long,
        val reactionUpdate: ReactionUpdate
    ) : UIPostUpdate(postId)

    data class MeeraUpdateReaction(
        override val postId: Long,
        val reactionUpdate: MeeraReactionUpdate
    ) : UIPostUpdate(postId)

    data class UpdateTagSpan(
        override val postId: Long,
        val post: PostUIEntity
    ) : UIPostUpdate(postId)

    data class UpdateLoadingState(
        override val postId: Long,
        val loadingInfo: LoadingPostVideoInfoUIModel
    ) : UIPostUpdate(postId)

    data class UpdateUpdatingState(
        override val postId: Long,
        val loadingInfo: LoadingPostVideoInfoUIModel
    ) : UIPostUpdate(postId)

    data class UpdateVolumeState(
        override val postId: Long,
        val volumeState: VolumeState
    ) : UIPostUpdate(postId)

    data class UpdateEventPostParticipationState(
        override val postId: Long,
        val postUIEntity: PostUIEntity
    ) : UIPostUpdate(postId)

    data class UpdateMoments(
        override val postId: Long,
        val roadType: RoadTypesEnum,
        val asPayload: Boolean,
        val scrollToGroupId: Long?,
        val scrollToStart: Boolean,
        val moments: MomentInfoCarouselUiModel?,
        val momentsBlockAvatar: String?
    ) : UIPostUpdate(postId)

    data class UpdateUserMomentsState(
        val userId: Long,
        val hasMoments: Boolean,
        val hasNewMoments: Boolean,
        val postMomentsBlock: PostUIEntity? = null
    ) : UIPostUpdate(null)

    data class UpdateSelectedMediaPosition(
        override val postId: Long,
        val selectedMediaPosition: Int
    ) : UIPostUpdate(postId)

    data class UpdateTimeAgo(
        override val postId: Long
    ) : UIPostUpdate(postId)
}

package com.numplates.nomera3.modules.moments.show.data

import androidx.work.ListenableWorker
import com.numplates.nomera3.modules.moments.comments.data.MomentsCommentsRepository
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentPagingParams
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.domain.MomentItemModel
import com.numplates.nomera3.modules.moments.show.domain.MomentLinkModel
import com.numplates.nomera3.modules.moments.show.domain.MomentsAction
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import kotlinx.coroutines.flow.Flow

interface MomentsRepository : MomentSettingsRepository, MomentsCommentsRepository {

    fun getLastLoadedMomentUserId(momentsSource: GetMomentDataUseCase.MomentsSource): Int

    fun getEventStream(): Flow<MomentRepositoryEvent>

    fun updateMomentReactions(
        momentId: Long,
        reactionList: List<ReactionEntity>
    )

    fun updateUserSubscriptions(userIds: List<Long>, isAdded: Boolean)

    fun updateUserBlockStatus(remoteUserId: Long, isBlockedByMe: Boolean)

    fun getMomentsFromCache(momentsSource: GetMomentDataUseCase.MomentsSource): MomentInfoModel

    fun updateCommentCounter(momentId: Long)

    fun newMomentCreated()

    suspend fun getMomentsFromRest(
        userId: Long,
        targetMomentId: Long?,
        momentsSource: GetMomentDataUseCase.MomentsSource
    ): MomentInfoModel

    suspend fun getMomentsPaginated(
        userId: Long,
        startId: Int,
        limit: Int,
        momentsSource: GetMomentDataUseCase.MomentsSource,
        sessionId: String?
    ): MomentInfoModel

    suspend fun getMomentById(momentId: Long): MomentItemModel

    suspend fun getUpdatedMomentViewCount(momentId: Long): Long

    suspend fun setMomentViewed(momentId: Long, userId: Long)

    suspend fun addMoment(
        filePath: String,
        isVideo: Boolean,
        gpsX: Double,
        gpsY: Double,
        place: String? = null,
        media: String,
        mediaKeyboard: Array<String>
    ): ListenableWorker.Result


    suspend fun deleteMoment(momentId: Long, userId: Long)

    suspend fun setCommentAvailability(
        momentId: Long,
        commentAvailability: Int
    )

    suspend fun momentComplain(
        remoteUserId: Long,
        reasonId: Int,
        momentId: Long
    )

    /**
     * Поделиться моментом в чате
     * @param momentId id момента, которым надо делиться
     * @param userIds список Id пользователей в чатах
     * @param comment присоединённый к ссылке комментарий
     */
    suspend fun shareMoment(
        momentId: Long,
        userIds: List<Long>,
        roomIds: List<Long>,
        comment: String
    ): MomentItemModel

    /**
     * Получить ссылку для момента
     * @param momentId id момента, ссылку на который нужно получить
     */
    suspend fun getMomentLink(
        momentId: Long
    ): MomentLinkModel

    suspend fun updateUserMomentsState(action: MomentsAction, userMomentsStateUpdate: UserMomentsStateUpdateModel)

    suspend fun updateProfileUserMomentsState(userId: Long)

    fun getPagingParams(momentsSource: GetMomentDataUseCase.MomentsSource): MomentPagingParams

    fun setPagingParams(momentsSource: GetMomentDataUseCase.MomentsSource, momentPagingParams: MomentPagingParams)
}

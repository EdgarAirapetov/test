package com.numplates.nomera3.modules.reaction.domain.repository

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AppScope
class ReactionRepositoryImpl @Inject constructor(
    private val postsRepository: PostsRepository,
    private val momentsRepository: MomentsRepository
) : ReactionRepository {

    private val subject = PublishSubject.create<ReactionUpdate>()
    private val subjectMeera = PublishSubject.create<MeeraReactionUpdate>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val postReactionUpdatesFlow = MutableSharedFlow<PostEntityResponse>()

    override fun getCommandReactionStream(): Observable<ReactionUpdate> {
        return subject
    }

    override fun getCommandReactionStreamMeera(): Observable<MeeraReactionUpdate> {
        return subjectMeera
    }

    override fun addReaction(reactionSource: ReactionSource, reactionType: ReactionType, reactionList: List<ReactionEntity>) {
        val reactions = reactionList.toMutableList()
        reactions.upVoteReaction(reactionType)

        updateReactionSourceData(reactionSource, reactions)

        subject.onNext(ReactionUpdate(ReactionUpdate.Type.Add, reactionSource, reactionType, reactions))
    }

    override fun addReactionMeera(reactionSource: MeeraReactionSource, reactionType: ReactionType, reactionList: List<ReactionEntity>) {
        val reactions = reactionList.toMutableList()
        reactions.upVoteReaction(reactionType)

        updateReactionSourceDataMeera(reactionSource, reactions)

        subjectMeera.onNext(MeeraReactionUpdate(MeeraReactionUpdate.Type.Add, reactionSource, reactionType, reactions))
    }

    override fun removeReaction(reactionSource: ReactionSource, reactionType: ReactionType, reactionList: List<ReactionEntity>) {
        val reactions = reactionList.toMutableList()
        reactions.tryDownVoteReaction(reactionType)

        updateReactionSourceData(reactionSource, reactions)

        subject.onNext(ReactionUpdate(ReactionUpdate.Type.Remove, reactionSource, reactionType, reactions))
    }

    override fun removeReactionMeera(reactionSource: MeeraReactionSource, reactionType: ReactionType, reactionList: List<ReactionEntity>) {
        val reactions = reactionList.toMutableList()
        reactions.tryDownVoteReaction(reactionType)

        updateReactionSourceDataMeera(reactionSource, reactions)

        subjectMeera.onNext(MeeraReactionUpdate(MeeraReactionUpdate.Type.Remove, reactionSource, reactionType, reactions))
    }

    override fun observeReactionPostUpdates(): Flow<PostEntityResponse> = postReactionUpdatesFlow

    override fun setReactionPostUpdate(postEntityResponse: PostEntityResponse) {
        coroutineScope.launch {
            postReactionUpdatesFlow.emit(postEntityResponse)
        }
    }

    private fun updateReactionSourceData(reactionSource: ReactionSource, reactionList: List<ReactionEntity>) {
        coroutineScope.launch {
            when (reactionSource) {
                is ReactionSource.Post -> {
                    postsRepository.updatePostReactions()
                }
                is ReactionSource.Moment -> {
                    momentsRepository.updateMomentReactions(
                        momentId = reactionSource.momentId,
                        reactionList = reactionList
                    )
                }
                else -> Unit
            }
        }
    }

    private fun updateReactionSourceDataMeera(reactionSource: MeeraReactionSource, reactionList: List<ReactionEntity>) {
        coroutineScope.launch {
            when (reactionSource) {
                is MeeraReactionSource.Post -> {
                    postsRepository.updatePostReactions()
                }
                is MeeraReactionSource.Moment -> {
                    momentsRepository.updateMomentReactions(
                        momentId = reactionSource.momentId,
                        reactionList = reactionList
                    )
                }
                else -> Unit
            }
        }
    }

    private fun MutableList<ReactionEntity>.upVoteReaction(reactionType: ReactionType) {
        val existedReactionIndex =
            this.indexOfFirst { reactionEntity -> reactionEntity.reactionType == reactionType.value }

        if (existedReactionIndex >= 0) {
            val existedReaction = this[existedReactionIndex]

            this.tryRemoveOtherMyReaction()
            this.remove(existedReaction)
            this.add(existedReaction.copy(count = existedReaction.count + 1, isMine = 1))
        } else {
            this.tryRemoveOtherMyReaction()
            this.add(ReactionEntity(count = 1, isMine = 1, reactionType.value))
        }
    }

    private fun MutableList<ReactionEntity>.tryDownVoteReaction(reactionType: ReactionType) {
        val existedReactionIndex =
            this.indexOfFirst { reactionEntity -> reactionEntity.reactionType == reactionType.value }

        if (existedReactionIndex >= 0) {
            val existedReaction = this[existedReactionIndex]

            if (existedReaction.count > 1) {
                this.remove(existedReaction)
                this.add(existedReaction.copy(count = existedReaction.count - 1, isMine = 0))
            } else {
                this.remove(existedReaction)
            }
        } else {
            // do nothing
        }
    }

    private fun MutableList<ReactionEntity>.tryRemoveOtherMyReaction() {
        val mineIndex = this.indexOfFirst { it.isMine == 1 }

        if (mineIndex >= 0) {
            val mineReaction = this[mineIndex]

            val newCount = mineReaction.count - 1

            if (newCount > 0) {
                this.removeAt(mineIndex)
                this.add(mineIndex, mineReaction.copy(isMine = 0, count = newCount))
            } else {
                this.removeAt(mineIndex)
            }
        }
    }
}

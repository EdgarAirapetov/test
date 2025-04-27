package com.numplates.nomera3.modules.moments.comments.presentation

import androidx.lifecycle.MutableLiveData
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class MomentCommentReactionModel(
    private val reactionRepository: ReactionRepository,
    private val commentList: List<CommentUIType>,
    private val viewEvent: MutableLiveData<MomentsCommentViewEvent>
) {
    private val disposables = CompositeDisposable()

    init {
        initListenReaction()
    }

    fun onCleared() {
        disposables.clear()
    }

    private fun initListenReaction() {
        val disposable = reactionRepository
            .getCommandReactionStream()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { reactionUpdate ->
                proceedReaction(reactionUpdate)
            }

        disposables.add(disposable)
    }

    private fun proceedReaction(reactionUpdate: ReactionUpdate) {
        when (reactionUpdate.reactionSource) {
            is ReactionSource.MomentComment -> {
                val commentId = reactionUpdate.reactionSource.commentId
                val commentPosition = commentList.indexOfFirst { it.id == commentId }
                if (commentPosition == -1) return
                val comment = commentList[commentPosition] as? CommentEntity ?: return

                comment.comment.reactions = reactionUpdate.reactionList

                viewEvent.postValue(
                    MomentsCommentViewEvent.UpdateCommentReaction(
                        commentPosition,
                        reactionUpdate
                    )
                )
            }
            else -> Unit
        }
    }
}

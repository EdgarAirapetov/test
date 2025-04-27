package com.numplates.nomera3.modules.reaction.domain.repository

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

interface ReactionRepository {
    fun getCommandReactionStream(): Observable<ReactionUpdate>
    fun getCommandReactionStreamMeera(): Observable<MeeraReactionUpdate>
    fun addReaction(reactionSource: ReactionSource, reactionType: ReactionType, reactionList: List<ReactionEntity>)
    fun addReactionMeera(reactionSource: MeeraReactionSource, reactionType: ReactionType, reactionList: List<ReactionEntity>)
    fun removeReaction(reactionSource: ReactionSource, reactionType: ReactionType, reactionList: List<ReactionEntity>)
    fun removeReactionMeera(reactionSource: MeeraReactionSource, reactionType: ReactionType, reactionList: List<ReactionEntity>)
    fun observeReactionPostUpdates(): Flow<PostEntityResponse>
    fun setReactionPostUpdate(postEntityResponse: PostEntityResponse)
}

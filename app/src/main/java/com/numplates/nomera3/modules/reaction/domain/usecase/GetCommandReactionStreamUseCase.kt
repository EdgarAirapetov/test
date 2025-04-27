package com.numplates.nomera3.modules.reaction.domain.usecase

import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetCommandReactionStreamUseCase @Inject constructor(
    private val reactionRepository: ReactionRepository
    ) {

    fun execute(): Observable<ReactionUpdate> {
        return reactionRepository
            .getCommandReactionStream()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

package com.numplates.nomera3.modules.reaction.domain.usecase

import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MeeraGetCommandReactionStreamUseCase @Inject constructor(
    private val reactionRepository: ReactionRepository
) {

    fun execute(): Observable<MeeraReactionUpdate> {
        return reactionRepository
            .getCommandReactionStreamMeera()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.posts.domain.model.PostActionModel
import javax.inject.Inject

class UpdatePostStateUseCase @Inject constructor(
    private val repository: PostsRepository
) {
    fun invoke(model: PostActionModel.PostEditingStartModel) {
        repository.newPostObservable.onNext(model)
    }
}

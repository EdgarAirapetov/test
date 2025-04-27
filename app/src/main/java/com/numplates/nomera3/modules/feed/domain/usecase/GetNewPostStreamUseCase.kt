package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.posts.domain.model.PostActionModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class GetNewPostStreamUseCase @Inject constructor(
    private val repository: PostsRepository
) {
    fun invoke(): Observable<PostActionModel> {
        return repository.newPostObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
    }
}

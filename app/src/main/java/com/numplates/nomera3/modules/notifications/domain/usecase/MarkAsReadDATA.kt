package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.domain.BaseUseCase
import com.numplates.nomera3.modules.notifications.domain.DefParams
import io.reactivex.Observable
import org.phoenixframework.Message
import javax.inject.Inject

class MarkAsReadDATA @Inject constructor(
        private val repository: NotificationRepository
) : BaseUseCase<MarkPostCommentParams, Observable<Message>> {

    override fun execute(params: MarkPostCommentParams): Observable<Message> {
        return if (params.shouldMarkPost){
            repository.markAsReadPostNotification(params.postId)
        } else {
            repository.markAsReadCommentNotification(params.postId)
        }
    }
}

data class MarkPostCommentParams(
        val postId: Long,
        val shouldMarkPost: Boolean
) : DefParams()

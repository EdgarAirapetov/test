package com.numplates.nomera3.modules.comments.data.repository

import com.meera.core.di.modules.APPLICATION_COROUTINE_SCOPE
import com.meera.core.network.MeraNetworkException
import com.numplates.nomera3.modules.comments.data.api.CommentsApi
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.api.SortByType
import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class CommentsRepositoryImpl @Inject constructor(
        private val commentsApi: CommentsApi,
        @Named(APPLICATION_COROUTINE_SCOPE)
        private val applicationScope: CoroutineScope
) : PostCommentsRepository {

    override suspend fun commentComplain(commentId: Long): Boolean {
        return try {
            val response = commentsApi.addComplainV2(hashMapOf("comment_id" to commentId))
            response?.data != null
        } catch (e: Exception) {
            Timber.e("Add complained failed ${e.message}")
            false
        }
    }

    override suspend fun momentCommentComplain(commentId: Long): Boolean {
        return try {
            val response = commentsApi.addComplainV2(hashMapOf("moment_comment_id" to commentId))
            response?.data != null
        } catch (e: Exception) {
            Timber.e("Add complained failed ${e.message}")
            false
        }
    }

    override suspend fun fetchComments(postId: Long,
                                       limit: Long,
                                       startId: Long?,
                                       parentId: Long?,
                                       commentId: Long?,
                                       order: OrderType, sortBy: SortByType,
                                       success: (CommentsEntityResponse) -> Unit,
                                       fail: (Exception) -> Unit) {
        try {
            val r0 = commentsApi.fetchComments(postId, limit, startId, parentId, commentId, order.value, sortBy.v0)
            if (r0.data == null) fail(IllegalArgumentException("${r0.err.code}"))
            else success(r0.data)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    /**
     * Удалить комментарий https://nomera.atlassian.net/wiki/spaces/NOMIT/pages/1260945417/Posts#Delete-post-comment
     * */
    override suspend fun deletePostComment(commentId: Long) {
        withContext(applicationScope.coroutineContext) {
            commentsApi.deletePostComment(commentId)
        }
    }

    override suspend fun sendComment(
            postId: Long,
            text: String,
            commentId: Long,
            errorTypeListener: (SendCommentError) -> Unit
    ): SendCommentResponse? {
        return try {
            val response = commentsApi.sendComment(
                    postId = postId,
                    params = hashMapOf("post_id" to postId, "text" to text, "comment_id" to commentId)
            )
            if (response?.data == null && response?.err == null) {
                throw SendCommentException()
            }
            response.err?.message?.let {
                errorTypeListener(SendCommentError.UserDeletedPostComment(it))
            }
            return response.data
        } catch (e: Exception) {
            when(e) {
                is MeraNetworkException -> errorTypeListener(SendCommentError.UnknownHost)
                is SendCommentException -> errorTypeListener(SendCommentError.SendFail)
            }
            Timber.e(e)
            null
        }
    }

    override suspend fun requestLastComments(postId: Long,
                                             limit: Long,
                                             fail: (Exception) -> Unit,
                                             success: (CommentsEntityResponse?) -> Unit) {
        try {
            val result = commentsApi.requestLastComment(postId, limit)
            success(result.data)
        }catch (e: java.lang.Exception){
            Timber.e(e)
            fail(e)
        }

    }

}

sealed class SendCommentError{
    object UnknownHost : SendCommentError()

    object SendFail: SendCommentError()

    class UserDeletedPostComment(
            val messageError: String = ""
    ): SendCommentError()
}

class SendCommentException : Exception()

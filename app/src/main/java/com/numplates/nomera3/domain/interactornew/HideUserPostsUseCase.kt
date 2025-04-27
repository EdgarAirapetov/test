package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

class HideUserPostsUseCase(private val apiHiWay: ApiHiWay,
                           private val apiMain: ApiMain) {

    fun hidePosts(userId: Long, hideStatus: Int) : Flowable<ResponseWrapper<Any>> {
        val body: HashMap<String, Int> = hashMapOf()
        body["hide"] = hideStatus
        return apiHiWay.hidePosts(userId, body)
    }


    suspend fun hidePostsV2(userId: Long, hideStatus: Int) : ResponseWrapper<Any?>? {
        val body: HashMap<String, Int> = hashMapOf()
        body["hide"] = hideStatus
        return apiMain.hidePosts(userId, body)
    }

    suspend fun showUserPosts(userId: Long) = apiMain.unhidePosts(userId)

}
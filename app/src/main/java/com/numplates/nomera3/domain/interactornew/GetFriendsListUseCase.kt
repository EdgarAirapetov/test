package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.UserModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

class GetFriendsListUseCase(private val repository: ApiHiWay?,
                            var userId: Long = 0L,
                            var startIndex: Int = 0) {

    companion object {
        const val FRIENDS = 0
        const val BLACKLIST = 1
        const val OUTCOMING = 2
        const val INCOMING = 3
        const val SUBSCRIBERS = 4
        const val SUBSCRIPTIONS = 5
    }


    fun setParams(userId: Long, startIndex: Int) {
        this.userId = userId
        this.startIndex = startIndex
    }

    fun getFriendsBlockedList(): Flowable<ResponseWrapper<MutableList<UserModel>>>? {
        return repository?.getFriendBlockedList(userId)
    }

    fun getFriendOutcomingList(page: Int): Flowable<ResponseWrapper<MutableList<UserModel>>>? {
        return repository?.getFriendOutcomingList(userId, page)
    }
}

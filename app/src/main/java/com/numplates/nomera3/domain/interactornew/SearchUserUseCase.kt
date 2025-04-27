package com.numplates.nomera3.domain.interactornew

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.UserSearchByNameModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

class SearchUserUseCase(private val repository: ApiHiWay) {

    /**
     * query - search string
     * friends type - (0 all users)
     * limit - 20 (default)
     * offset - 0 (default)
     */

// https://nomeraworkspace.slack.com/archives/C013HKF6MJS/p1593068576020600?thread_ts=1592844688.013400&cid=C013HKF6MJS
//    case notFriend = 0
//    case incomingFriendRequest = 1
//    case friend = 2
//    case outgoingFriendRequest = 3
//    case blacklist = 4
//    case all

    // поиск по исходящим заявка в друзья
    fun searchOutgoingRequestedFriends(
            query: String,
            limit: Int = 20,
            offset: Int = 0
    ): Flowable<ResponseWrapper<List<UserSearchByNameModel>>> {
        return repository.searchUserExt(
                query,
                3,
                limit,
                offset
        )
    }

    fun searchFriendUserSimple(
            query: String,
            limit: Int = 20,
            offset: Int = 0
    ): Flowable<ResponseWrapper<MutableList<UserSimple>>>? {
        return repository.searchUserSimple(
                query,
                2,
                limit,
                offset,
                "UserSimple"
        )
    }

}

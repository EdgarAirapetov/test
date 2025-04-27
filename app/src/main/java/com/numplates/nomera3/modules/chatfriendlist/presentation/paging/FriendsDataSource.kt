package com.numplates.nomera3.modules.chatfriendlist.presentation.paging

import androidx.paging.PositionalDataSource
import com.meera.db.models.userprofile.UserSimple

class FriendsDataSource(
    private val friendNameQuery: String? = null,
    private val dataCallback: FriendsDataCallback,
) : PositionalDataSource<UserSimple>() {

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<UserSimple>
    ) {
        val result = dataCallback.getData(
            nameQuery = friendNameQuery,
            startingFrom = params.requestedStartPosition,
            howMuch = params.requestedLoadSize,
        )
        if (result != null) {
            callback.onResult(result, DEFAULT_PAGING_START_POSITION)
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<UserSimple>) {
        val result = dataCallback.getData(
            nameQuery = friendNameQuery,
            startingFrom = params.startPosition,
            howMuch = params.loadSize,
        )
        if (result != null) {
            callback.onResult(result)
        }
    }

    fun getCurrentNameQuery() = friendNameQuery

    companion object {
        private const val DEFAULT_PAGING_START_POSITION = 0
    }
}

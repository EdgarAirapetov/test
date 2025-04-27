package com.numplates.nomera3.modules.chatfriendlist.presentation.paging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.meera.db.models.userprofile.UserSimple

class FriendsDataSourceFactory(
    private val dataCallback: FriendsDataCallback
): DataSource.Factory<Int, UserSimple>() {

    private var currentFriendNameQuery: String? = null

    private val _sourceLiveData = MutableLiveData<FriendsDataSource>()
    val sourceLiveData: LiveData<FriendsDataSource>
        get() = _sourceLiveData

    override fun create(): DataSource<Int, UserSimple> {
        val newDataSource = FriendsDataSource(currentFriendNameQuery, dataCallback)
        _sourceLiveData.postValue(newDataSource)
        return newDataSource
    }

    fun newNameQuery(newQuery: String?) {
        currentFriendNameQuery = newQuery
        _sourceLiveData.value?.invalidate()
    }

    fun isSearchingAllFriends(): Boolean =
        _sourceLiveData.value?.getCurrentNameQuery().isNullOrBlank()

    fun invalidateData() = _sourceLiveData.value?.invalidate()
}

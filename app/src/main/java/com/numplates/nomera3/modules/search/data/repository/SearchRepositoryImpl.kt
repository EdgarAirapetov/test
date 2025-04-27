package com.numplates.nomera3.modules.search.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.search.data.api.SearchApi
import com.numplates.nomera3.modules.search.data.entity.RecentGroupEntityResponse
import com.numplates.nomera3.modules.search.data.entity.RecentHashtagEntityResponse
import com.numplates.nomera3.modules.search.data.entity.RecentUserEntityResponse
import com.numplates.nomera3.modules.search.data.entity.SearchGroupEntityResponse
import com.numplates.nomera3.modules.tags.data.api.TagApi
import com.numplates.nomera3.modules.tags.data.entity.HashtagTagListModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

@AppScope
class SearchRepositoryImpl @Inject constructor(
    private val searchApi: SearchApi,
    private val tagApi: TagApi
) : SearchRepository {

    private val searchNumberParamsFlow = MutableStateFlow<NumberSearchParameters?>(null)

    override fun setNumberSearchParams(numberSearchParameters: NumberSearchParameters) {
        searchNumberParamsFlow.value = numberSearchParameters
    }

    override fun observeNumberSearchParamsChange(): Flow<NumberSearchParameters?> {
        return searchNumberParamsFlow
    }

    override suspend fun requestSubscribeGroup(
        groupId: Int,
        success: (List<*>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = searchApi.subscribeGroup(groupId)
            if (response.data != null) {
                success(response.data)
            } else {
                fail(IllegalArgumentException("Empty response"))
            }
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun requestSearchUsers(
        query: String,
        limit: Int,
        offset: Int,
        gender: Int?,
        ageFrom: Int?,
        ageTo: Int?,
        cityIds: String?,
        countryIds: String?,
        success: (List<UserSimple>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = searchApi.searchUsers(
                query = query,
                friendsType = null,
                limit = limit,
                offset = offset,
                userType = USER_TYPE_SIMPLE,
                cityIds = cityIds,
                countryIds = countryIds,
                gender = gender,
                ageFrom = ageFrom,
                ageTo = ageTo
            )
            if (response.data == null) fail(IllegalArgumentException("Empty response"))
            else success(response.data)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun requestSearchGroups(
        query: String,
        limit: Int,
        offset: Int,
        success: (SearchGroupEntityResponse) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = searchApi.searchGroups(query, offset, limit, 0)
            if (response.data == null) fail(IllegalArgumentException("Empty response"))
            else success(response.data)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun requestSearchHashTags(
        query: String,
        limit: Int,
        offset: Int,
        success: (HashtagTagListModel) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = tagApi.getTagListByHashtag(query, limit, offset)
            if (response.data == null) fail(IllegalArgumentException("Empty response"))
            else success(response.data)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun requestSearchByNumber(
        number: String,
        typeId: Int,
        countryId: Int,
        success: (List<UserSimple>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = searchApi.searchUsersByNumber(number, countryId, typeId,
                USER_TYPE_SIMPLE_WITH_VEHICLE)
            if (response.data == null) fail(IllegalArgumentException("Empty response"))
            else success(response.data)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun requestRecentUsers(
        success: (List<RecentUserEntityResponse>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = searchApi.recentSearchUsers()
            if (response.error != null) fail(IllegalArgumentException("Empty response"))
            else success(response.success)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun getRecentUsers(): List<RecentUserEntityResponse> {
        val response = searchApi.recentSearchUsers()
        return response.success
    }

    override suspend fun requestRecentGroups(
        success: (List<RecentGroupEntityResponse>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = searchApi.recentSearchGroups()
            if (response.error != null) fail(IllegalArgumentException("Empty response"))
            else success(response.success)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun requestRecentHashtags(
        success: (List<RecentHashtagEntityResponse>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = searchApi.recentSearchHashtags()
            if (response.error != null) fail(IllegalArgumentException("Empty response"))
            else success(response.success)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun requestCleanRecentUsers(success: () -> Unit, fail: (Exception) -> Unit) {
        try {
            searchApi.clearSearchRecent(RECENT_SEARCH_TYPE_USER)
            success()
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun requestCleanRecentGroups(success: () -> Unit, fail: (Exception) -> Unit) {
        try {
            searchApi.clearSearchRecent(RECENT_SEARCH_TYPE_GROUP)
            success()
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun requestCleanRecentHashtags(
        success: () -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            searchApi.clearSearchRecent(RECENT_SEARCH_TYPE_HASHTAG)
            success()
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    companion object {
        const val USER_TYPE_SIMPLE = "UserSimple"
        const val USER_TYPE_SIMPLE_WITH_VEHICLE = "UserSimpleWithVehicle"
        const val RECENT_SEARCH_TYPE_USER = "user"
        const val RECENT_SEARCH_TYPE_GROUP = "group"
        const val RECENT_SEARCH_TYPE_HASHTAG = "hashtag"
    }
}

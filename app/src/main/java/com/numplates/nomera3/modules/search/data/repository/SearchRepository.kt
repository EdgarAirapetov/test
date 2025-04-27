package com.numplates.nomera3.modules.search.data.repository

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.search.data.entity.RecentGroupEntityResponse
import com.numplates.nomera3.modules.search.data.entity.RecentHashtagEntityResponse
import com.numplates.nomera3.modules.search.data.entity.RecentUserEntityResponse
import com.numplates.nomera3.modules.search.data.entity.SearchGroupEntityResponse
import com.numplates.nomera3.modules.tags.data.entity.HashtagTagListModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun setNumberSearchParams(numberSearchParameters: NumberSearchParameters)

    fun observeNumberSearchParamsChange(): Flow<NumberSearchParameters?>

    suspend fun requestSubscribeGroup(
        groupId: Int,
        success: (List<*>) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun requestSearchUsers(query: String,
                                   limit: Int,
                                   offset: Int,
                                   gender: Int?,
                                   ageFrom: Int?,
                                   ageTo: Int?,
                                   cityIds: String?,
                                   countryIds: String?,
                                   success: (List<UserSimple>) -> Unit,
                                   fail: (Exception) -> Unit)

    suspend fun requestSearchGroups(query: String,
                                    limit: Int,
                                    offset: Int,
                                    success: (SearchGroupEntityResponse) -> Unit,
                                    fail: (Exception) -> Unit)

    suspend fun requestSearchHashTags(query: String,
                                      limit: Int,
                                      offset: Int,
                                      success: (HashtagTagListModel) -> Unit,
                                      fail: (Exception) -> Unit)

    suspend fun requestSearchByNumber(number: String,
                                      typeId: Int,
                                      countryId: Int,
                                      success: (List<UserSimple>) -> Unit,
                                      fail: (Exception) -> Unit)

    suspend fun requestRecentUsers(success: (List<RecentUserEntityResponse>) -> Unit,
                                   fail: (Exception) -> Unit)

    suspend fun getRecentUsers(): List<RecentUserEntityResponse>

    suspend fun requestRecentGroups(success: (List<RecentGroupEntityResponse>) -> Unit,
                                    fail: (Exception) -> Unit)

    suspend fun requestRecentHashtags(success: (List<RecentHashtagEntityResponse>) -> Unit,
                                      fail: (Exception) -> Unit)

    suspend fun requestCleanRecentUsers(success: () -> Unit, fail: (Exception) -> Unit)

    suspend fun requestCleanRecentGroups(success: () -> Unit, fail: (Exception) -> Unit)

    suspend fun requestCleanRecentHashtags(success: () -> Unit, fail: (Exception) -> Unit)
}

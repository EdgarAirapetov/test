package com.numplates.nomera3.modules.search.domain.usecase

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.search.data.repository.SearchRepository
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val repository: SearchRepository
) : BaseUseCaseCoroutine<SearchUsersParams, List<UserSimple>> {

    override suspend fun execute(
        params: SearchUsersParams,
        success: (List<UserSimple>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.requestSearchUsers(
            query = params.query,
            limit = params.limit,
            offset = params.offset,
            gender = params.gender,
            ageFrom = params.ageFrom,
            ageTo = params.ageTo,
            cityIds = params.getCityIds(),
            countryIds = params.getCountryIds(),
            success,
            fail
        )
    }
}

class SearchUsersParams(
    val query: String,
    val limit: Int,
    val offset: Int,
    val gender: Int?,
    val ageFrom: Int?,
    val ageTo: Int?,
    private val cityIds: List<Int>?,
    private val countryIds: List<Int>?
) : DefParams() {

    fun getCountryIds(): String? {
        return countryIds?.makeString()
    }

    fun getCityIds(): String? {
        return cityIds?.makeString()
    }

    private fun List<Int>.makeString(): String {
        return this.toIntArray()
            .contentToString()
            .replace("[", "")
            .replace("]", "")
            .replace(" ", "")
    }
}

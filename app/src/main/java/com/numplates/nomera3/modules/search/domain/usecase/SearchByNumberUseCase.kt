package com.numplates.nomera3.modules.search.domain.usecase

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.search.data.repository.SearchRepository
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import java.lang.Exception
import javax.inject.Inject

class SearchByNumberUseCase @Inject constructor(
    private val repository: SearchRepository
) : BaseUseCaseCoroutine<SearchByNumberUseCaseParams, List<UserSimple>> {
    override suspend fun execute(
        params: SearchByNumberUseCaseParams,
        success: (List<UserSimple>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.requestSearchByNumber(
            number = params.number,
            typeId = params.typeId,
            countryId = params.countryId,
            success = success,
            fail = fail
        )
    }
}

data class SearchByNumberUseCaseParams(
    val number: String,
    val countryId: Int,
    val typeId: Int
) : DefParams()

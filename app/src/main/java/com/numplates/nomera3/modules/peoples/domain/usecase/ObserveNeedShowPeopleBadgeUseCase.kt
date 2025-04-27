package com.numplates.nomera3.modules.peoples.domain.usecase

import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNeedShowPeopleBadgeUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    fun invoke(): Flow<Boolean> = repository.observeNeedShowPeopleBadge()
}

package com.numplates.nomera3.modules.peoples.domain.usecase

import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import javax.inject.Inject

class SetSelectCommunityTooltipShownUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    fun invoke() = repository.setSelectCommunityTooltipShown()
}

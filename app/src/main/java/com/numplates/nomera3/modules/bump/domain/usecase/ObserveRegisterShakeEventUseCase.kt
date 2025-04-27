package com.numplates.nomera3.modules.bump.domain.usecase

import com.numplates.nomera3.modules.bump.data.mapper.ShakeDataMapper
import com.numplates.nomera3.modules.bump.domain.entity.ShakeEvent
import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveRegisterShakeEventUseCase @Inject constructor(
    private val repository: ShakeRepository,
    private val mapper: ShakeDataMapper
) {
    fun invoke(): Flow<ShakeEvent> {
        return repository.observeShakeRegisteredChanged()
            .map { event ->
                mapper.mapShakeDataEvent(event)
            }
    }
}

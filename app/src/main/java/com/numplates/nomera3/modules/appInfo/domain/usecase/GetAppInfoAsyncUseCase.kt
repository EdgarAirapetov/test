package com.numplates.nomera3.modules.appInfo.domain.usecase

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.data.repository.AppInfoAsyncRepository
import kotlinx.coroutines.Deferred
import javax.inject.Inject

@AppScope
class GetAppInfoAsyncUseCase @Inject constructor(
    private val repository: AppInfoAsyncRepository
) {
    fun resetCache() = repository.resetCache()

    fun executeAsync(): Deferred<Settings> = repository.executeAsync()

    fun executeBlocking(): Settings? = repository.executeBlocking()
}

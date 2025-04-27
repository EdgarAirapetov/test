package com.numplates.nomera3.modules.appInfo.domain.usecase

import com.numplates.nomera3.modules.appInfo.data.entity.UpdateResponse
import com.numplates.nomera3.modules.appInfo.data.repository.AppInfoRepository
import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import javax.inject.Inject

class GetUpdateInfoUseCase @Inject constructor(
    private val repository: AppInfoRepository
) : BaseUseCaseNoSuspend<DefParams, UpdateResponse> {

    override fun execute(params: DefParams) = repository.requestUpdateAppSettings()

    suspend fun execute(
        success: (UpdateResponse) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.requestUpdateAppSettingsNetwork(success, fail)
    }
}

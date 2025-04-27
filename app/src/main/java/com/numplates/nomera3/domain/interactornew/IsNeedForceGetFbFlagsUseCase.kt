package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.appInfo.data.repository.AppInfoRepository
import javax.inject.Inject

private const val DEF_VALUE_CASE = true

class IsNeedForceGetFbFlagsUseCase @Inject constructor(
    private val repository: AppInfoRepository
) {

    suspend fun invoke(): Boolean =
        runCatching {
            val currentVersion = repository.getSettings().currentApp?.version.orEmpty()
            val fbVersion = repository.getFbForceUpdateAppVersion()
            repository.setFbForceUpdateAppVersion(currentVersion)

            currentVersion != fbVersion
        }.getOrDefault(DEF_VALUE_CASE)

}

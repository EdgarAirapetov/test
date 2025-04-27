package com.numplates.nomera3.modules.appInfo.domain.usecase

import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.data.repository.AppInfoRepository
import javax.inject.Inject

class GetAppInfoUseCase @Inject constructor(private val appInfoRepository: AppInfoRepository) {

    suspend operator fun invoke(): Settings = appInfoRepository.getSettings()
}

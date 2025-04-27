package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityAnalyticsSettingsInitUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilitySettingsAnalyticsChangeCountUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilitySettingsAnalyticsLogDataUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.AddMapBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.DeleteMapBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.GetMapBlacklistWithCounterUseCase
import javax.inject.Inject


class MapSettingsBlacklistViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    val mapVisibilityAnalyticsSettingsInitUseCase: MapVisibilityAnalyticsSettingsInitUseCase,
    val mapVisibilitySettingsAnalyticsChangeCountUseCase: MapVisibilitySettingsAnalyticsChangeCountUseCase,
    val mapVisibilitySettingsAnalyticsLogDataUseCase: MapVisibilitySettingsAnalyticsLogDataUseCase,
    private val getMapBlacklistWithCounterUseCase: GetMapBlacklistWithCounterUseCase,
    private val addMapBlacklistUseCase: AddMapBlacklistUseCase,
    private val deleteMapBlacklistUseCase: DeleteMapBlacklistUseCase,
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getMapBlacklistWithCounterUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addMapBlacklistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteMapBlacklistUseCase.invoke(userIds)
    }
}

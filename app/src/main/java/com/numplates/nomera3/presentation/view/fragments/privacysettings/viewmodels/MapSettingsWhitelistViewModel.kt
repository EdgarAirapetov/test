package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityAnalyticsSettingsInitUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilitySettingsAnalyticsChangeCountUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilitySettingsAnalyticsLogDataUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.AddMapWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.DeleteMapWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.GetMapWhitelistWithCounterUseCase
import javax.inject.Inject


class MapSettingsWhitelistViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    val mapVisibilitySettingsAnalyticsLogDataUseCase: MapVisibilitySettingsAnalyticsLogDataUseCase,
    val mapVisibilitySettingsAnalyticsChangeCountUseCase: MapVisibilitySettingsAnalyticsChangeCountUseCase,
    val mapVisibilityAnalyticsSettingsInitUseCase: MapVisibilityAnalyticsSettingsInitUseCase,
    private val getMapWhitelistWithCounterUseCase: GetMapWhitelistWithCounterUseCase,
    private val addMapWhitelistUseCase: AddMapWhitelistUseCase,
    private val deleteMapWhitelistUseCase: DeleteMapWhitelistUseCase,
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int, offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getMapWhitelistWithCounterUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addMapWhitelistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteMapWhitelistUseCase.invoke(userIds)
    }
}

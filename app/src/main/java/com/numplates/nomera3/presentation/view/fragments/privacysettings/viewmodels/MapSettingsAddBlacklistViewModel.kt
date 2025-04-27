package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityAnalyticsSettingsInitUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilitySettingsAnalyticsChangeCountUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilitySettingsAnalyticsLogDataUseCase
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.AddMapBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.DeleteMapBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.SearchMapBlacklistUseCase
import javax.inject.Inject

class MapSettingsAddBlacklistViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var mapVisibilityAnalyticsSettingsInitUseCase: MapVisibilityAnalyticsSettingsInitUseCase

    @Inject
    lateinit var mapVisibilitySettingsAnalyticsChangeCountUseCase: MapVisibilitySettingsAnalyticsChangeCountUseCase

    @Inject
    lateinit var mapVisibilitySettingsAnalyticsLogDataUseCase: MapVisibilitySettingsAnalyticsLogDataUseCase

    @Inject
    lateinit var searchMapBlacklistUseCase: SearchMapBlacklistUseCase

    @Inject
    lateinit var addMapBlacklistUseCase: AddMapBlacklistUseCase

    @Inject
    lateinit var deleteMapBlacklistUseCase: DeleteMapBlacklistUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchMapBlacklistUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchMapBlacklistUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addMapBlacklistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteMapBlacklistUseCase.invoke(userIds)
    }
}

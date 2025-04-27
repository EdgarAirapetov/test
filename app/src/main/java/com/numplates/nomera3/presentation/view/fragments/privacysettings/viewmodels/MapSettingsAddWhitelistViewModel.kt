package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityAnalyticsSettingsInitUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilitySettingsAnalyticsChangeCountUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilitySettingsAnalyticsLogDataUseCase
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.AddMapWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.DeleteMapWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.map.SearchMapWhitelistUseCase
import javax.inject.Inject

class MapSettingsAddWhitelistViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var mapVisibilityAnalyticsSettingsInitUseCase: MapVisibilityAnalyticsSettingsInitUseCase

    @Inject
    lateinit var mapVisibilitySettingsAnalyticsChangeCountUseCase: MapVisibilitySettingsAnalyticsChangeCountUseCase

    @Inject
    lateinit var mapVisibilitySettingsAnalyticsLogDataUseCase: MapVisibilitySettingsAnalyticsLogDataUseCase

    @Inject
    lateinit var searchMapWhitelistUseCase: SearchMapWhitelistUseCase

    @Inject
    lateinit var addMapWhitelistUseCase: AddMapWhitelistUseCase

    @Inject
    lateinit var deleteMapWhitelistUseCase: DeleteMapWhitelistUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchMapWhitelistUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchMapWhitelistUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addMapWhitelistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteMapWhitelistUseCase.invoke(userIds)
    }
}

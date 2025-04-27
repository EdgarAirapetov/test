package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.AddOnlineWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.DeleteOnlineWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.GetOnlineWhitelistWithCounterUseCase
import javax.inject.Inject

class OnlineSettingsWhitelistViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val getOnlineWhitelistWithCounterUseCase: GetOnlineWhitelistWithCounterUseCase,
    private val addOnlineWhitelistUseCase: AddOnlineWhitelistUseCase,
    private val deleteOnlineWhitelist: DeleteOnlineWhitelistUseCase
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getOnlineWhitelistWithCounterUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addOnlineWhitelistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteOnlineWhitelist.invoke(userIds)
    }
}

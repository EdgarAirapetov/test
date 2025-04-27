package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.AddOnlineBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.DeleteOnlineBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.GetOnlineBlacklistWithCounterUseCase
import javax.inject.Inject

class OnlineSettingsBlacklistViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val getOnlineBlacklistWithCounterUseCase: GetOnlineBlacklistWithCounterUseCase,
    private val addOnlineBlacklistUseCase: AddOnlineBlacklistUseCase,
    private val deleteOnlineBlacklistUseCase: DeleteOnlineBlacklistUseCase,
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getOnlineBlacklistWithCounterUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addOnlineBlacklistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteOnlineBlacklistUseCase.invoke(userIds)
    }
}

package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.AddCallBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.DeleteCallBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.GetCallBlacklistWithCounterUseCase
import javax.inject.Inject

class CallSettingsBlacklistViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val getCallBlacklistWithCounterUseCase: GetCallBlacklistWithCounterUseCase,
    private val addCallBlacklistUseCase: AddCallBlacklistUseCase,
    private val deleteCallBlacklistUseCase: DeleteCallBlacklistUseCase,
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getCallBlacklistWithCounterUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addCallBlacklistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteCallBlacklistUseCase.invoke(userIds)
    }
}

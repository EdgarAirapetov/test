package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.AddCallWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.DeleteCallWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.GetCallWhitelistWithCounterUseCase
import javax.inject.Inject


class CallSettingsWhitelistViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val getCallWhitelistWithCounterUseCase: GetCallWhitelistWithCounterUseCase,
    private val addCallWhitelistUseCase: AddCallWhitelistUseCase,
    private val deleteCallWhitelistUseCase: DeleteCallWhitelistUseCase,
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getCallWhitelistWithCounterUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addCallWhitelistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteCallWhitelistUseCase.invoke(userIds)
    }
}

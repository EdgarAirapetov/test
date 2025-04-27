package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.blacklist.AddBlacklistExclusionUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.blacklist.DeleteBlacklistExclusionUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.blacklist.GetBlacklistExclusionsWithCounterUseCase
import javax.inject.Inject

class BlacklistSettingsViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val getBlacklistExclusionsWithCounterUseCase: GetBlacklistExclusionsWithCounterUseCase,
    private val addBlacklistExclusionUseCase: AddBlacklistExclusionUseCase,
    private val deleteBlacklistExclusionUseCase: DeleteBlacklistExclusionUseCase,
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getBlacklistExclusionsWithCounterUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        return addBlacklistExclusionUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        return deleteBlacklistExclusionUseCase.invoke(userIds)
    }
}

package com.numplates.nomera3.modules.moments.settings.notshow.presentation

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowAddExclusionUseCase
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowDeleteExclusionUseCase
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowGetExclusionUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserListViewModel
import javax.inject.Inject

class MomentSettingsNotShowViewModel @Inject constructor(
    private val getExclusionUseCase: MomentSettingsNotShowGetExclusionUseCase,
    private val addExclusionUseCase: MomentSettingsNotShowAddExclusionUseCase,
    private val deleteExclusionUseCase: MomentSettingsNotShowDeleteExclusionUseCase,
    private val getSettingsUseCase: GetSettingsUseCase
) : BaseSettingsUserListViewModel(getSettingsUseCase) {
    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getExclusionUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addExclusionUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteExclusionUseCase.invoke(userIds)
    }
}

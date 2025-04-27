package com.numplates.nomera3.modules.moments.settings.hidefrom.presentation

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.settings.hidefrom.domain.MomentSettingsHideFromAddExclusionUseCase
import com.numplates.nomera3.modules.moments.settings.hidefrom.domain.MomentSettingsHideFromDeleteExclusionUseCase
import com.numplates.nomera3.modules.moments.settings.hidefrom.domain.MomentSettingsHideFromGetExclusionUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserListViewModel
import javax.inject.Inject

class MomentSettingsHideFromViewModel @Inject constructor(
    private val getExclusionUseCase: MomentSettingsHideFromGetExclusionUseCase,
    private val addExclusionUseCase: MomentSettingsHideFromAddExclusionUseCase,
    private val deleteExclusionUseCase: MomentSettingsHideFromDeleteExclusionUseCase,
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

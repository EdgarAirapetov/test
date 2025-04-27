package com.numplates.nomera3.modules.moments.settings.notshow.presentation

import com.meera.core.extensions.empty
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowAddExclusionUseCase
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowDeleteExclusionUseCase
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowSearchMomentUseCase
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import javax.inject.Inject

class MomentSettingsAddNotShowViewModel @Inject constructor(
    private val searchNotShowExclusionUseCase: MomentSettingsNotShowSearchMomentUseCase,
    private val addNotShowExclusionUseCase: MomentSettingsNotShowAddExclusionUseCase,
    private val deleteNotShowExclusionUseCase: MomentSettingsNotShowDeleteExclusionUseCase
) : BaseSettingsUserSearchViewModel() {

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchNotShowExclusionUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchNotShowExclusionUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addNotShowExclusionUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteNotShowExclusionUseCase.invoke(userIds)
    }
}

package com.numplates.nomera3.modules.moments.settings.hidefrom.presentation

import com.meera.core.extensions.empty
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.settings.hidefrom.domain.MomentSettingsHideFromAddExclusionUseCase
import com.numplates.nomera3.modules.moments.settings.hidefrom.domain.MomentSettingsHideFromDeleteExclusionUseCase
import com.numplates.nomera3.modules.moments.settings.hidefrom.domain.MomentSettingsHideFromSearchMomentUseCase
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import javax.inject.Inject

class MomentSettingsHideFromAddUserViewModel @Inject constructor(
    private val searchNotShowExclusionUseCase: MomentSettingsHideFromSearchMomentUseCase,
    private val momentHideFromAddExclusionUseCase: MomentSettingsHideFromAddExclusionUseCase,
    private val deleteMomentNotShowExclusionUseCase: MomentSettingsHideFromDeleteExclusionUseCase
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
        momentHideFromAddExclusionUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteMomentNotShowExclusionUseCase.invoke(userIds)
    }
}

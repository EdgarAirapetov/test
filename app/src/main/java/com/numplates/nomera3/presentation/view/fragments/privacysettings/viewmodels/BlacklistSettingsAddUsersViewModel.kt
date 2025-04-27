package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.usersettings.domain.usecase.blacklist.AddBlacklistExclusionUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.blacklist.DeleteBlacklistExclusionUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.blacklist.SearchBlacklistExclusionUseCase
import javax.inject.Inject

class BlacklistSettingsAddUsersViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var searchBlacklistExclusionUseCase: SearchBlacklistExclusionUseCase

    @Inject
    lateinit var addBlacklistExclusionUseCase: AddBlacklistExclusionUseCase

    @Inject
    lateinit var deleteBlacklistExclusionUseCase: DeleteBlacklistExclusionUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchBlacklistExclusionUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchBlacklistExclusionUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addBlacklistExclusionUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteBlacklistExclusionUseCase.invoke(userIds)
    }
}

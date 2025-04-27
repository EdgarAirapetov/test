package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.AddCallBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.DeleteCallBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.SearchCallBlacklistUseCase
import javax.inject.Inject

class CallSettingsAddBlacklistViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var searchCallBlacklistUseCase: SearchCallBlacklistUseCase

    @Inject
    lateinit var addCallBlacklistUseCase: AddCallBlacklistUseCase

    @Inject
    lateinit var deleteCallBlacklistUseCase: DeleteCallBlacklistUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchCallBlacklistUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchCallBlacklistUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addCallBlacklistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteCallBlacklistUseCase.invoke(userIds)
    }
}

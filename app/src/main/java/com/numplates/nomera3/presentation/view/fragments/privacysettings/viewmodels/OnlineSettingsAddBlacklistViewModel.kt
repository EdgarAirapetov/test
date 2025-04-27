package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.core.extensions.empty
import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.AddOnlineBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.DeleteOnlineBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.SearchOnlineBlacklistUseCase
import javax.inject.Inject

class OnlineSettingsAddBlacklistViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var searchOnlineBlackListUseCase: SearchOnlineBlacklistUseCase

    @Inject
    lateinit var addOnlineBlacklistUseCase: AddOnlineBlacklistUseCase

    @Inject
    lateinit var deleteOnlineBlacklistUseCase: DeleteOnlineBlacklistUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchOnlineBlackListUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchOnlineBlackListUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addOnlineBlacklistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteOnlineBlacklistUseCase.invoke(userIds)
    }
}


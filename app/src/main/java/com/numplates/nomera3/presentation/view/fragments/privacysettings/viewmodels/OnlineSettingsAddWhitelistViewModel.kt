package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.core.extensions.empty
import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.AddOnlineWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.DeleteOnlineWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.online.SearchOnlineWhitelistUseCase
import javax.inject.Inject

class OnlineSettingsAddWhitelistViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var searchOnlineWhiteListUseCase: SearchOnlineWhitelistUseCase

    @Inject
    lateinit var addOnlineWhitelistUseCase: AddOnlineWhitelistUseCase

    @Inject
    lateinit var deleteOnlineWhitelistUseCase: DeleteOnlineWhitelistUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchOnlineWhiteListUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchOnlineWhiteListUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addOnlineWhitelistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteOnlineWhitelistUseCase.invoke(userIds)
    }
}


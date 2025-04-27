package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.AddCallWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.DeleteCallWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.call.SearchCallWhitelistUseCase
import javax.inject.Inject

class CallSettingsAddWhitelistViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var searchCallWhitelistUseCase: SearchCallWhitelistUseCase

    @Inject
    lateinit var addCallWhitelistUseCase: AddCallWhitelistUseCase

    @Inject
    lateinit var deleteCallWhitelistUseCase: DeleteCallWhitelistUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchCallWhitelistUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchCallWhitelistUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addCallWhitelistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteCallWhitelistUseCase.invoke(userIds)
    }
}

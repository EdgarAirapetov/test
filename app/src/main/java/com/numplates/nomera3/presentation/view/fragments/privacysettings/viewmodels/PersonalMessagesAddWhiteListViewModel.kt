package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.AddPrivateMessageWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.DeletePrivateMessageWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.SearchPrivateMessageWhitelistUseCase
import javax.inject.Inject

class PersonalMessagesAddWhiteListViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var searchPrivateMessageWhitelistUseCase: SearchPrivateMessageWhitelistUseCase

    @Inject
    lateinit var addPrivateMessageWhitelistUseCase: AddPrivateMessageWhitelistUseCase

    @Inject
    lateinit var deletePrivateMessageWhitelist: DeletePrivateMessageWhitelistUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchPrivateMessageWhitelistUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchPrivateMessageWhitelistUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addPrivateMessageWhitelistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deletePrivateMessageWhitelist.invoke(userIds)
    }
}


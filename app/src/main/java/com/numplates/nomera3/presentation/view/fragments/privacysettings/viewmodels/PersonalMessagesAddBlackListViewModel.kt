package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.AddPrivateMessageBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.DeletePrivateMessageBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.SearchPrivateMessageBlacklistUseCase
import javax.inject.Inject

class PersonalMessagesAddBlackListViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var searchPrivateMessageBlacklistUseCase: SearchPrivateMessageBlacklistUseCase

    @Inject
    lateinit var addPrivateMessageBlacklistUseCase: AddPrivateMessageBlacklistUseCase

    @Inject
    lateinit var deletePrivateMessageBlacklistUseCase: DeletePrivateMessageBlacklistUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchPrivateMessageBlacklistUseCase.invoke(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(
        text: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return searchPrivateMessageBlacklistUseCase.invoke(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addPrivateMessageBlacklistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deletePrivateMessageBlacklistUseCase.invoke(userIds)
    }
}


package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.AddPrivateMessageWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.DeletePrivateMessageWhitelistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.GetPrivateMessageWhitelistWithCounterUseCase
import javax.inject.Inject

class PersonalMessagesWhiteListViewModel @Inject constructor(
    private val getPrivateMessageWhitelistWithCounterUseCase: GetPrivateMessageWhitelistWithCounterUseCase,
    private val addPrivateMessageWhitelistUseCase: AddPrivateMessageWhitelistUseCase,
    private val deletePrivateMessageWhitelistUseCase: DeletePrivateMessageWhitelistUseCase,
    getSettingsUseCase: GetSettingsUseCase

) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getPrivateMessageWhitelistWithCounterUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addPrivateMessageWhitelistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deletePrivateMessageWhitelistUseCase.invoke(userIds)
    }
}

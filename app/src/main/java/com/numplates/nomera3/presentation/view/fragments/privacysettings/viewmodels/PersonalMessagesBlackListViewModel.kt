package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.AddPrivateMessageBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.DeletePrivateMessageBlacklistUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage.GetPrivateMessageBlackListWithCounterUseCase
import javax.inject.Inject

class PersonalMessagesBlackListViewModel @Inject constructor(
    private val getPrivateMessageBlacklistWithCounterUseCase: GetPrivateMessageBlackListWithCounterUseCase,
    private val addPrivateMessageBlacklistUseCase: AddPrivateMessageBlacklistUseCase,
    private val deletePrivateMessageBlacklistUseCase: DeletePrivateMessageBlacklistUseCase,
    private val getSettingsUseCase: GetSettingsUseCase
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getPrivateMessageBlacklistWithCounterUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addPrivateMessageBlacklistUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deletePrivateMessageBlacklistUseCase.invoke(userIds)
    }
}

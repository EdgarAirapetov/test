package com.numplates.nomera3.presentation.view.fragments.notificationsettings.message

import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import com.meera.core.extensions.empty
import javax.inject.Inject

class MessageNotificationsAddUsersViewModel : BaseSettingsUserSearchViewModel() {

    @Inject
    lateinit var userExclusionsUseCase: MessageNotificationsExclusionsUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun getNonSearchUsersModeRequest(text: String, limit: Int, offset: Int):
        ResponseWrapper<UsersWrapper<UserSimple>> {
        return userExclusionsUseCase.searchMessageSettingsExclusion(String.empty(), limit, offset)
    }

    override suspend fun getSearchUsersModeRequest(text: String, limit: Int, offset: Int):
        ResponseWrapper<UsersWrapper<UserSimple>> {
        return userExclusionsUseCase.searchMessageSettingsExclusion(text, limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        userExclusionsUseCase.addMessageSettingsExclusion(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        userExclusionsUseCase.deleteMessageSettingsExclusion(userIds)
    }
}

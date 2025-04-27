package com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserListViewModel
import javax.inject.Inject


class SubscriptionsNotificationUsersViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val useCase: SubscriptionNotificationsUseCase
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(limit: Int, offset: Int):
        ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return useCase.getUsers(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        useCase.addUsers(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        useCase.deleteUsers(userIds)
    }
}

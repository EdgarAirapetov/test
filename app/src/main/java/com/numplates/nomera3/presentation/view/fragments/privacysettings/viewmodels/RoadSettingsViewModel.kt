package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.post.AddShowPostsExclusionsListUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.post.DeleteShowPostsExclusionsListUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.post.GetShowPostsExclusionsListUseCase
import javax.inject.Inject


class RoadSettingsViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val getShowPostsExclusionsListUseCase: GetShowPostsExclusionsListUseCase,
    private val addShowPostsExclusionsListUseCase: AddShowPostsExclusionsListUseCase,
    private val deleteShowPostExclusionsListUseCase: DeleteShowPostsExclusionsListUseCase,
) : BaseSettingsUserListViewModel(getSettingsUseCase) {

    override suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return getShowPostsExclusionsListUseCase.invoke(limit, offset)
    }

    override suspend fun addUsersRequest(userIds: List<Long>) {
        addShowPostsExclusionsListUseCase.invoke(userIds)
    }

    override suspend fun deleteUsersRequest(userIds: List<Long>) {
        deleteShowPostExclusionsListUseCase.invoke(userIds)
    }
}

package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import com.numplates.nomera3.modules.user.domain.mapper.toUserPermissions
import com.numplates.nomera3.modules.user.ui.entity.UserPermissions
import javax.inject.Inject


class UserPermissionsUseCase @Inject constructor(
    private val repository: UserRepository
) : BaseUseCaseCoroutine<UserPermissionParams, UserPermissions> {
    override suspend fun execute(
        params: UserPermissionParams,
        success: (UserPermissions) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.requestUserPermissions(
            success = { permission ->
                success(permission.toUserPermissions())
            },
            fail = fail
        )
    }


}

class UserPermissionParams : DefParams()

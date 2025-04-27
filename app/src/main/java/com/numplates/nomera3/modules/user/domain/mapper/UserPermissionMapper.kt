package com.numplates.nomera3.modules.user.domain.mapper

import com.numplates.nomera3.modules.user.data.entity.UserPermissionResponse
import com.numplates.nomera3.modules.user.ui.entity.CommunitiesPermissions
import com.numplates.nomera3.modules.user.ui.entity.MainRoadPermissions
import com.numplates.nomera3.modules.user.ui.entity.UserBlockInfo
import com.numplates.nomera3.modules.user.ui.entity.UserPermissions

private const val CREATE_POST = "create_post"

fun UserPermissionResponse.toUserPermissions(): UserPermissions {

    val canCreatePostInMainRoad = !permissions?.mainRoad?.find { it == CREATE_POST }.isNullOrEmpty()
    val canCreatePostInPrivateCommunity = !permissions?.communities?.closed?.find { it == CREATE_POST }.isNullOrEmpty()
    val canCreatePostInOpenCommunity = !permissions?.communities?.open?.find { it == CREATE_POST }.isNullOrEmpty()
    val blockInfo = UserBlockInfo(
        blockReasonId = userBlockInfo?.blockReasonId,
        type = userBlockInfo?.type,
        blockedUntil = userBlockInfo?.blockedUntil,
        blockReasonText = userBlockInfo?.blockReasonValue
    )
    return UserPermissions(
        MainRoadPermissions(canCreatePostInMainRoad),
        CommunitiesPermissions(
            canCreatePostInOpenCommunity = canCreatePostInOpenCommunity,
            canCreatePostInPrivateCommunity = canCreatePostInPrivateCommunity
        ),
        blockInfo
    )
}
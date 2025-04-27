package com.numplates.nomera3.modules.moments.show.data

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper

interface MomentSettingsRepository {
    suspend fun addMomentHideFromExclusion(userIds: List<Long>): ResponseWrapper<Any>

    suspend fun deleteMomentHideFromExclusion(params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    suspend fun searchHideFromExclusion(
        name: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    suspend fun getMomentsHideFromExclusions(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    suspend fun addMomentNotShowExclusion(userIds: List<Long>): ResponseWrapper<Any>

    suspend fun deleteMomentNotShowExclusion(params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    suspend fun searchNotShowExclusion(
        name: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    suspend fun getMomentsNotShowExclusions(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>
}

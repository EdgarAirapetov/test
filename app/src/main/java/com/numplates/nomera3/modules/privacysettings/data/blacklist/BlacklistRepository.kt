package com.numplates.nomera3.modules.privacysettings.data.blacklist

interface BlacklistRepository {
    suspend fun addToBlackList(userIdList: List<Long>): Boolean
    suspend fun deleteFromBlacklist(userIdList: List<Long>): Boolean
}

package com.numplates.nomera3.modules.privacysettings.data.blacklist

import com.meera.db.DataStore
import com.numplates.nomera3.data.network.ApiMain
import com.meera.db.dao.PrivacySettingsDao
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import javax.inject.Inject

class BlacklistRepositoryImpl @Inject constructor(
    dataStore: DataStore,
    private val api: ApiMain,
) : BlacklistRepository {

    private val settingsDao: PrivacySettingsDao = dataStore.privacySettingsDao()
    private val blackListKey: String = SettingsKeyEnum.BLACKLIST.key

    override suspend fun addToBlackList(userIdList: List<Long>): Boolean {
        val result = api.addBlacklistExclusion(userIdList)
        updateBlackListLocally(userIdList.size)
        return result.data != null
    }

    override suspend fun deleteFromBlacklist(userIdList: List<Long>): Boolean {
        val result = api.deleteBlacklistExclusion(userIdList)
        updateBlackListLocally(-1 * userIdList.size)
        return result.data != null
    }

    private suspend fun updateBlackListLocally(value: Int) {
        if (value == 0) {
            settingsDao.updateCountBlackList(blackListKey, null)
        } else {
            val setting = settingsDao.getByKey(blackListKey)
            val blackListCounter = (setting.countBlacklist ?: 0) + value
            settingsDao.updateCountBlackList(blackListKey, blackListCounter)
        }
    }
}

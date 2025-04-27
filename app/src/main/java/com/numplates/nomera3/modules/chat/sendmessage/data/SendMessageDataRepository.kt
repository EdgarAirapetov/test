package com.numplates.nomera3.modules.chat.sendmessage.data

import com.google.gson.Gson
import com.meera.db.DataStore
import com.meera.db.models.message.SendMessageDataDbModel
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageModel
import timber.log.Timber
import javax.inject.Inject

interface SendMessageDataRepository {
    suspend fun storeMessageData(messageData: SendMessageModel): Long
    suspend fun retrieveMessageData(key: Long): SendMessageModel?
    suspend fun deleteMessageData(key: Long)
}

class SendMessageDataRepositoryImpl @Inject constructor(
    private val dataStore: DataStore,
    private val gson: Gson
): SendMessageDataRepository {

    override suspend fun storeMessageData(messageData: SendMessageModel): Long {
        val jsonifiedData = gson.toJson(messageData)
        val dbModel = SendMessageDataDbModel(dataAsJson = jsonifiedData)
        return dataStore.sendMessageDataDao().insert(dbModel)
    }

    override suspend fun retrieveMessageData(key: Long): SendMessageModel? {
        return try {
            val dbModel = dataStore.sendMessageDataDao().getDataByKey(key)
            gson.fromJson(dbModel?.dataAsJson, SendMessageModel::class.java)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override suspend fun deleteMessageData(key: Long) = dataStore.sendMessageDataDao().deleteByKey(key)
}

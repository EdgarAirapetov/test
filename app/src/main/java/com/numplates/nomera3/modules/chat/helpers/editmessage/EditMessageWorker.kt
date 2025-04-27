package com.numplates.nomera3.modules.chat.helpers.editmessage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.meera.db.DataStore
import com.meera.db.dao.EditMessageDataDao
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.chat.helpers.editmessage.models.EditMessageModel
import com.numplates.nomera3.modules.chat.helpers.editmessage.models.EditMessageWorkResultKey
import timber.log.Timber
import javax.inject.Inject

const val EDIT_MESSAGE_WORKER_INPUT_DATA_ID = "edit_message_input_data_id"
private const val INVALID_EDIT_MESSAGE_DATA_ID = -1L

class EditMessageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), EditMessageInteractionCallback {

    @Inject
    lateinit var editManager: EditMessageManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var dataStore: DataStore

    private val editMessageDao: EditMessageDataDao
        get() = dataStore.editMessageDataDao()

    init {
        App.component.inject(this)
        editManager.addInteractionCallback(this)
    }

    override suspend fun doWork(): Result {
        val dataKey = inputData.getLong(EDIT_MESSAGE_WORKER_INPUT_DATA_ID, INVALID_EDIT_MESSAGE_DATA_ID)
        return try {
            if (dataKey == INVALID_EDIT_MESSAGE_DATA_ID) throw Exception("Invalid input data key")
            val sendData = editMessageDao.getDataByKey(dataKey)
                ?: throw Exception("Could not retrieve input data")
            val parsedData = gson.fromJson(sendData.dataAsJson, EditMessageModel::class.java)
            editManager.editMessage(parsedData)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        } finally {
            runCatching { editMessageDao.deleteByKey(dataKey) }
                .onFailure { Timber.e(it) }
        }
    }

    override suspend fun onShowLoadingProgress(messageId: String) {
        setProgress(workDataOf(
            EditMessageWorkResultKey.SHOW_MEDIA_PROGRESS.key to messageId
        ))
    }

    override suspend fun onHideLoadingProgress(messageId: String) {
        setProgress(workDataOf(
            EditMessageWorkResultKey.HIDE_MEDIA_PROGRESS.key to messageId
        ))
    }
}

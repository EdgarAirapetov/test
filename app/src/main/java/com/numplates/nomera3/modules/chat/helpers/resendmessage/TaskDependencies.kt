package com.numplates.nomera3.modules.chat.helpers.resendmessage

import android.content.Context
import com.google.gson.Gson
import com.meera.db.DataStore
import com.numplates.nomera3.domain.interactornew.SendNewMessageUseCase
import com.numplates.nomera3.domain.interactornew.SendVoiceMessageUseCase
import com.numplates.nomera3.modules.chat.helpers.UploadChatHelper
import com.numplates.nomera3.modules.fileuploads.domain.usecase.PartialUploadChatVideoUseCase


data class TaskDependencies(
    val appContext: Context,
    val newMessageUseCase: SendNewMessageUseCase,
    val dataStore: DataStore,
    val partialUploadChatVideoUseCase: PartialUploadChatVideoUseCase,
    val chatUploadHelper: UploadChatHelper,
    val sendVoiceMessageUseCase: SendVoiceMessageUseCase,
    val gson: Gson
)

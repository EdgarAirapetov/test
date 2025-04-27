package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.VoiceMessage
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.meera.core.extensions.empty
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class SendVoiceMessageUseCase(private val repository: ApiMain?,
                              private var pathAudio: String = String.empty(),
                              private var listOfAmplitudes: List<Int>? = mutableListOf<Int>()) {

    fun setParams(pathAudio: String, listOfAmplitudes: List<Int>?) {
        this.pathAudio = pathAudio
        this.listOfAmplitudes = listOfAmplitudes
    }

    suspend fun sendVoiceMessage(): ResponseWrapper<VoiceMessage>? {
        val audioFile = File(pathAudio)
        val audioRequestFile = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
        val audio = MultipartBody.Part.createFormData("file", audioFile.name, audioRequestFile)
        return repository?.sendVoiceMessage(audio, listOfAmplitudes)
    }

}

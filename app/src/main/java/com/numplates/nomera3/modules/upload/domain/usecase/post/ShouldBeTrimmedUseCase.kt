package com.numplates.nomera3.modules.upload.domain.usecase.post

import android.net.Uri
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ShouldBeTrimmedUseCase @Inject constructor( // todo remove it?
    private val getVideoLengthUseCase: GetVideoLengthUseCase
) {
    fun execute(videoUri: Uri, maxLengthSeconds: Int): Boolean {
        return executeAndReturnCurrentLength(videoUri, maxLengthSeconds).second
    }

    fun execute(videoSource: String, maxLengthSeconds: Int): Boolean {
        val videoUri = getUri(videoSource)

        return execute(videoUri, maxLengthSeconds)
    }

    fun executeAndReturnCurrentLength(videoUri: Uri, maxLengthSeconds: Int): Pair<Int, Boolean> {
        val currentLength = getVideoLengthUseCase.execute(videoUri).toInt()
        val shouldBeTrimmed = currentLength >= maxLengthSeconds

        return currentLength to shouldBeTrimmed
    }

    private fun getUri(path: String): Uri {
        return try {
            Uri.fromFile(File(path))
        } catch (ex: Exception) {
            Timber.e(ex)
            Uri.parse(path)
        }
    }
}

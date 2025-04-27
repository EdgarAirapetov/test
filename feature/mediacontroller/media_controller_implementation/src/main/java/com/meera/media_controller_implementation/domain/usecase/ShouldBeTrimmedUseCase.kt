package com.meera.media_controller_implementation.domain.usecase

import android.net.Uri
import java.io.File
import javax.inject.Inject

internal class ShouldBeTrimmedUseCase @Inject constructor(
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
            Uri.parse(path)
        }
    }
}

package com.numplates.nomera3.modules.chat.domain.usecases

import android.net.Uri
import com.meera.core.utils.files.IMAGE_GIF
import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetImageFileForKeyboardContentUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {
    suspend fun invoke(media: Uri, label: String) =
        withContext(Dispatchers.IO) {
            runCatching {
                repository.getFileForContentUri(media, label)?.let { nonNullFile ->
                    val path = Uri.fromFile(nonNullFile).toString()
                    if (label == IMAGE_GIF) {
                        return@withContext SuccessGif(path)
                    } else {
                        return@withContext SuccessImage(path)
                    }
                }
            }.onFailure { Timber.e(it) }
            return@withContext UnknownFail
        }
}


sealed interface KeyboardContentResultModel
class SuccessImage(val imagePath: String) : KeyboardContentResultModel
class SuccessGif(val gifPath: String) : KeyboardContentResultModel
object UnknownFail : KeyboardContentResultModel

package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickerModel
import javax.inject.Inject

private const val EMOJI_GREETING = "👋"

class GetGreetingStickerUseCase @Inject constructor(
    private val repository: MediakeyboardRepository
) {

    suspend fun invoke(): MediaKeyboardStickerModel? {
        val stickerPacks = repository.getStickers().stickerPacks
        val greetingStickers = stickerPacks.flatMap { it.stickers }.filter { it.emoji.contains(EMOJI_GREETING) }
        return greetingStickers.randomOrNull()
    }

}

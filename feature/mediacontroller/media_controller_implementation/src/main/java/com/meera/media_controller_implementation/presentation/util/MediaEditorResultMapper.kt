package com.meera.media_controller_implementation.presentation.util

import com.meera.media_controller_common.MediaEditorResult
import com.meera.media_controller_common.MediaKeyboard
import com.noomeera.nmrmediatools.NMRResult
import com.noomeera.nmrmediatools.NMRStoryGifObject
import com.noomeera.nmrmediatools.NMRStoryStickerObject

internal class MediaEditorResultMapper {
    fun map(result: NMRResult) = MediaEditorResult(
        uri = result.uri,
        isVideo = result.isVideo,
        media = result.media,
        mediaKeyboard = result.mediaKeyboard?.mapNotNull {
            when (it) {
                is NMRStoryStickerObject -> MediaKeyboard(it.stickerId)
                is NMRStoryGifObject -> MediaKeyboard(
                    gifId = it.gifId,
                    preview = it.preview,
                    ratio = it.ratio,
                    url = it.url
                )

                else -> null
            }
        }
    )
}

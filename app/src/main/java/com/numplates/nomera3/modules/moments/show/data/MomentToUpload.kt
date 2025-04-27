package com.numplates.nomera3.modules.moments.show.data

import com.meera.media_controller_common.MediaEditorResult
import com.meera.media_controller_common.MediaKeyboard
import java.io.Serializable

data class MomentToUpload(
    val file: String,
    val isVideo: Boolean,
    val position: MomentGpsPosition,
    val media: String,
    val mediaKeyboard: List<MediaKeyboard>?
) : Serializable

data class MomentGpsPosition(val x: Double, val y: Double) : Serializable

fun List<MediaEditorResult>.mapToMoments(position: MomentGpsPosition): List<MomentToUpload> {
    return this.map { moment ->
        val path = moment.uri?.path ?: error("Ошибка. Невозожно определить путь до Момента.")
        MomentToUpload(
            file = path,
            isVideo = moment.isVideo,
            position = position,
            media = moment.media ?: "",
            mediaKeyboard = moment.mediaKeyboard
        )
    }
}

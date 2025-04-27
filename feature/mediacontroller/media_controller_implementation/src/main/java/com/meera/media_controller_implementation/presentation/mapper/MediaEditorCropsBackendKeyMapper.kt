package com.meera.media_controller_implementation.presentation.mapper

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.media_controller_api.model.MediaControllerCropBackendKey
import com.meera.media_controller_common.CropInfo
import com.meera.media_controller_common.MediaControllerOpenPlace

class MediaEditorCropsBackendKeyMapper {

    private var videoCropInfo: List<CropInfo> = emptyList()

    fun setVideoCrops(videoCropInfo: List<CropInfo>) {
        this.videoCropInfo = videoCropInfo
    }

    fun map(mediaPlace: MediaControllerOpenPlace): MediaControllerCropBackendKey {
        return when (mediaPlace) {
            MediaControllerOpenPlace.Post -> MediaControllerCropBackendKey.Post
            MediaControllerOpenPlace.Avatar -> MediaControllerCropBackendKey.AnyAvatar
            MediaControllerOpenPlace.Community -> MediaControllerCropBackendKey.AnyAvatar
            MediaControllerOpenPlace.Chat -> MediaControllerCropBackendKey.Chat
            MediaControllerOpenPlace.Gallery -> MediaControllerCropBackendKey.Gallery
            MediaControllerOpenPlace.Profile -> MediaControllerCropBackendKey.Profile
            MediaControllerOpenPlace.VideoPost -> MediaControllerCropBackendKey.Post
            MediaControllerOpenPlace.EventPost -> MediaControllerCropBackendKey.EventPost
            MediaControllerOpenPlace.Moments -> MediaControllerCropBackendKey.Moments
            MediaControllerOpenPlace.Common -> MediaControllerCropBackendKey.Common
            MediaControllerOpenPlace.CreatePost -> MediaControllerCropBackendKey.Post
            MediaControllerOpenPlace.CreatePostVideoPreview -> MediaControllerCropBackendKey.Post
        }
    }

    fun mapToMaxVideoLengthSec(mediaPlace: MediaControllerOpenPlace): Int {
        return when (mediaPlace) {
            MediaControllerOpenPlace.Post,
            MediaControllerOpenPlace.Common,
            MediaControllerOpenPlace.Profile,
            MediaControllerOpenPlace.VideoPost,
            MediaControllerOpenPlace.Chat,
            MediaControllerOpenPlace.EventPost,
            MediaControllerOpenPlace.CreatePost,
            MediaControllerOpenPlace.CreatePostVideoPreview,
            MediaControllerOpenPlace.Moments-> {
                getMaxLengthSec(mediaPlace)
            }

            MediaControllerOpenPlace.Gallery,
            MediaControllerOpenPlace.Avatar,
            MediaControllerOpenPlace.Community -> {
                FirebaseCrashlytics.getInstance().recordException(
                    IllegalStateException("Ошибка. Попытка получения максимальной длинны видео для точки входа в Видео-редактор где видео не предусматривается")
                )
                Int.MAX_VALUE - 1
            }
        }
    }

    fun mapToMaxVideoLengthMs(mediaPlace: MediaControllerOpenPlace): Long {
        return mapToMaxVideoLengthSec(mediaPlace) * 1000L
    }


    private fun getMaxLengthSec(openPlace: MediaControllerOpenPlace): Int {
        val mediaControllerCropBackendKey = map(openPlace)

        return videoCropInfo.find { it.name == mediaControllerCropBackendKey.name }?.maxVideoDurationSec
            ?: DEFAULT_VIDEO_LENGTH_SEC
    }

    companion object {
        const val DEFAULT_VIDEO_LENGTH_SEC = 60
    }
}

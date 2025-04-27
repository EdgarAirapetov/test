package com.meera.media_controller_common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class MediaControllerOpenPlace : Parcelable {
    object Gallery : MediaControllerOpenPlace()
    object Avatar : MediaControllerOpenPlace()
    object Community : MediaControllerOpenPlace()
    object Chat : MediaControllerOpenPlace()
    object Post : MediaControllerOpenPlace()
    object Profile : MediaControllerOpenPlace()
    object Common : MediaControllerOpenPlace()
    object CreatePost : MediaControllerOpenPlace()
    object CreatePostVideoPreview : MediaControllerOpenPlace()
    object VideoPost : MediaControllerOpenPlace()
    object EventPost : MediaControllerOpenPlace()
    object Moments : MediaControllerOpenPlace()
}

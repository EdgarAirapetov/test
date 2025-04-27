package com.numplates.nomera3.modules.feed.ui.data

import android.os.Parcelable
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoadingPostVideoInfoUIModel(
    val loadingState: MediaLoadingState = MediaLoadingState.NONE,
    val loadingTime: Long = 0L,
    val isShowLoadingProgress: Boolean = false
) : Parcelable

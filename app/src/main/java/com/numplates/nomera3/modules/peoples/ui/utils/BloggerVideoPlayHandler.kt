package com.numplates.nomera3.modules.peoples.ui.utils

import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.google.android.exoplayer2.ui.PlayerView

interface BloggerVideoPlayHandler {

    fun getPlayerView(): PlayerView

    fun getVideoUrlString(): String?

    fun getThumbnail() : ImageView?

    fun getStaticDurationView() : View

    fun getRoot() : CardView?

    fun getItemView(): View
}

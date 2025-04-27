package com.numplates.nomera3.modules.chat.views

import android.widget.ImageView


fun ImageView.scaleAlphaAnimateVoiceBtn(
    alpha: Float,
    scale: Float,
    duration: Long,
    onComplete: () -> Unit = {}
) {
    this.animate()
        ?.alpha(alpha)
        ?.scaleX(scale)
        ?.scaleY(scale)
        ?.withEndAction { onComplete.invoke() }
        ?.duration = duration
}
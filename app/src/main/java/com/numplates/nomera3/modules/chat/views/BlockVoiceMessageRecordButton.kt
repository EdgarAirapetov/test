package com.numplates.nomera3.modules.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.numplates.nomera3.R
import com.meera.core.extensions.visible

private const val ANIMATION_DURATION = 200L


/**
 * Вьюха кнопки блокировки записи голосового сообщения
 */
class BlockVoiceMessageRecordButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var btnLockRecord: FrameLayout? = null
    private var ivLock: ImageView? = null

    init {
        View.inflate(context, R.layout.block_voice_message_record_button, this)
        btnLockRecord = findViewById(R.id.btn_lock_record)
        ivLock = findViewById(R.id.iv_lock)

        btnLockRecord?.alpha = 0.0f
        ivLock?.alpha = 0.0f
        ivLock?.scaleX = 0.7f
        ivLock?.scaleY = 0.7f
    }

    fun startAppearAnimation() {
        this.visible()
        btnLockRecord?.animateHeight(48.dp, ANIMATION_DURATION)
        btnLockRecord?.animate()
            ?.translationY(-dpToPx(16).toFloat())
            ?.alpha(1.0f)
            ?.duration = ANIMATION_DURATION

        ivLock?.scaleAlphaAnimateVoiceBtn(1.0f, 1.0f, ANIMATION_DURATION)
    }

    fun setLockMode() {
        ivLock?.scaleAlphaAnimateVoiceBtn(0.5f, 0.5f, ANIMATION_DURATION) {
            ivLock?.setImageResource(R.drawable.ic_stop_voice_record)
            ivLock?.scaleAlphaAnimateVoiceBtn(1.0f, 1.0f, ANIMATION_DURATION)
        }
    }

    fun clearLockMode() {
        this.gone()
        ivLock?.setImageResource(R.drawable.ic_lock_voice_record)
        btnLockRecord?.animateHeight(24.dp, ANIMATION_DURATION)
        btnLockRecord?.animate()
            ?.translationY(dpToPx(16).toFloat())
            ?.alpha(0.0f)
            ?.duration = 10
    }

}
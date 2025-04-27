package com.numplates.nomera3.modules.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R

private const val ANIMATION_DURATION = 200L

class MeeraBlockVoiceMessageRecordButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var btnLockRecord: FrameLayout? = null
    private var ivLock: ImageView? = null
    private var ivLockArrow: ImageView? = null

    init {
        View.inflate(context, R.layout.meera_block_voice_message_record_button, this)
        btnLockRecord = findViewById(R.id.btn_lock_record)
        ivLock = findViewById(R.id.iv_lock)
        ivLockArrow = findViewById(R.id.iv_lock_record_arrow)

        clipChildren = false
    }

    fun startAppearAnimation() {
        this.visible()
        ivLockArrow?.visible()
        this.animate()
            ?.translationY(-dpToPx(48).toFloat())
            ?.alpha(1.0f)
            ?.duration = ANIMATION_DURATION
    }

    fun setLockMode() {
        ivLockArrow?.gone()
        ivLock?.setImageResource(R.drawable.ic_meera_stop_voice_record)
    }

    fun clearLockMode() {
        this.gone()
        ivLockArrow?.visible()
        ivLock?.setImageResource(R.drawable.ic_outlined_lock_l)
        this.animate()
            ?.translationY(dpToPx(16).toFloat())
            ?.alpha(0.0f)
            ?.duration = ANIMATION_DURATION
    }
}

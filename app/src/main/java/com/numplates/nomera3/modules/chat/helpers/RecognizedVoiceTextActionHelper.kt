package com.numplates.nomera3.modules.chat.helpers

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.expandTouchArea
import com.meera.core.extensions.gone
import com.meera.core.extensions.setImageDrawable
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.utils.layouts.ExpandableLayout
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.utils.TEXT_SPACE_DEFAULT_SEND_24H

private const val BTN_EXPAND_TOUCH_OFFSET = 8
private const val BTN_EXPAND_MOVE_ANIMATION_OFFSET = 8
private const val BTN_EXPAND_DEFAULT_OFFSET = 0
private const val BTN_EXPAND_MARGIN_TOP = 16
private const val BTN_COLLAPSE_MARGIN_TOP = 8
private const val META_ERROR_MSG = "ERROR"

private val MSG_TYPE_1_WIDTH = Pair(0..11, 136)
private val MSG_TYPE_2_WIDTH = Pair(11..19, 180)
private const val MSG_TYPE_3_WIDTH = 240

private const val BTN_EXPAND_START_ANIM_STATE = 0.0f
private const val BTN_EXPAND_SCALE_STEP_1 = 1.1f
private const val BTN_EXPAND_ALPHA_STEP_1 = 0.7f
private const val BTN_EXPAND_SCALE_STEP_2 = 1.0f
private const val BTN_EXPAND_ALPHA_STEP_2 = 1.0f
private const val BTN_EXPAND_ANIM_DURATION_STEP_1 = 200L
private const val BTN_EXPAND_ANIM_DURATION_STEP_2 = 100L


class RecognizedVoiceTextActionHelper {

    private var isRecognizedTextExpanded = false

    fun handleAudioRecognizedTextExpandLayout(
        isSend: Boolean,
        message: MessageEntity,
        textView: TextView,
        btnExpand: ImageView,
        container: ExpandableLayout,
        isForwardMessage: Boolean,
        isExpandAction: (isExpanded: Boolean) -> Unit,
        onBtnAnimationComplete: () -> Unit
    ) {
        val (expandIconRes, collapseIconRes) = if (isSend) {
            R.drawable.ic_expand_recognized_text_send to R.drawable.ic_collapse_recognized_text_send
        } else {
            R.drawable.ic_expand_recognized_text_receive to R.drawable.ic_collapse_recognized_text_receive
        }

        val waveForm = message.attachment.waveForm
        var recognizedText = message.attachment.audioRecognizedText
        if (recognizedText.isNotEmpty() && recognizedText != META_ERROR_MSG) {
            val isExpandedText = message.isExpandedRecognizedText ?: false
            isRecognizedTextExpanded = isExpandedText

            expandButtonAnimationHandler(message, btnExpand, onBtnAnimationComplete)

            if (isExpandedText) {
                btnExpand.setImageDrawable(collapseIconRes)
                btnExpand.setMargins(top = BTN_EXPAND_MARGIN_TOP.dp)
            } else {
                btnExpand.setImageDrawable(expandIconRes)
                btnExpand.setMargins(top = BTN_COLLAPSE_MARGIN_TOP.dp)
            }

            val containerWidth = when (waveForm.size) {
                in MSG_TYPE_1_WIDTH.first -> MSG_TYPE_1_WIDTH.second.dp
                in MSG_TYPE_2_WIDTH.first -> MSG_TYPE_2_WIDTH.second.dp
                else -> MSG_TYPE_3_WIDTH.dp
            }

            if (!isForwardMessage) textView.width = containerWidth
            recognizedText += TEXT_SPACE_DEFAULT_SEND_24H
            textView.text = recognizedText

            if (isExpandedText) {
                container.expand(false)
                btnExpand.translationY = dpToPx(BTN_EXPAND_MOVE_ANIMATION_OFFSET).toFloat()
            } else {
                container.collapse(false)
                btnExpand.translationY = BTN_EXPAND_DEFAULT_OFFSET.toFloat()
            }

            container.setOnExpansionUpdateListener { _, state ->
                when (state) {
                    ExpandableLayout.State.EXPANDED -> btnExpand.setImageDrawable(collapseIconRes)
                    ExpandableLayout.State.EXPANDING -> btnExpand.setImageDrawable(collapseIconRes)
                    ExpandableLayout.State.COLLAPSING -> btnExpand.setImageDrawable(expandIconRes)
                    ExpandableLayout.State.COLLAPSED -> btnExpand.setImageDrawable(expandIconRes)
                }
            }

            btnExpand.setOnClickListener {
                if (container.state == ExpandableLayout.State.EXPANDING ||
                    container.state == ExpandableLayout.State.COLLAPSING
                ) {
                    return@setOnClickListener
                }
                isRecognizedTextExpanded = if (isRecognizedTextExpanded) {
                    container.collapse()
                    btnExpand.clearAnimation()
                    btnExpand.animate()
                        .translationY(BTN_EXPAND_DEFAULT_OFFSET.toFloat())
                        .setDuration(container.duration.toLong())
                        .setListener(null)
                        .withEndAction { isExpandAction.invoke(false) }
                    false
                } else {
                    container.expand()
                    btnExpand.clearAnimation()
                    btnExpand.animate()
                        .translationYBy(dpToPx(BTN_EXPAND_MOVE_ANIMATION_OFFSET).toFloat())
                        .setDuration(container.duration.toLong())
                        .setListener(null)
                        .withEndAction { isExpandAction.invoke(true) }
                    true
                }
            }
        } else {
            btnExpand.gone()
            container.gone()
        }
    }

    private fun expandButtonAnimationHandler(
        message: MessageEntity,
        btnExpand: ImageView,
        onBtnAnimationComplete: () -> Unit
    ) {
        btnExpand.expandTouchArea(BTN_EXPAND_TOUCH_OFFSET)
        if (message.isExpandedRecognizedText == null) {
            if (btnExpand.isVisible.not()) btnExpand.visibleAppearScaleAnimate()
            onBtnAnimationComplete.invoke()
        } else {
            btnExpand.visible()
        }
    }
}

fun View.visibleAppearScaleAnimate() {
    this.scaleX = BTN_EXPAND_START_ANIM_STATE
    this.scaleY = BTN_EXPAND_START_ANIM_STATE
    this.alpha = BTN_EXPAND_START_ANIM_STATE
    this.visible()
    this.clearAnimation()
    this.animate()
        .scaleX(BTN_EXPAND_SCALE_STEP_1)
        .scaleY(BTN_EXPAND_SCALE_STEP_1)
        .alpha(BTN_EXPAND_ALPHA_STEP_1)
        .withEndAction {
            this@visibleAppearScaleAnimate.animate()
                .scaleX(BTN_EXPAND_SCALE_STEP_2)
                .scaleY(BTN_EXPAND_SCALE_STEP_2)
                .alpha(BTN_EXPAND_ALPHA_STEP_2)
                .duration = BTN_EXPAND_ANIM_DURATION_STEP_2
        }
        .duration = BTN_EXPAND_ANIM_DURATION_STEP_1
}

@Deprecated("Unused at this time! Used in deprecated ChatPagedListBoundaryCallback")
object ExpandBtnVoiceMessageStorage {
    val messages = mutableSetOf<String>()
}

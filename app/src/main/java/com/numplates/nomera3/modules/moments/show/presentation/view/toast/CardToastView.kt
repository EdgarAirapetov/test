package com.numplates.nomera3.modules.moments.show.presentation.view.toast

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.animation.doOnEnd
import androidx.core.view.children
import com.numplates.nomera3.databinding.LayoutCardToastViewBinding

private const val SHOW_DURATION_MS = 500L
private const val HIDE_DURATION_MS = 3000L
private const val ALPHA_PROPERTY = "alpha"

class CardToastView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private val binding =
        LayoutCardToastViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun showToast(@StringRes text: Int) {
        binding.tvCardToastText.text = resources.getString(text)
    }

    private fun setToastLayoutParams() {
        layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
    }

    private fun startToastAnimation(
        durationMs: Long,
        isShow: Boolean,
        completeCallback: (() -> Unit)? = null
    ) {
        val startValue = if (isShow) 0f else 1f
        val endValue = if (isShow) 1f else 0f
        ObjectAnimator.ofFloat(this, ALPHA_PROPERTY, startValue, endValue).apply {
            duration = durationMs
            doOnEnd { completeCallback?.invoke() }
        }.start()
    }

    companion object {

        fun show(
            container: ViewGroup,
            @StringRes text: Int,
            showDuration: Long = SHOW_DURATION_MS,
            hideDuration: Long = HIDE_DURATION_MS
        ): CardToastView {
            val toastInViewTree = container.children.find { it is CardToastView } as? CardToastView
            container.removeView(toastInViewTree)
            val toast = CardToastView(container.context).apply {
                setToastLayoutParams()
                showToast(text)
            }
            container.addView(toast)
            toast.playAnimation(showDuration, hideDuration) { container.removeView(it) }
            return toast
        }

        private fun CardToastView.playAnimation(
            showDuration: Long,
            hideDuration: Long,
            completeCallback: ((CardToastView) -> Unit)? = null
        ) {
            startToastAnimation(durationMs = showDuration, isShow = true) {
                startToastAnimation(durationMs = hideDuration, isShow = false) {
                    completeCallback?.invoke(this)
                }
            }
        }
    }
}

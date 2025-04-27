package com.numplates.nomera3.modules.moments.show.presentation.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.R
import timber.log.Timber
import java.util.EnumMap
import kotlin.properties.Delegates

private const val LEFT_INDEX = 0
private const val TOP_INDEX = 1
private const val RIGHT_INDEX = 2
private const val BOTTOM_INDEX = 3

private enum class SubscribeButtonState {
    DEFAULT, NOT_SUBSCRIBED, SUBSCRIBED
}

class MomentSubscribeToUserView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var buttonState: SubscribeButtonState by Delegates
        .vetoable(SubscribeButtonState.DEFAULT) { _, oldValue, newValue ->
            if (oldValue == newValue) {
                Timber.d("button state was not changed;")
                false
            } else {
                Timber.d("button state changed; oldValue: $oldValue, newValue: $newValue,")
                handleButtonState(oldValue, newValue)
                true
            }
        }

    private val checkmarkDrawable =
        ContextCompat.getDrawable(context, R.drawable.ic_check_mark_active)?.apply {
            setTint(ContextCompat.getColor(context, R.color.tale_white))
            val drawableSize = dpToPx(14)
            setBounds(0, 0, drawableSize, drawableSize)
        }

    private val listeners: MutableMap<SubscribeButtonState, (() -> Unit)?> = EnumMap(SubscribeButtonState::class.java)
    private val stateChangeDelegate: StateChangeDelegate

    init {
        background = ContextCompat.getDrawable(context, R.drawable.background_moment_subscribe_status)
        compoundDrawablePadding = dpToPx(4)
        setCompoundDrawables(checkmarkDrawable, null, null, null)
        setOnClickListener { listeners[buttonState]?.invoke() }
        stateChangeDelegate = HideReadButtonDelegate(this)
    }

    fun setIsSubscribed(isSubscribedTo: Boolean) {
        buttonState = if (isSubscribedTo) {
            SubscribeButtonState.SUBSCRIBED
        } else {
            SubscribeButtonState.NOT_SUBSCRIBED
        }
    }

    fun resetButtonState() {
        buttonState = SubscribeButtonState.DEFAULT
    }

    fun setSubscribeClickListener(onSubscribeClick: () -> Unit) {
        listeners[SubscribeButtonState.NOT_SUBSCRIBED] = onSubscribeClick
    }

    fun setUnsubscribeClickListener(onUnsubscribeClick: () -> Unit) {
        listeners[SubscribeButtonState.SUBSCRIBED] = onUnsubscribeClick
    }

    private fun handleButtonState(old: SubscribeButtonState, new: SubscribeButtonState) {
        stateChangeDelegate.handleButtonState(old, new)
    }
}

/**
 * Интерфейс для делегата обновления состояния [MomentSubscribeToUserView]
 */
private interface StateChangeDelegate {
    fun handleButtonState(old: SubscribeButtonState, new: SubscribeButtonState)
}

@Suppress("unused")
private class DefaultStateChangeDelegate(val view: MomentSubscribeToUserView) : StateChangeDelegate {

    private val cd = view.compoundDrawables

    override fun handleButtonState(old: SubscribeButtonState, new: SubscribeButtonState) = with(view) {
        when (new) {
            SubscribeButtonState.DEFAULT -> {
                isVisible = false
                text = null
                setCompoundDrawables(null, null, null, null)
            }

            SubscribeButtonState.SUBSCRIBED -> {
                isVisible = false
                setText(R.string.post_read)
                setCompoundDrawables(cd[LEFT_INDEX], cd[TOP_INDEX], cd[RIGHT_INDEX], cd[BOTTOM_INDEX])
            }

            SubscribeButtonState.NOT_SUBSCRIBED -> {
                isVisible = true
                setText(R.string.post_start_follow)
                setCompoundDrawables(null, null, null, null)
            }
        }
    }
}

private class HideReadButtonDelegate(
    val view: MomentSubscribeToUserView
) : StateChangeDelegate {

    private val cd = view.compoundDrawables
    private var remainRunnable: Runnable = Runnable {}

    override fun handleButtonState(old: SubscribeButtonState, new: SubscribeButtonState): Unit = with(view) {
        handler?.removeCallbacks(remainRunnable)
        when (new) {
            SubscribeButtonState.DEFAULT -> handleDefaultState()
            SubscribeButtonState.NOT_SUBSCRIBED -> handleNotSubscribedState()
            SubscribeButtonState.SUBSCRIBED -> handleSubscribedState(old)
        }
    }

    private fun handleDefaultState() = with(view) {
        isVisible = false
        text = null
        setCompoundDrawables(null, null, null, null)
    }

    private fun handleNotSubscribedState() = with(view) {
        isVisible = true
        setText(R.string.post_start_follow)
        setCompoundDrawables(null, null, null, null)
    }

    private fun handleSubscribedState(old: SubscribeButtonState) = with(view) {
        setText(R.string.post_read)
        setCompoundDrawables(cd[LEFT_INDEX], cd[TOP_INDEX], cd[RIGHT_INDEX], cd[BOTTOM_INDEX])
        when (old) {
            SubscribeButtonState.DEFAULT -> {
                isVisible = true
            }

            SubscribeButtonState.NOT_SUBSCRIBED -> {
                isVisible = true
            }

            else -> Unit
        }
    }
}

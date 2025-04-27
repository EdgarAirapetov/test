package com.meera.core.utils

import android.app.Activity
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.meera.core.BuildConfig
import com.meera.core.R
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getColorCompat
import com.meera.core.extensions.gone
import com.meera.core.extensions.setTint
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.visible
import kotlin.math.ceil

/**
 * todo переписать на BaseTransientBottomBar
 * todo неплохой пример https://medium.com/@fabionegri/make-snackbar-great-again-51edf7c940d4
 *
 * Informational or interactive menu at the bottom of the screen.
 *
 * @param onTimerFinished triggers when timer in finished counting down.
 * @param onDismissedManually triggers when user clicked on cancel button.
 * @param onDismissedSwipe triggers when user closed snack bar by swiping gesture.
 */
class NSnackbar(
    val act: Activity?,
    @SnackBarType val type: Int = SNACKBAR_TYPE_ALERT,
    @BaseTransientBottomBar.Duration val duration: Int = Snackbar.LENGTH_SHORT,
    @DrawableRes val iconDrawable: Int? = null,
    @ColorRes val iconTintColor: Int? = null,
    val title: String = String.empty(),
    val description: String = String.empty(),
    val actionButtonText: String?,
    val dismissSnackbarOnClick: Boolean = true,
    val inView: View? = null,
    val marginBottom: Int = MARGIN_BOTTOM_DEFAULT,
    val timerStartSec: Int = 0,
    val showOnTop: Boolean = false,
    val onTimerFinished: (() -> Unit)?,
    val onDismissedManually: (() -> Unit)?,
    val onDismissedSwipe: (() -> Unit)?,
    val isWhiteBackground: Boolean = false,
    val animationMode: Int
) {
    val classTag = this.simpleName

    val isVisible: Boolean
        get() = snackbar?.isShownOrQueued ?: false

    private var snackbar: Snackbar? = null
    private var countDownTimer: SnackCountDownTimer? = null
    private var snackView: View? = null
    private var snackbarLayout: View? = null
    private var ivIcon: ImageView? = null
    private var tvTitle: TextView? = null
    private var tvDescription: TextView? = null
    private var tvActionButton: TextView? = null
    private var timerView: DonutProgress? = null
    private var snackBarDismissCallback: SnackBarDismissCallback? = null

    init {
        act?.let { act ->
            snackbar = Snackbar.make(inView ?: act.window.decorView, "", duration)
            val layout = snackbar?.view as Snackbar.SnackbarLayout
            snackView = LayoutInflater.from(layout.context).inflate(R.layout.snackbar_container, null)
            snackbarLayout = snackView?.findViewById<ConstraintLayout>(R.id.snackbar_layout)
            ivIcon = snackView?.findViewById(R.id.iv_snackbar)
            tvTitle = snackView?.findViewById(R.id.tv_title)
            tvDescription = snackView?.findViewById(R.id.tv_description)
            tvActionButton = snackView?.findViewById(R.id.tv_btn_action)
            timerView = snackView?.findViewById(R.id.timer_progress)

            initBackground()
            initIcon()
            initIconTintColor()
            initTitle()
            initDescription()
            initActionButton()
            initDismissOnClick()
            initSnackBarCallbacks()
            initTimer()

            setCustomLayout(layout)
            when {
                showOnTop -> setupSnackbarOnTop(snackbar?.view)
                else -> setMarginBottom(snackbar?.view)
            }

            snackbar?.animationMode = animationMode
            snackbar?.show()
        }
    }

    private fun initBackground() {
        if (isWhiteBackground) {
            snackbarLayout?.setBackgroundResource(R.drawable.background_n_snackbar_white)
            tvTitle?.setTextColor(act.getColorCompat(R.color.color_soft_black))
        }
    }

    /**
     * Setup icon by [NSnackbar.type]. Icon is visible for all the types except timer.
     */
    private fun initIcon() {
        when (type) {
            SNACKBAR_TYPE_ALERT -> ivIcon?.setImageResource(R.drawable.alert_info)
            SNACKBAR_TYPE_ERROR -> ivIcon?.setImageResource(R.drawable.alert_error)
            SNACKBAR_TYPE_SUCCESS -> ivIcon?.setImageResource(R.drawable.alert_success)
            SNACKBAR_TYPE_TIMER, SNACKBAR_TYPE_TEXT -> ivIcon?.setImageDrawable(null)
        }
        when (type) {
            SNACKBAR_TYPE_ALERT,
            SNACKBAR_TYPE_ERROR,
            SNACKBAR_TYPE_SUCCESS -> ivIcon?.visible()

            SNACKBAR_TYPE_TIMER, SNACKBAR_TYPE_TEXT -> ivIcon?.gone()
        }
        if (iconDrawable != null) {
            ivIcon?.visible()
            ivIcon?.setImageResource(iconDrawable)
        }
    }

    private fun initIconTintColor() {
        if (iconTintColor != null) {
            ivIcon?.setTint(iconTintColor)
        }
    }

    private fun initTitle() {
        tvTitle?.text = title
    }

    private fun initDescription() {
        if (description.isNotEmpty()) {
            tvDescription?.text = description
        } else {
            tvDescription?.gone()
        }
    }

    private fun initActionButton() {
        actionButtonText?.let { btnText ->
            tvActionButton?.apply {
                visible()
                text = btnText
                setOnClickListener { dismiss() }
            }
        }
    }

    private fun initDismissOnClick() {
        if (dismissSnackbarOnClick) {
            snackbarLayout?.setOnClickListener { dismiss() }
        }
    }

    /**
     * Setup [SnackBarDismissCallback] for [NSnackbar] to track dismiss events.
     */
    private fun initSnackBarCallbacks() {
        if (BuildConfig.DEBUG) {
            SnackBarDismissCallback(
                swipeEvent = { Log.d(classTag, "DISMISS_EVENT_SWIPE called.") },
                actionEvent = { Log.d(classTag, "DISMISS_EVENT_ACTION called.") },
                timeoutEvent = { Log.d(classTag, "DISMISS_EVENT_TIMEOUT called.") },
                manualEvent = { Log.d(classTag, "DISMISS_EVENT_MANUAL called.") },
                consecutiveEvent = { Log.d(classTag, "DISMISS_EVENT_CONSECUTIVE called.") },
            ).let { callback -> snackbar?.addCallback(callback) }
        }
        snackBarDismissCallback = SnackBarDismissCallback(
            manualEvent = onDismissedManually,
            swipeEvent = onDismissedSwipe,
            anyDismissEvent = this::cancelTimer,
        )
        snackbar?.addCallback(snackBarDismissCallback)
    }

    /**
     * Hide timer view if [NSnackbar.type] is informational. Otherwise check parameters and start timer.
     */
    private fun initTimer() {
        if (type == SNACKBAR_TYPE_TIMER) {
            require(timerStartSec > 0) { "Please specify interval for CountDownTimer." }
            timerView?.visible()
            timerView?.text = timerStartSec.toString()
            startTimer()
        } else {
            timerView?.gone()
        }
    }

    private fun startTimer() {
        val multiplier = 1000F
        countDownTimer = SnackCountDownTimer(
            millisInFuture = (timerStartSec * multiplier).toLong(),
            millisCountdownInterval = 10,
            onTick = { tick ->
                val tickSec = ceil(tick / multiplier).toInt()
                val progress = tick / multiplier / timerStartSec * 100F
                timerView?.text = tickSec.toString()
                timerView?.progress = progress * -1
            },
            onFinish = {
                onTimerFinished?.invoke()
                dismissNoCallbacks()
            })
        countDownTimer?.start()
    }

    private fun setCustomLayout(layout: Snackbar.SnackbarLayout) {
        with(layout) {
            snackView?.setPadding(
                0,
                SNACKBAR_VIEW_VERTICAL_PADDING.dp,
                0,
                SNACKBAR_VIEW_VERTICAL_PADDING.dp
            )
            addView(snackView, 0)
            setBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent))
        }
    }

    private fun setMarginBottom(view: View?) {
        view?.let {
            val margin = when {
                inView != null && marginBottom == MARGIN_BOTTOM_DEFAULT -> MARGIN_BOTTOM_DEFAULT
                inView == null && marginBottom == MARGIN_BOTTOM_DEFAULT -> MARGIN_BOTTOM_DEFAULT + MARGIN_BOTTOM_THRESHOLD
                else -> marginBottom
            }

            val snackBarView = snackbar?.view
            snackBarView?.post {
                val params = snackBarView.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(
                    params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + margin.dp
                )
                snackBarView.layoutParams = params
            }
        }
    }

    private fun setupSnackbarOnTop(view: View?) {
        if (view == null) return
        view.layoutParams = (view.layoutParams as? FrameLayout.LayoutParams?)?.apply {
            gravity = Gravity.TOP
        }
        view.post {
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(
                params.leftMargin,
                params.topMargin + MARGIN_TOP_DEFAULT.dp,
                params.rightMargin,
                params.bottomMargin
            )
            view.layoutParams = params
        }
    }

    /**
     * Cancel [SnackCountDownTimer] manually.
     */
    private fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    /**
     * Remove [NSnackbar] from screen and triggers appropriate callbacks.
     * One of [SnackBarDismissCallback] methods is called if snack bar was dismissed by user.
     * If snack bar was dismissed by timer only [NSnackbar.onTimerFinished] is called.
     */
    fun dismiss() {
        snackbar?.dismiss()
    }

    /**
     * Dismiss [NSnackbar] without triggering any callbacks.
     */
    fun dismissNoCallbacks() {
        snackbar?.removeCallback(snackBarDismissCallback)
        dismiss()
    }

    /**
     * Wrapper class for [CountDownTimer] with convenient [onTick] and [onFinish] callbacks.
     */
    inner class SnackCountDownTimer(
        millisInFuture: Long,
        millisCountdownInterval: Long,
        val onTick: (Long) -> Unit,
        val onFinish: () -> Unit,
    ) : CountDownTimer(millisInFuture, millisCountdownInterval) {

        override fun onTick(millisUntilFinished: Long) {
            onTick.invoke(millisUntilFinished)
        }

        override fun onFinish() {
            onFinish.invoke()
        }
    }

    /**
     * Helper nested class which allows to add events listeners by one with a meaningful name.
     * More details about event types can be found in [BaseTransientBottomBar.BaseCallback]
     *
     * @param anyDismissEvent triggers when [NSnackbar] is dismissed by any reason.
     */
    inner class SnackBarDismissCallback(
        private val swipeEvent: (() -> Unit)? = null,
        private val actionEvent: (() -> Unit)? = null,
        private val timeoutEvent: (() -> Unit)? = null,
        private val manualEvent: (() -> Unit)? = null,
        private val consecutiveEvent: (() -> Unit)? = null,
        private val anyDismissEvent: (() -> Unit)? = null,
    ) : Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            when (event) {
                BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_SWIPE -> swipeEvent?.invoke()
                BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION -> actionEvent?.invoke()
                BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT -> timeoutEvent?.invoke()
                BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL -> manualEvent?.invoke()
                BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_CONSECUTIVE -> consecutiveEvent?.invoke()
            }
            anyDismissEvent?.invoke()
        }
    }

    @IntDef(
        SNACKBAR_TYPE_ERROR,
        SNACKBAR_TYPE_ALERT,
        SNACKBAR_TYPE_SUCCESS,
        SNACKBAR_TYPE_TIMER,
        SNACKBAR_TYPE_TEXT
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class SnackBarType

    class Builder(private val act: Activity?, view: View?) {
        @SnackBarType
        private var snackBarType: Int = SNACKBAR_TYPE_TIMER

        @BaseTransientBottomBar.Duration
        private var snackBarDuration: Int = Snackbar.LENGTH_SHORT
        @DrawableRes
        private var iconDrawable: Int? = null
        @ColorRes
        private var iconTintColor: Int? = null
        private var title: String = String.empty()
        private var description: String = String.empty()
        private var actionButtonText: String? = null
        private var dismissSnackbarOnClick: Boolean = true
        private var inView: View? = view
        private var marginBottom: Int = MARGIN_BOTTOM_DEFAULT
        private var timerStartSec: Int = 0
        private var onTimerFinished: (() -> Unit)? = null
        private var onDismissedManually: (() -> Unit)? = null
        private var onDismissedSwipe: (() -> Unit)? = null
        private var showOnTop: Boolean = false
        private var isWhiteBackground: Boolean = false
        private var animationMode: Int = ANIMATION_MODE_SLIDE

        fun setAnimationMode(mode: Int): Builder {
            this.animationMode = mode;
            return this
        }

        fun type(@SnackBarType type: Int): Builder {
            this.snackBarType = type
            return this
        }

        fun typeAlert(): Builder {
            this.snackBarType = SNACKBAR_TYPE_ALERT
            return this
        }

        fun typeSuccess(): Builder {
            this.snackBarType = SNACKBAR_TYPE_SUCCESS
            return this
        }

        fun typeError(): Builder {
            this.snackBarType = SNACKBAR_TYPE_ERROR
            return this
        }


        fun typeText(): Builder {
            this.snackBarType = SNACKBAR_TYPE_TEXT
            return this
        }

        fun duration(@BaseTransientBottomBar.Duration duration: Int): Builder {
            this.snackBarDuration = duration
            return this
        }

        fun durationLong(): Builder {
            this.snackBarDuration = Snackbar.LENGTH_LONG
            return this
        }

        fun durationIndefinite(): Builder {
            this.snackBarDuration = Snackbar.LENGTH_INDEFINITE
            return this
        }

        fun text(text: String?): Builder {
            try {
                this.title = text ?: String.empty()
            } catch (e: Exception) {
                this.title = String.empty()
                e.printStackTrace()
            }
            return this
        }

        fun description(text: String?): Builder {
            try {
                this.description = text ?: String.empty()
            } catch (e: Exception) {
                this.description = String.empty()
                e.printStackTrace()
            }
            return this
        }

        fun inView(inView: View?): Builder {
            this.inView = inView
            return this
        }

        fun setWhiteBackground(): Builder {
            this.isWhiteBackground = true
            return this
        }

        fun button(actionButtonText: String): Builder {
            this.actionButtonText = actionButtonText
            return this
        }

        fun timer(timerStartSec: Int, onTimerFinished: (() -> Unit)? = null): Builder {
            this.snackBarType = SNACKBAR_TYPE_TIMER
            this.timerStartSec = timerStartSec
            this.onTimerFinished = onTimerFinished
            return this
        }

        fun marginBottom(marginBottom: Int): Builder {
            this.marginBottom = marginBottom
            return this
        }

        fun dismissManualListener(onDismissedManually: () -> Unit): Builder {
            this.onDismissedManually = onDismissedManually
            return this
        }

        fun dismissSwipeListener(onDismissedSwipe: () -> Unit): Builder {
            this.onDismissedSwipe = onDismissedSwipe
            return this
        }

        fun setIcon(@DrawableRes iconDrawable: Int?): Builder {
            this.iconDrawable = iconDrawable
            return this
        }

        fun showOnTop(showOnTop: Boolean): Builder {
            this.showOnTop = showOnTop
            return this
        }

        fun iconTintColor(color: Int): Builder {
            this.iconTintColor = color
            return this
        }

        fun show(): NSnackbar = NSnackbar(
            act = act,
            type = snackBarType,
            duration = snackBarDuration,
            iconDrawable = iconDrawable,
            iconTintColor = iconTintColor,
            title = title,
            description = description,
            actionButtonText = actionButtonText,
            dismissSnackbarOnClick = dismissSnackbarOnClick,
            inView = inView,
            timerStartSec = timerStartSec,
            onTimerFinished = onTimerFinished,
            marginBottom = marginBottom,
            onDismissedManually = onDismissedManually,
            onDismissedSwipe = onDismissedSwipe,
            showOnTop = showOnTop,
            isWhiteBackground = isWhiteBackground,
            animationMode = animationMode
        )
    }

    companion object {

        fun with(activity: Activity?): Builder {
            return Builder(activity, null)
        }

        fun with(view: View?): Builder {
            return if (view?.context is Activity) {
                val activity = view.context as Activity
                Builder(activity, view)
            } else {
                Builder(null, view)
            }
        }

        private const val SNACKBAR_TYPE_ERROR = 1
        private const val SNACKBAR_TYPE_ALERT = 2
        private const val SNACKBAR_TYPE_SUCCESS = 3
        private const val SNACKBAR_TYPE_TIMER = 4
        private const val SNACKBAR_TYPE_TEXT = 5

        private const val MARGIN_TOP_DEFAULT = 65
        private const val MARGIN_BOTTOM_DEFAULT = 100
        private const val MARGIN_BOTTOM_THRESHOLD = 40
        private const val SNACKBAR_VIEW_VERTICAL_PADDING = 4
    }
}


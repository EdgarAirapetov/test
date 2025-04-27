package com.numplates.nomera3.presentation.view.ui.customView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.card.MaterialCardView
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.getColorCompat
import com.meera.core.extensions.getSilentState
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import java.util.*
import kotlin.math.abs

const val SWITCH_CALL_STATE_DISABLED = 0
const val SWITCH_CALL_STATE_LOCKED = 1
const val SWITCH_CALL_STATE_ALLOWED = 2

private const val BUTTON_ANIMATION_DURATION = 100L
private const val SETTING_DURATION = 200L
private const val PROGRESS_CHECKED_THRESHOLD = 0.95F

private const val PROGRESS_MAX = 1F
private const val PROGRESS_MIN = 0F

class CallSwitchView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val effectMediaPlayer = MediaPlayer()

    var state = SWITCH_CALL_STATE_DISABLED
        set(value) {
            if (value != state) {
                field = value
                setSwitchState(value)
            }
        }

    var onCheckedChangeListener: ((state: Int, isChecked: Boolean) -> Unit)? = null
    var onClick: ((state: Int, isChecked: Boolean) -> Unit)? = null
    var shouldInterceptTouch: ((should: Boolean) -> Unit)? = null

    private val interpolator by lazy { DecelerateInterpolator() }

    private var transfusionView: View? = null

    private val view: View = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        .inflate(R.layout.layout_view_switch_call, this)
    private val mlSwitchContainer: MotionLayout = view.findViewById(R.id.ml_switch_container)
    private val tvTextLeft: TextView = view.findViewById(R.id.tv_text_left)
    private val tvTextRight: TextView = view.findViewById(R.id.tv_text_right)
    private val ivSwitchButtonLeft: ImageView = view.findViewById(R.id.iv_switch_button_left)
    private val ivSwitchButtonRight: ImageView = view.findViewById(R.id.iv_switch_button_right)
    private val mcvTextContainer: MaterialCardView = view.findViewById(R.id.mcv_text_container)

    init {


        mlSwitchContainer.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) = Unit
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) = Unit
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, progress: Float) {
                if (progress > 0 && transfusionView != null) {
                    transfusionView!!.animate().cancel()
                }
                tvTextLeft.alpha = progress
                tvTextRight.alpha = abs(PROGRESS_MAX - progress)
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                if (needToToggle) onCheckedChangeListener?.invoke(state, isChecked())
                else needToToggle = true
                setTexts()
                if (!isChecked()) {
                    animateTransfusion()
                } else if (isChecked() && state == SWITCH_CALL_STATE_ALLOWED) {
                    playToggleOn()
                    toggleSoundAllowed = true
                    doDelayed(300) {
                        heartBeatButtonAnimation()
                    }
                    doDelayed(600) {
                        heartBeatButtonAnimation()
                    }
                }
            }
        })

        setClickListener()
    }

    var needToToggle = true
    var toggleSoundAllowed = true
    var isCheckedToggle = false
    fun setChecked(
        isChecked: Boolean,
        needToToogle: Boolean = true
    ) {
        isCheckedToggle = isChecked
        this.needToToggle = needToToogle
        toggleSoundAllowed = false
        if (isChecked) mlSwitchContainer.transitionToEnd()
        else mlSwitchContainer.transitionToStart()
        mlSwitchContainer.progress = if (isChecked) PROGRESS_MAX else PROGRESS_MIN
        doDelayed(SETTING_DURATION) { setTexts() }
        if (!isChecked) {
            tvTextRight.alpha = 1f
            tvTextLeft.alpha = 0f
        } else {
            tvTextRight.alpha = 0f
            tvTextLeft.alpha = 1f
        }
    }

    fun isChecked(): Boolean = mlSwitchContainer.progress > PROGRESS_CHECKED_THRESHOLD

    private fun setSwitchState(state: Int) {
        when (state) {
            SWITCH_CALL_STATE_DISABLED -> {
                mlSwitchContainer.getTransition(R.id.switch_call_transition)?.isEnabled = false
                mlSwitchContainer.setOnClickListener(null)
                ivSwitchButtonLeft.loadGlide(R.drawable.switch_call_inactive)
                ivSwitchButtonRight.loadGlide(R.drawable.switch_call_block)
                tvTextLeft.setTextColor(context.getColorCompat(R.color.text_red))
                tvTextLeft.text = context.getString(R.string.call_switch_disabled)
                tvTextRight.text =
                    context.getString(R.string.call_switch_enabled).lowercase(Locale.getDefault())
                tvTextRight.setTextColor(context.getColorCompat(R.color.ui_gray))
                mlSwitchContainer.loadLayoutDescription(R.xml.scene_switch_call_disabled)
            }
            SWITCH_CALL_STATE_LOCKED -> {
                mlSwitchContainer.getTransition(R.id.switch_call_transition)?.isEnabled = true
                ivSwitchButtonLeft.loadGlide(R.drawable.switch_call_inactive)
                ivSwitchButtonRight.loadGlide(R.drawable.switch_call_block)
                tvTextLeft.setTextColor(context.getColorCompat(R.color.text_red))
                tvTextLeft.text = context.getString(R.string.call_switch_disabled)
                tvTextRight.text =
                    context.getString(R.string.call_switch_enabled).lowercase(Locale.getDefault())
                tvTextRight.setTextColor(context.getColorCompat(R.color.ui_gray))
                mlSwitchContainer.loadLayoutDescription(R.xml.scene_switch_call_locked)
            }
            SWITCH_CALL_STATE_ALLOWED -> {
                mlSwitchContainer.getTransition(R.id.switch_call_transition)?.isEnabled = true
                ivSwitchButtonLeft.loadGlide(R.drawable.switch_call_inactive)
                ivSwitchButtonRight.loadGlide(R.drawable.switch_call_active)
                tvTextLeft.setTextColor(context.getColorCompat(R.color.ui_purple))
                tvTextLeft.text = context.getString(R.string.call_switch_make_call)
                tvTextRight.text =
                    context.getString(R.string.call_switch_enabled).lowercase(Locale.getDefault())
                tvTextRight.setTextColor(context.getColorCompat(R.color.ui_gray))
                mlSwitchContainer.loadLayoutDescription(R.xml.scene_switch_call_allowed)
            }
        }

        if (!isChecked() && state != SWITCH_CALL_STATE_DISABLED) {
            animateTransfusion()
        }
    }

    private fun playToggleOn() {
        if (context.getSilentState() || !toggleSoundAllowed) return
        effectMediaPlayer.stop()
        effectMediaPlayer.reset()
        val afd = resources.openRawResourceFd(R.raw.auto4) ?: return
        effectMediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        effectMediaPlayer.prepare()
        effectMediaPlayer.isLooping = false
        effectMediaPlayer.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setClickListener() {
        var startX = 0f
        var startY = 0f
        var newX = 0f
        var newY = 0f
        val MAX_DISTANCE = 10.dp

        mlSwitchContainer.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    toggleSoundAllowed = true
                    shouldInterceptTouch?.invoke(true)
                    newX = motionEvent.x
                    newY = motionEvent.y
                    startX = newX
                    startY = newY

                    if (isChecked()) {
                        ivSwitchButtonRight.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50).start()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    newX = motionEvent.x
                    newY = motionEvent.y
                    if (abs(startX - newX) > MAX_DISTANCE && abs(startY - newY) > MAX_DISTANCE) {
                        startX = 0f
                        startY = 0f
                    }
                    if (ivSwitchButtonRight.scaleX != 1f) {
                        ivSwitchButtonRight.scaleX = 1f
                        ivSwitchButtonRight.scaleY = 1f
                    }
                }
                MotionEvent.ACTION_UP -> {
                    shouldInterceptTouch?.invoke(false)
                    ivSwitchButtonRight.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
                    if (abs(startX - newX) < MAX_DISTANCE && abs(startY - newY) < MAX_DISTANCE) {
                        if (!isChecked()) {
                            mlSwitchContainer.transitionToEnd()
                        }
                        onClick?.invoke(state, isChecked())
                        return@setOnTouchListener true
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    shouldInterceptTouch?.invoke(false)
                    ivSwitchButtonRight.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun createTransfusionView(): View =
            View(context).apply {
                layoutParams = ViewGroup.LayoutParams(6.dp, 100.dp)
                setBackgroundColor(Color.parseColor("#ECECEC"))
                translationY = (-20f).dp
                translationX = (-40f).dp
                rotation = 30f
            }

    private fun animateTransfusion() {
        doDelayed(1000) {
            if (mlSwitchContainer.progress == 0f) {
                transfusionView = createTransfusionView()
                mcvTextContainer.addView(transfusionView)
                transfusionView!!.animate()
                        .translationX(mcvTextContainer.width * 1.5f)
                        .setInterpolator(interpolator)
                        .setDuration(1800)
                        .setListener(
                                onAnimationEnd = {
                                    mcvTextContainer.removeView(transfusionView)
                                    transfusionView = null
                                },
                                onAnimationCancel = {
                                    mcvTextContainer.removeView(transfusionView)
                                    transfusionView = null
                                })
                        .start()
            }
        }
    }

    private fun setTexts() {
        if (isChecked()) {
            tvTextRight.invisible()
            tvTextLeft.visible()
        } else {
            tvTextLeft.invisible()
            tvTextRight.visible()
        }
    }

    private fun heartBeatButtonAnimation() {
        ivSwitchButtonRight.animate().cancel()

        ivSwitchButtonRight.animate()
                .scaleY(0.85f)
                .scaleX(0.85f)
                .setInterpolator(interpolator)
                .setDuration(BUTTON_ANIMATION_DURATION)
                .setListener(onAnimationEnd = {
                    doDelayed(30) {
                        ivSwitchButtonRight.animate().cancel()
                        ivSwitchButtonRight.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setInterpolator(interpolator)
                                .setDuration(BUTTON_ANIMATION_DURATION)
                                .start()
                    }
                })
                .start()
    }

    override fun onDetachedFromWindow() {
        onCheckedChangeListener = null
        onClick = null
        shouldInterceptTouch = null
        super.onDetachedFromWindow()
    }
}

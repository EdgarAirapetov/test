package com.meera.core.extensions

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.SystemClock
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.view.marginStart
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.meera.core.R
import com.meera.core.base.viewbinding.checkMainThread
import com.meera.core.utils.BounceInterpolator
import com.meera.core.utils.CompoundDrawableClickListener
import com.meera.core.utils.EnhancedMovementMethod
import com.meera.core.utils.transition.PaddingTransitionFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.security.MessageDigest
import kotlin.math.abs

// Данная константа устанавливает default delay для ClickListener
const val DEFAULT_CLICK_DELAY = 1000L
const val DEFAULT_DRAWABLE_FUZZ = 4
private const val STAND_DOWN_ROTATION_POSITION = -270f
private const val STAND_UP_ROTATION_POSITION = -360f
private const val ROTATION_X_PROPERTY = "rotationX"
private const val ALPHA_PROPERTY = "alpha"

private const val VISIBLE_SCALE = 1f
private const val INVISIBLE_SCALE = 0f
private const val ANIM_DURATION_HIDE = 150L
private const val ANIM_DURATION_SHOW = 250L

fun View.visible() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

fun View.invisible() {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }
}

fun View.gone() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

fun View.setVisible(value: Boolean) {
    if (value) {
        this.visible()
    } else {
        this.gone()
    }
}

fun View.isOnTheScreen(): Boolean {
    if (!isShown) {
        return false
    }
    val actualPosition = Rect()
    getGlobalVisibleRect(actualPosition)
    val screen = Rect(0, 0, getScreenWidth(), getScreenHeight())
    return actualPosition.intersect(screen)
}

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}

fun View.isVisibleToUser(): Boolean {
    val nodeInfo = AccessibilityNodeInfo.obtain()
    this.onInitializeAccessibilityNodeInfo(nodeInfo)
    return nodeInfo.isVisibleToUser
}

fun View.visibleAnimation(duration: Int = 150) {
    if (visibility != View.VISIBLE) {
        val autoTransition = AutoTransition()
            .setDuration(duration.toLong())
            .setInterpolator(DecelerateInterpolator())
        TransitionManager.beginDelayedTransition(parent as ViewGroup, autoTransition)
        visible()
    }
}

fun View.invisibleAnimation(duration: Int = 150) {
    if (visibility != View.INVISIBLE) {
        val autoTransition = AutoTransition()
            .setDuration(duration.toLong())
            .setInterpolator(DecelerateInterpolator())
        TransitionManager.beginDelayedTransition(parent as ViewGroup, autoTransition)
        invisible()
    }
}

fun View.applyRoundedOutline(@DimenRes radiusRes: Int) {
    val radius: Float = resources.getDimension(radiusRes)
    applyRoundedOutline(radius)
}

fun View.applyRoundedOutline(radius: Float) {
    applyRoundedOutline(top = 0, radius = radius)
}

fun View.applyRoundedOutline(top: Int = 0, radius: Float = 0f) {
    outlineProvider = object : ViewOutlineProvider() {

        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, top, view.measuredWidth, view.measuredHeight, radius)
        }
    }
    clipToOutline = true
}
fun View.applyTopRoundedCorners(radius: Float = 0f) {
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, top, view.measuredWidth, view.measuredHeight + radius.toInt(), radius)
        }
    }
    clipToOutline = true
}

fun View.applyRoundedOutlineStoke(
    @DimenRes cornerRadiusRes: Int,
    @DimenRes strokeWidthRes: Int,
    @ColorRes strokeColor: Int
) {
    val cornerRadius = resources?.getDimension(cornerRadiusRes) ?: 0f
    val strokeWidth = resources?.getDimension(strokeWidthRes) ?: 0f
    applyRoundedOutlineStoke(
        cornerRadius = cornerRadius,
        strokeWidth = strokeWidth.toInt(),
        strokeColor = strokeColor
    )
}

fun View.applyRoundedOutlineStoke(
    cornerRadius: Float,
    strokeWidth: Int,
    @ColorRes strokeColor: Int
) {
    val gradientDrawable = GradientDrawable()
    gradientDrawable.cornerRadius = cornerRadius
    gradientDrawable.setStroke(
        strokeWidth,
        context.color(strokeColor)
    )
    this.background = gradientDrawable
}

fun View.goneAnimation(duration: Int = 150) {
    if (visibility != View.GONE) {
        val autoTransition = AutoTransition()
            .setDuration(duration.toLong())
            .setInterpolator(DecelerateInterpolator())
        TransitionManager.beginDelayedTransition(parent as ViewGroup, autoTransition)
        gone()
    }
}

fun View.setClickEnabled(enabled: Boolean) {
    this.isEnabled = enabled
    if (this is ViewGroup) {
        val group = this
        for (idx in 0 until group.childCount) {
            group.getChildAt(idx).setClickEnabled(enabled)
        }
    }
}

fun View.visibleSlideInAnimate() {
    val anim = AnimationUtils.loadAnimation(this.context, R.anim.slide_in_bottom_custom)
    this.visibility = View.VISIBLE
    this.isClickable = false
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(p0: Animation?) {
            this@visibleSlideInAnimate.isClickable = true
        }

        override fun onAnimationRepeat(p0: Animation?) = Unit

        override fun onAnimationStart(p0: Animation?) = Unit
    })
    this.startAnimation(anim)
}

inline fun AnimationSet.addAnimationListener(
    crossinline animationStartListener: (() -> Unit) = { },
    crossinline animationEndListener: (() -> Unit) = { },
    crossinline animationRepeatListener: (() -> Unit) = { }
) {
    this.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) = animationStartListener.invoke()

        override fun onAnimationEnd(animation: Animation?) = animationEndListener.invoke()

        override fun onAnimationRepeat(animation: Animation?) = animationRepeatListener.invoke()
    })
}

fun View.visibleAppearAnimate() {
    this.visibility = View.VISIBLE
    this.alpha = 0f
    this.animate().alpha(1F).duration = 300
}

const val VIEW_DISAPPEAR_DURATION_MS = 300L
fun View.visibleDisAppearAnimate() {
    this.alpha = 1f
    this.animate()
        .alpha(0F)
        .duration = VIEW_DISAPPEAR_DURATION_MS
}

/**
 * Inflate container
 * Example: val view = container?.inflate(R.layout.news_fragment)
 */
fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View =
    LayoutInflater.from(context).inflate(layoutRes, this, false)


fun ViewGroup.inflateLayout(@LayoutRes id: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(this.context).inflate(id, this, attachToRoot)


fun View?.showKeyboard() {
    this?.let {
        requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View?.hideKeyboard() {
    this?.let {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        clearFocus()
    }
}

fun View.circularProgressDrawable(color: Int = Color.WHITE): CircularProgressDrawable {
    val circularProgressDrawable = CircularProgressDrawable(this.context)
    circularProgressDrawable.strokeWidth = 10f
    circularProgressDrawable.centerRadius = 60f
    circularProgressDrawable.setColorSchemeColors(color)
    circularProgressDrawable.start()
    return circularProgressDrawable
}

fun EditText.clearText() {
    this.setText("")
}

fun TextView.clearText() {
    this.text = ""
}

fun View.visibleAppearAnimateWithBlock() {
    isClickable = false
    visibility = View.VISIBLE
    alpha = 0f
    animate().withEndAction {
        this.isClickable = true
    }.alpha(1F).duration = 300
}

fun View?.animateSnippetMargins(
    newMargins: Int,
    duration: Long,
    onAnimationEnd: (() -> Unit)? = null
) {
    this?.let {
        val prevHeight = marginStart
        val valueAnimator = ValueAnimator.ofInt(prevHeight, newMargins)
        valueAnimator.addUpdateListener { animation ->
            val marginsCurrent = animation.animatedValue as Int
            setMargins(start = marginsCurrent, end = marginsCurrent, bottom = marginsCurrent)
            requestLayout()
        }
        onAnimationEnd?.let {
            valueAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd.invoke()
                }
            })
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration
        valueAnimator.start()
    }
}

fun View?.animateHorizontalMargins(
    newMargins: Int,
    duration: Long,
    onAnimationEnd: (() -> Unit)? = null
) {
    this?.let {
        val prevHeight = marginStart
        val valueAnimator = ValueAnimator.ofInt(prevHeight, newMargins)
        valueAnimator.addUpdateListener { animation ->
            val marginsCurrent = animation.animatedValue as Int
            setMargins(start = marginsCurrent, end = marginsCurrent)
            requestLayout()
        }
        onAnimationEnd?.let {
            valueAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd.invoke()
                }
            })
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration
        valueAnimator.start()
    }
}

fun View?.animateHeight(
    newHeight: Int,
    duration: Long,
    onAnimationEnd: (() -> Unit)? = null
) {
    this?.let {
        val prevHeight = height
        val valueAnimator = ValueAnimator.ofInt(prevHeight, newHeight)
        valueAnimator.addUpdateListener { animation ->
            layoutParams.height = animation.animatedValue as Int
            requestLayout()
        }
        onAnimationEnd?.let {
            valueAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd.invoke()
                }
            })
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration
        valueAnimator.start()
    }
}

fun View?.animateHeight(
    prevHeight: Int,
    newHeight: Int,
    duration: Long,
    onAnimationEnd: (() -> Unit)? = null
) {
    this?.let {
        val valueAnimator = ValueAnimator.ofInt(prevHeight, newHeight)
        valueAnimator.addUpdateListener { animation ->
            layoutParams.height = animation.animatedValue as Int
            requestLayout()
        }
        onAnimationEnd?.let {
            valueAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd.invoke()
                }
            })
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration
        valueAnimator.start()
    }
}

fun View?.animateWidth(
    newWidth: Int,
    duration: Long,
    interpolator: Interpolator = DecelerateInterpolator(),
    startWidth: Int? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    this?.let {
        val prevWidth = startWidth ?: width
        val valueAnimator = ValueAnimator.ofInt(prevWidth, newWidth)
        valueAnimator.addUpdateListener { animation ->
            layoutParams.width = animation.animatedValue as Int
            requestLayout()
        }
        onAnimationEnd?.let {
            valueAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd.invoke()
                }
            })
        }
        valueAnimator.interpolator = interpolator
        valueAnimator.duration = duration
        valueAnimator.start()
    }
}

inline fun <T : View?> T.onMeasured(crossinline f: T.() -> Unit) {
    this?.viewTreeObserver?.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                try {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    })
}

fun View.onLayout(onLayout: (View) -> Boolean) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View, left: Int, top: Int, right: Int, bottom: Int,
            oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
        ) {
            if (onLayout(view)) {
                view.removeOnLayoutChangeListener(this)
            }
        }
    })
}

fun View?.onSizeChange(runnable: () -> Unit) = this?.apply {
    addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        val rect = Rect(left, top, right, bottom)
        val oldRect = Rect(oldLeft, oldTop, oldRight, oldBottom)
        if (rect.width() != oldRect.width() || rect.height() != oldRect.height()) {
            runnable()
        }
    }
}

fun View.updatePadding(
    paddingStart: Int = getPaddingStart(),
    paddingTop: Int = getPaddingTop(),
    paddingEnd: Int = getPaddingEnd(),
    paddingBottom: Int = getPaddingBottom()
) {
    setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
}

fun View.setPaddingLeft(value: Int) =
    setPadding(value, paddingTop, paddingRight, paddingBottom)

fun View.setPaddingRight(value: Int) =
    setPadding(paddingLeft, paddingTop, value, paddingBottom)

fun View.setPaddingTop(value: Int) =
    setPaddingRelative(paddingStart, value, paddingEnd, paddingBottom)

fun View.setPaddingBottom(value: Int) =
    setPaddingRelative(paddingStart, paddingTop, paddingEnd, value)

fun View.setPaddingStart(value: Int) =
    setPaddingRelative(value, paddingTop, paddingEnd, paddingBottom)

fun View.setPaddingEnd(value: Int) =
    setPaddingRelative(paddingStart, paddingTop, value, paddingBottom)

fun View.setMargins(
    start: Int? = null,
    top: Int? = null,
    end: Int? = null,
    bottom: Int? = null
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = start ?: marginStart
            topMargin = top ?: topMargin
            marginEnd = end ?: marginEnd
            bottomMargin = bottom ?: bottomMargin
        }
    }
}

fun View.newSize(
    width: Int, height: Int
) {
    layoutParams.width = width
    layoutParams.height = height
    layoutParams = layoutParams
}

fun View.newHeight(height: Int) {
    layoutParams.width = width
    layoutParams.height = height
    layoutParams = layoutParams
}

fun View.getBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    draw(canvas)
    canvas.save()
    return bmp
}

@SuppressLint("ClickableViewAccessibility")
inline fun View?.setOnClickXY(crossinline onClick: (rX: Int, rY: Int) -> Unit) {
    this?.let {
        var startX = 0f
        var startY = 0f
        var newX = 0f
        var newY = 0f
        val maxDistance = 10.dp
        setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    newX = motionEvent.rawX
                    newY = motionEvent.rawY
                    startX = newX
                    startY = newY
                }

                MotionEvent.ACTION_MOVE -> {
                    newX = motionEvent.rawX
                    newY = motionEvent.rawY
                    if (abs(startX - newX) > maxDistance && abs(startY - newY) > maxDistance) {
                        startX = 0f
                        startY = 0f
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (abs(startX - newX) < maxDistance && abs(startY - newY) < maxDistance) {
                        onClick.invoke(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())
                        return@setOnTouchListener true
                    }
                }
            }
            return@setOnTouchListener false
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
inline fun View?.setOnLongClickXY(crossinline onClick: (rX: Int, rY: Int) -> Unit) {
    this?.let {
        var rX = 0
        var rY = 0
        setOnLongClickListener {
            onClick(rX, rY)
            return@setOnLongClickListener true
        }
        setOnTouchListener { _, motionEvent ->
            rX = motionEvent.rawX.toInt()
            rY = motionEvent.rawY.toInt()
            return@setOnTouchListener false
        }
    }
}

fun View.revealAnimate(startOffsetMs: Long = 0) {
    val myAnim = AnimationUtils.loadAnimation(context, R.anim.reveal_anim)
    val interpolator = FastOutSlowInInterpolator()
    myAnim.interpolator = interpolator
    myAnim.startOffset = startOffsetMs
    this.startAnimation(myAnim)
}

fun View.hideAnimate() {
    val myAnim = AnimationUtils.loadAnimation(context, R.anim.hide_anim)
    val interpolator = FastOutSlowInInterpolator()
    myAnim.interpolator = interpolator
    myAnim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(p0: Animation?) {
            if (this@hideAnimate.context != null && this@hideAnimate.isAttachedToWindow) {
                this@hideAnimate.gone()
            }
        }

        override fun onAnimationStart(p0: Animation?) = Unit
        override fun onAnimationRepeat(p0: Animation?) = Unit
    })
    this.startAnimation(myAnim)
}

fun View.clickAnimate() {
    val myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce)
    val interpolator = BounceInterpolator(0.1, 20.0)
    myAnim.interpolator = interpolator
    this.startAnimation(myAnim)
}

fun View.clickAnimateScaleUp() {
    val myAnim = AnimationUtils.loadAnimation(context, R.anim.registration_next_button)
    myAnim.interpolator = DecelerateInterpolator()
    this.clearAnimation()
    this.startAnimation(myAnim)
}

fun View.setBackgroundTint(@ColorRes colorRes: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        background?.colorFilter =
            BlendModeColorFilter(context.getColorCompat(colorRes), BlendMode.SRC_ATOP)
    } else {
        background?.setColorFilter(context.getColorCompat(colorRes), PorterDuff.Mode.SRC_ATOP)
    }
}

fun View.setBackgroundTintColor(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        background?.colorFilter =
            BlendModeColorFilter(color, BlendMode.SRC_IN)
    } else {
        background?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

/**
 * If ellipsize set programmatically call View.doOnPreDraw { isEllipsized } after layout created
 */
fun TextView.isEllipsized(): Boolean {
    val layout = this.layout
    layout?.let {
        val lines = layout.lineCount
        if (lines > 0) {
            val ellipsizeCount = layout.getEllipsisCount(lines - 1)
            return ellipsizeCount > 0
        }
    }
    return false
}

fun View.setBackgroundShapeColor(@ColorRes colorRes: Int) {
    this.background?.let { backgroundDrawable: Drawable ->
        DrawableCompat.setTint(
            DrawableCompat.wrap(backgroundDrawable).mutate(),
            ContextCompat.getColor(context, colorRes)
        )
    }
}

fun View.click(click: (View) -> Unit) {
    setOnClickListener { click(it) }
}

fun View.longClick(click: (View) -> Unit) {
    setOnLongClickListener {
        click(it); false
    }
}

fun View.expandTouchArea(offsetDp: Int) {
    val parent = this.parent as View
    parent.post {
        val touchableArea = Rect()
        this.getHitRect(touchableArea)
        touchableArea.top -= offsetDp
        touchableArea.bottom += offsetDp
        touchableArea.left -= offsetDp
        touchableArea.right += offsetDp
        parent.touchDelegate = TouchDelegate(touchableArea, this)
    }
}

fun View.setThrottledClickListener(
    delay: Long = DEFAULT_CLICK_DELAY,
    clickListener: () -> Unit
) {
    var lastClickedTime = 0L

    this.setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickedTime < delay) return@setOnClickListener

        clickListener.invoke()
        lastClickedTime = SystemClock.elapsedRealtime()
    }
}

fun View.animateWithColor(
    @ColorInt firstColor: Int,
    @ColorInt secondColor: Int,
    durationMills: Long
) {
    ValueAnimator.ofInt(
        firstColor,
        secondColor
    ).apply {
        interpolator = LinearInterpolator()
        duration = durationMills
        repeatCount = 1
        repeatMode = ValueAnimator.REVERSE
        setEvaluator(ArgbEvaluator())
        addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            backgroundTintList = ColorStateList.valueOf(animatedValue)
        }
        start()
    }
}

/**
 * Данный extension используйте тогда, когда вам нужно в процессе ввода текста распознать какие-то слова
 * По дефолту текст будет иметь состояние Typeface.BOLD
 * @param rangeList - массив, который возможно имеет ключевые слова
 * @param color - Устанавливается цвет
 * @param onClickListener - Лямда, если нужно поставить обработку клика
 */
fun EditText.addSpanBoldRangesClickColored(
    rangeList: List<IntRange>,
    @ColorInt color: Int,
    onClickListener: (() -> Unit)? = null
) {
    this.clearSpans()
    if (rangeList.isEmpty()) return
    rangeList.forEach { range ->
        addSpanBoldRangeClickColored(
            range = range,
            color = color,
            onClickListener = onClickListener
        )
    }
}

/**
 * Данный extension используйте тогда, когда вам нужно в процессе ввода текста распознать какие-то слова
 * По дефолту текст будет иметь состояние Typeface.BOLD
 * @param range - Range, которые содержит ключевые слова
 * @param color - Устанавливается цвет
 * @param onClickListener - Лямда, если нужно поставить обработку клика
 */
fun EditText.addSpanBoldRangeClickColored(
    range: IntRange,
    @ColorInt color: Int,
    onClickListener: (() -> Unit)? = null
) {
    if (range.first < 0 || range.last > this.text.length) return
    val clickableSpan = getClickableSpanWithDataBold(
        color = color,
        onClickListener = onClickListener
    )
    this.movementMethod = EnhancedMovementMethod
    try {
        text.setSpan(
            clickableSpan,
            range.first,
            range.last,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun EditText.clearSpans() {
    val spansCount = this.text.getSpans(
        0,
        this.text.length,
        ClickableSpan::class.java
    )
    spansCount.forEach { clickableSpan ->
        this.text.removeSpan(clickableSpan)
    }
}

fun View.animateFlying(
    leftDirection: Boolean,
    radius: Int = 500,
    durationMills: Long,
    durationMillsToStartNext: Long? = null,
    startNextCallback: () -> Unit = {},
    onAnimationEnd: () -> Unit = {}
) {
    val x = 0f
    val y = 0f
    val angle = 25f
    val path = Path().apply {
        if (leftDirection) {
            arcTo(RectF(x - 2 * radius, y - radius, x, y + radius), 0f, -angle)
        } else {
            arcTo(RectF(x, y - radius, x + 2 * radius, y + radius), 180f, angle)
        }
    }
    if (durationMillsToStartNext != null) {
        doDelayed(durationMillsToStartNext) {
            startNextCallback.invoke()
        }
    }
    ObjectAnimator.ofFloat(this, View.X, View.Y, path).apply {
        duration = durationMills
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) = Unit
            override fun onAnimationCancel(animation: Animator) = Unit
            override fun onAnimationRepeat(animation: Animator) = Unit
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd.invoke()
            }
        })
        start()
    }
}

fun View.animateScale(
    durationMills: Long,
    startScale: Float,
    endScale: Float,
    onAnimationEnd: () -> Unit = {}
) {
    ValueAnimator.ofFloat(startScale, endScale).apply {
        duration = durationMills
        addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            scaleX = animatedValue
            scaleY = animatedValue
        }
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) = Unit
            override fun onAnimationCancel(animation: Animator) = Unit
            override fun onAnimationStart(animation: Animator) = Unit
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd.invoke()
            }
        })
        start()
    }
}

fun View.animateFading(
    durationMills: Long,
    endAlpha: Float,
    reverse: Boolean = false,
    onAnimationEnd: () -> Unit = {}
) {
    ObjectAnimator.ofFloat(this, View.ALPHA, alpha, endAlpha).apply {
        duration = durationMills
        if (reverse) {
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
        }
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) = Unit
            override fun onAnimationCancel(animation: Animator) = Unit
            override fun onAnimationStart(animation: Animator) = Unit
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd.invoke()
            }
        })
        start()
    }
}

fun ImageView.setImageDrawable(@DrawableRes drawableRes: Int) {
    this.setImageDrawable(ContextCompat.getDrawable(this.context, drawableRes))
}

fun ImageView.setImageDrawable(fragment: Fragment, @DrawableRes drawableRes: Int) {
    this.setImageDrawable(fragment.context.getDrawableCompat(drawableRes))
}

fun View.fadeIn(durationMillis: Long) {
    this.startAnimation(AlphaAnimation(0F, 1F).apply {
        duration = durationMillis
        fillAfter = true
    })
}

fun View.fadeOut(durationMillis: Long) {
    this.startAnimation(AlphaAnimation(1F, 0F).apply {
        duration = durationMillis
        fillAfter = true
    })
}

fun View.standUpAnimation(durationMs: Long, completeCallback: (() -> Unit)? = null) {
    onMeasured {
        val x = (this.width - this.paddingLeft - this.paddingRight) / 2
        val y = this.height

        this.pivotX = x.toFloat()
        this.pivotY = y.toFloat()

        ObjectAnimator
            .ofFloat(this, ROTATION_X_PROPERTY, STAND_DOWN_ROTATION_POSITION, STAND_UP_ROTATION_POSITION).apply {
                interpolator = FastOutSlowInInterpolator()
                duration = durationMs
                reverse()
            }.start()

        ObjectAnimator
            .ofFloat(this, ALPHA_PROPERTY, 0f, 1f).apply {
                interpolator = FastOutSlowInInterpolator()
                duration = durationMs
                doOnEnd {
                    completeCallback?.invoke()
                }
            }.start()
    }
}

fun View.standDownAnimation(durationMs: Long, completeCallback: (() -> Unit)? = null) {
    val x = (this.width - this.paddingLeft - this.paddingRight) / 2
    val y = this.height

    this.pivotX = x.toFloat()
    this.pivotY = y.toFloat()

    ObjectAnimator
        .ofFloat(this, ROTATION_X_PROPERTY, this.rotationX, STAND_DOWN_ROTATION_POSITION).apply {
            interpolator = FastOutSlowInInterpolator()
            duration = durationMs
            reverse()
        }.start()

    ObjectAnimator
        .ofFloat(this, ALPHA_PROPERTY, 1f, 0f).apply {
            interpolator = FastOutSlowInInterpolator()
            duration = durationMs
            doOnEnd {
                completeCallback?.invoke()
            }
        }.start()
}


const val GLIDE_THUMBNAIL_SIZE_MULTIPLIER = 0.01f

private val optionsError: RequestOptions =
    RequestOptions().error(R.drawable.ic_error_alert_red_rectangle)

private val drawableCrossFadeFactory: DrawableCrossFadeFactory by lazy {
    DrawableCrossFadeFactory.Builder()
        .setCrossFadeEnabled(true)
        .build()
}

/**
 * Оптимизированный transition для Glide исправляющий мерцания при кроссфейде
 */
val glideCrossFadeTransition = DrawableTransitionOptions.with(
    PaddingTransitionFactory(
        drawableCrossFadeFactory
    )
)

fun Context?.isValidContextForGlide(): Boolean {
    if (this == null) {
        return false
    }
    if (this is Activity) {
        if (this.isDestroyed || this.isFinishing) {
            return false
        }
    }
    return true
}

fun ImageView.glideClear() =
    Glide.with(this.context.applicationContext)
        .clear(this)

fun ImageView.release() {
    setImageDrawable(null)
    setImageBitmap(null)
    setBackgroundResource(android.R.color.transparent)
}
fun ImageView.loadGlideWithOptions(path: Any?, requestOptions: RequestOptions) {
    if (!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .apply(requestOptions)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)

}
fun ImageView.loadGlide(path: Any?) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.loadGlideWithCallback(path: Any?, onFinished: () -> Unit) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade())
        .addListener(object : RequestListener<Drawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onFinished.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onFinished.invoke()
                return false
            }

        })
        .into(this)
}

fun ImageView.loadGlideWithPositioning(
    path: Any?,
    positionY: Double?,
    positionX: Double?,
    isNeedToFitHorizontal: Boolean = false,
    onFinished: () -> Unit = {}
) {
    if(!this.context.isValidContextForGlide()) return

    val defaultPaint = Paint(Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)

    val defaultShift = 0.5f
    val relativeShiftDiff = 0.5f

    Glide.with(this.context)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade())
        .transform(object : Transformation<Bitmap> {
            override fun updateDiskCacheKey(messageDigest: MessageDigest) = Unit

            override fun transform(
                context: Context,
                resource: Resource<Bitmap>,
                outWidth: Int,
                outHeight: Int
            ): Resource<Bitmap> {
                val shiftY = if (positionY != null) positionY + relativeShiftDiff else defaultShift
                val shiftX = if (positionX != null) positionX + relativeShiftDiff else defaultShift
                val matrix = Matrix()
                val inBitmap = resource.get()
                val mediaWidth = inBitmap.width
                val mediaHeight = inBitmap.height

                var dy = 0f
                var dx = 0f
                val xScale: Float
                val yScale: Float

                var isVerticalMedia = false
                var isHorizontalMedia = false

                if (mediaWidth == mediaHeight) {
                    isVerticalMedia = outWidth > outHeight
                    isHorizontalMedia = outWidth < outHeight && isNeedToFitHorizontal
                } else {
                    val widthAspect = outWidth.toDouble() / mediaWidth.toDouble()
                    val heightAspect = outHeight.toDouble() / mediaHeight.toDouble()

                    if (widthAspect > heightAspect) {
                        isVerticalMedia = (outWidth * mediaHeight / mediaWidth) > outHeight
                    }

                    if (widthAspect < heightAspect) {
                        isHorizontalMedia = ((outHeight * mediaWidth / mediaHeight) > outWidth) && isNeedToFitHorizontal
                    }
                }

                when {
                    isVerticalMedia && !isHorizontalMedia -> {
                        yScale = outWidth.toFloat() / mediaWidth.toFloat()
                        xScale = 1f
                        dy = (outHeight - mediaHeight * yScale) * shiftY.toFloat()
                    }
                    !isVerticalMedia && isHorizontalMedia -> {
                        xScale = outHeight.toFloat() / mediaHeight.toFloat()
                        yScale = 1f
                        dx = (outWidth - mediaWidth * xScale) * shiftX.toFloat()
                    }
                    else -> {
                        xScale = outHeight.toFloat() / mediaHeight.toFloat()
                        yScale = xScale
                    }
                }

                matrix.setScale(xScale, yScale)
                matrix.postTranslate(dx, dy)

                val outBitmap = Glide.get(context).bitmapPool.get(outWidth, outHeight, inBitmap.config)
                outBitmap.setHasAlpha(inBitmap.hasAlpha())

                val canvas = Canvas(outBitmap)
                canvas.drawBitmap(inBitmap, matrix, defaultPaint)
                canvas.setBitmap(null)

                val resultResource = BitmapResource.obtain(outBitmap, Glide.get(context).bitmapPool)

                return resultResource ?: resource
            }
        })
        .addListener(object : RequestListener<Drawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onFinished.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onFinished.invoke()
                return false
            }

        })
        .into(this)
}

fun ImageView.loadGlideFullSize(path: Any?) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .override(Target.SIZE_ORIGINAL)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.loadGlideFullSizeCircle(path: Any?) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .override(Target.SIZE_ORIGINAL)
        .apply(RequestOptions.circleCropTransform())
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.loadGlideCenterCrop(path: Any?){
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.loadGlideCenterCropNoFade(path: Any?){
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .centerCrop()
        .into(this)
}

fun ImageView.loadGlideFitCenter(path: Any?) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .fitCenter()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}


fun ImageView.loadGlideWithOptions(path: Any?, options: List<RequestOptions>) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .apply {
            options.forEach { apply(it) }
        }
        .into(this)
}

fun ImageView.loadGlideWithOptionsAndCallback(path: Any?, options: List<RequestOptions>, onFinished: () -> Unit) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .apply {
            options.forEach { apply(it) }
        }
        .addListener(object : RequestListener<Drawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onFinished.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onFinished.invoke()
                return false
            }

        })
        .into(this)
}

fun ImageView.loadGifWithOptions(path: Any?, options: List<RequestOptions>) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .asGif()
        .load(path)
        .apply {
            options.forEach { apply(it) }
        }
        .into(this)
}

fun ImageView.loadGlideWithCache(path: String?) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade())
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.loadGlideProgressive(path: Any?) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .thumbnail(GLIDE_THUMBNAIL_SIZE_MULTIPLIER)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.loadGlideWithCacheAndError(path: String?, error: RequestOptions = optionsError) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(error)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.loadGlideWithCacheAndError(bitmap: Bitmap?, error: RequestOptions = optionsError) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(bitmap)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(error)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.loadGlideWithCacheAndErrorPlaceHolder(path: String?, error: RequestOptions = optionsError) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(error)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.loadGlideRoundedCorner(path: Any?, radius: Int) {
    if(!this.context.isValidContextForGlide()) return

    var requestOptions = RequestOptions()
    requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(radius.dp))
    Glide.with(this.context)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(requestOptions)
        .into(this)
}

fun ImageView.loadGlideWithPlaceholder(path: Any?, @DrawableRes res: Int) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(res)
        .into(this)
}

fun ImageView.loadGlideCircle(path: Any?) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .apply(RequestOptions.circleCropTransform())
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.loadGlideCircleWithPlaceHolder(path: Any?, @DrawableRes placeholderResId: Int) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(this.context)
        .load(path)
        .apply(RequestOptions.circleCropTransform())
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(placeholderResId)
        .into(this)
}

fun ImageView.loadGlideProgress(path: Any?) {
    if(!this.context.isValidContextForGlide()) return

    Glide.with(context)
        .load(path)
        .placeholder(context.createProgressBar())
        .into(this)
}

fun ImageView.loadGlideGifWithCallback(
    path: Any?,
    onReady: ((drawable: Drawable?) -> Unit)? = null,
    onError: (() -> Unit)? = null
) = Glide.with(this)
    .load(path)
    .listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            onError?.invoke()
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            onReady?.invoke(resource)
            return false
        }
    })
    .into(this)

fun ImageView.loadGlideWithCallback(
    path: Any?,
    options: Array<Transformation<Bitmap>> = arrayOf(),
    onReady: ((bitmap: Bitmap?) -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    runCatching {
        Glide.with(this)
            .asBitmap()
            .load(path)
            .transform(*options)
            .transition(BitmapTransitionOptions.withCrossFade())
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onError?.invoke()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onReady?.invoke(resource)
                    return false
                }
            })
            .into(this)
    }.onFailure {
        onError?.invoke()
    }
}

fun ImageView.glideProgressCallback(
    path: Any?,
    onReady: ((bitmap: Bitmap?) -> Unit)? = null,
    onError: (() -> Unit)? = null
) =
    Glide.with(this)
        .asBitmap()
        .load(path)
        .placeholder(this.context.createProgressBar())
        .transition(BitmapTransitionOptions.withCrossFade())
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                onError?.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onReady?.invoke(resource)
                return false
            }
        })
        .into(this)


fun ImageView.glideCircleProgressCallback(
    path: Any?,
    onReady: ((bitmap: Bitmap?) -> Unit)? = null,
    onError: (() -> Unit)? = null
) =
    Glide.with(this)
        .asBitmap()
        .load(path)
        .placeholder(this.context.createProgressBar())
        .apply(RequestOptions.circleCropTransform())
        .transition(BitmapTransitionOptions.withCrossFade())
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                onError?.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onReady?.invoke(resource)
                return false
            }
        })
        .into(this)

fun ImageView.glideDrawable(
    path: Any?,
    onReady: ((drawable: Drawable?) -> Unit)? = null,
    onError: (() -> Unit)? = null
) =
    Glide.with(this)
        .load(path)
        .placeholder(context.createProgressBar())
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onError?.invoke()
                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onReady?.invoke(resource)
                return true
            }
        })
        .submit()

fun ImageView.loadGlideCustomSize(path: Any?, width: Int, height: Int) =
    Glide.with(this.context)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(RequestOptions().override(width, height))
        .into(this)

fun ImageView.loadGlideCustomSizeAsBitmap(path: Any?, width: Int, height: Int) =
    Glide.with(this.context)
        .asBitmap()
        .load(path)
        .transition(BitmapTransitionOptions.withCrossFade())
        .apply(RequestOptions().override(width, height))
        .into(this)

fun ImageView.loadBitmap(
    path: Any?,
    onReady: ((bitmap: Bitmap?) -> Unit)
) = loadBitmap(path, onReady, null)

fun ImageView.loadBitmap(
    path: Any?, onReady: ((bitmap: Bitmap?) -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    if (!this.context.isValidContextForGlide()) return
    Glide.with(this)
        .asBitmap()
        .load(path)
        .placeholder(context.createProgressBar())
        .transition(BitmapTransitionOptions.withCrossFade())
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                onError?.invoke()
                return true
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onReady?.invoke(resource)
                return true
            }
        })
        .submit()
}

fun ImageView.setTint(@ColorRes colorRes: Int) {
    setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_ATOP)
}

fun Drawable.setTintColor(context: Context, colorResId: Int) {
    DrawableCompat.setTint(
        DrawableCompat.wrap(this).mutate(),
        ContextCompat.getColor(context, colorResId)
    )
}

fun ImageView.applyAspectRation(
    parentWidth: Int,
    aspect: Double,
    makeInvisibleBefore: Boolean = false
) {
    if (makeInvisibleBefore) invisible()

    layoutParams = when (layoutParams) {
        is ConstraintLayout.LayoutParams -> ConstraintLayout.LayoutParams(parentWidth, (parentWidth / aspect).toInt())
        is FrameLayout.LayoutParams -> FrameLayout.LayoutParams(parentWidth, (parentWidth / aspect).toInt())
        else -> ViewGroup.LayoutParams(parentWidth, (parentWidth / aspect).toInt())
    }

    if (makeInvisibleBefore) visible()
}

fun ImageView.changeSize(height: Int, width: Int): ImageView {
    layoutParams?.height = height
    layoutParams?.width = width

    requestLayout()

    return this
}

fun ImageView.setDrawable(@DrawableRes id: Int) {
    context?.let { ctx ->
        setImageDrawable(
            ContextCompat.getDrawable(ctx, id)
        )
    }
}

fun TextView.textColor(colorRes: Int) {
    setTextColor(ContextCompat.getColor(context, colorRes))
}

fun TextView.drawableTint(colorRes: Int) {
    this.compoundDrawablesRelative.forEach { drawable ->
        drawable?.let {
            DrawableCompat.setTint(
                DrawableCompat.wrap(drawable).mutate(),
                ContextCompat.getColor(context, colorRes)
            )
        }
    }
}

fun TextView.underLine() {
    paint.flags = paint.flags or Paint.UNDERLINE_TEXT_FLAG
    paint.isAntiAlias = true
}

fun TextView.deleteLine() {
    paint.flags = paint.flags or Paint.STRIKE_THRU_TEXT_FLAG
    paint.isAntiAlias = true
}

fun TextView.bold() {
    paint.isFakeBoldText = true
    paint.isAntiAlias = true
}

fun TextView.font(@FontRes font: Int) {
    typeface = ResourcesCompat.getFont(context, font)
}

fun EditText.editorAction(action: (v: TextView, actionId: Int, event: KeyEvent?) -> Boolean) {
    setOnEditorActionListener { v, actionId, event -> action(v, actionId, event) }
}

fun TextView.setColorFilterForDrawable(
    context: Context,
    @ColorRes colorRes: Int,
    mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN
) {
    this.compoundDrawables.forEach {
        it?.colorFilter = PorterDuffColorFilter(context.color(colorRes), mode)
    }
}

fun TextView.setDrawableTint(context: Context, @ColorRes colorRes: Int) {
    this.compoundDrawablesRelative.forEach {
        it?.let { d -> DrawableCompat.setTint(DrawableCompat.wrap(d).mutate(), context.color(colorRes)) }
    }
}

fun TextView.setHtmlText(html: String) {
    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)
    }
}

fun TextView.setDrawables(
    left: Drawable? = null,
    right: Drawable? = null,
    top: Drawable? = null,
    bottom: Drawable? = null
) {
    this.setCompoundDrawables(left, top, right, bottom)
}

fun TextView.addClickForRange(
    range: IntRange, @ColorRes color: Int,
    onClickListener: () -> Unit
): SpannableStringBuilder {

    val t = SpannableStringBuilder(this.text)

    apply {
        val clickableSpan =
            object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onClickListener()
                }

                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }

        t.color(this.context.color(color), range)
        t.setSpanExclusive(clickableSpan, range)
    }

    this.text = t
    return t
}

fun <T> TextView.clickSpannable(
    data: T,
    range: IntRange,
    @ColorRes color: Int,
    listener: (T) -> Unit
): SpannableStringBuilder {

    val t = SpannableStringBuilder(this.text)

    apply {
        val clickableSpan =
            object : ClickableSpan() {
                override fun onClick(p0: View) {
                    listener(data)
                }

                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }

        t.color(this.context.color(color), range)
        t.setSpanExclusive(clickableSpan, range)
    }

    this.text = t
    return t
}

/**
 * Glide extension to prevent flicker upon image update in target ImageView
 * by setting result drawable manually
 */
fun RequestManager.preloadAndSet(url: String, imageView: ImageView) {
    this.load(url)
        .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                imageView.setImageDrawable(resource)
                (resource as? GifDrawable)?.let {
                    it.stop()
                    it.start()
                }
                return true
            }
        })
        .preload()
}

fun RequestManager.loadBlocking(url: String, options: RequestOptions? = null): Bitmap? {
    return runCatching {
        asBitmap()
            .load(url)
            .apply { options?.let(::apply) }
            .submit()
            .get()
    }.getOrNull()
}

fun EditText.checkCorrectSelection() {
    if (selectionEnd < text?.length ?: 0) setSelection(text?.length ?: 0)
}

fun TextView.hideIfNull(text: String?) {
    if (text == null) {
        gone()
        return
    }
    this.text = text
    visible()
}

fun TextView.hideIfNullOrEmpty(text: String?) {
    if (text.isNullOrEmpty()) {
        gone()
        return
    }
    this.text = text
    visible()
}

fun TextView.setTextStyle(@StyleRes style: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(style)
    } else {
        setTextAppearance(context, style)
    }
}

fun View.showScaleUp(startDelay: Long = 0) {
    this.visible()

    this.clearAnimation()

    this.animate()
        .scaleX(VISIBLE_SCALE)
        .scaleY(VISIBLE_SCALE)
        .setStartDelay(startDelay)
        .setDuration(ANIM_DURATION_SHOW)
        .setListener(null)
        .start()
}

fun View.hideScaleDown() {
    this.clearAnimation()

    this.animate()
        .scaleX(INVISIBLE_SCALE)
        .scaleY(INVISIBLE_SCALE)
        .setDuration(ANIM_DURATION_HIDE)
        .setListener(onAnimationEnd = {
            this.gone()
        })
        .start()
}

fun View.getYRelativeToParent(parentView: View): Float {
    return if (parent == parentView) {
        y
    } else {
        y + ((parent as? View)?.getYRelativeToParent(parentView) ?: 0f)
    }
}

fun View.getXRelativeToParent(parentView: View): Float {
    return if (parent == parentView) {
        x
    } else {
        x + ((parent as? View)?.getXRelativeToParent(parentView) ?: 0f)
    }
}

@SuppressLint("ClickableViewAccessibility")
fun View.setOnActionMoveListener(listener: (Boolean) -> Unit) {
    setOnTouchListener { v, event ->
        listener(event.action == MotionEvent.ACTION_MOVE)
        v.onTouchEvent(event)
        return@setOnTouchListener true
    }
}

fun TextView.setDrawable(
    start: Drawable? = null,
    top: Drawable? = null,
    end: Drawable? = null,
    bottom: Drawable? = null,
    drawablePadding: Int = 0
) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(
        start,
        top,
        end,
        bottom
    )
    if (drawablePadding != 0) {
        this.compoundDrawablePadding = drawablePadding.dp
    }
}

fun TextView.clearDrawables() {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(
        null,
        null,
        null,
        null
    )
}

fun Activity.keepScreenOnEnable() =
    this.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

fun Activity.keepScreenOnDisable() =
    this.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

@SuppressLint("ClickableViewAccessibility")
inline fun TextView.setDrawableClickListener(
    fuzz: Int = DEFAULT_DRAWABLE_FUZZ.dp,
    crossinline clickListener: (view: View?, drawableIndex: Int) -> Unit
) {
    this.setOnTouchListener(object : CompoundDrawableClickListener(fuzz) {
        override fun onDrawableClick(v: View?, drawableIndex: Int) = clickListener.invoke(v, drawableIndex)
    })
}

fun EditText.textChanges(): Flow<CharSequence?> {
    return callbackFlow {
        checkMainThread()
        val listener = doOnTextChanged { text, _, _, _ -> trySend(text) }
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}

fun View.animateHeightFromTo(initialHeight: Int, finalHeight: Int, duration: Long, pivot: Float) {
    val animator = ValueAnimator.ofInt(initialHeight, finalHeight)
    pivotY = pivot
    animator.duration = duration
    animator.addUpdateListener {
        val value = it.animatedValue as Int
        val layoutParamsAnim = layoutParams
        layoutParamsAnim.height = value
        layoutParams = layoutParamsAnim
        isVisible = value != 0
    }
    animator.start()
}

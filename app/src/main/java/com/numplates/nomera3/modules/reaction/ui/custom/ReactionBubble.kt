package com.numplates.nomera3.modules.reaction.ui.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Point
import android.graphics.PointF
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import com.meera.core.extensions.applyRoundedOutline
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.lightVibrate
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.standDownAnimation
import com.meera.core.extensions.standUpAnimation
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ReactionBubleBinding
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.reaction.data.ReactionType
import kotlin.math.abs

const val REACTION_ANIMATION_DURATION_MS = 200L
private const val STAND_UP_DOWN_ANIMATION_DURATION_MS = 200L
private const val CLOSE_DELAY = 300L
private const val ACTIVE_DRAG_AREA_MULTIPLIER = 2.5f
private const val ADDITIONAL_FINGER_AREA = 30
private const val HALF_MULTIPLIER = 1.5f
private val INCREASE_SIZE_VERTICAL_OFFSET_DP = 30.dp

@SuppressLint("ViewConstructor")
class ReactionBubble private constructor(
    private val container: ViewGroup,
    // Флаг - показывать ли иконки утра и вечера
    private val showMorningEvening: Boolean = true,
    private val selectListener: (ReactionType) -> Unit,
    private val hideListener: () -> Unit,
) : FrameLayout(container.context) {

    init {
        disableClipOnParents(container)
    }

    private var touchEnabled = true

    /**
     * Флаг - надо ли приподнимать иконку реакции. Текущая верстка [ReactionBubble] такая, что надо
     */
    private var isMoveUpAnimationEnabled = true

    /**
     * Размер до которого увеличивается вью реакции
     */
    private var bigSize = container.resources.getDimensionPixelSize(R.dimen.reaction_bar_increased_item_size)

    /**
     * Стандартный размер вью реакции
     */
    private var stdSize = container.resources.getDimensionPixelSize(R.dimen.reaction_bar_item_size)

    /** Так как вьюха с реакцией увеличивается, то нужно сдвигать эту вью вверх или вниз по оси У,
     * чтобы нижний край вью оставался на месте (см. видео в документации как надувается вью с
     * с реакцией):
     * 1. если нужно сдвигать вниз по оси Y, то deltaY = stdSize - bigSize
     * 2. если нужно сдвигать вверх по оси Y, то deltaY = bigSize - stdSize
     * 3. если верстка сделана так, что вью с реакцией внутри контейнера (например, FrameLayout) с
     * атрибутом gravity == "center_horizontal|bottom", то сдвигать вью по оси Y не нужно. Проблема
     * сдвига решается в этом случае на уровне верстки, поэтому ставим
     * isMoveUpAnimationEnabled = false
     */
    private var deltaY = bigSize.unaryMinus()

    /**
     * Size multiplier for the touchable area below the reaction bubble.
     *
     * Releasing the touch within this area will not dismiss the ReactionBubble
     * (unless it's a click instead of a touch or a reaction was successfully selected)
     *
     * ReactionBubble itself is also within the active area.
     */
    private val activeAreaHeightMultiplier: Float = ACTIVE_DRAG_AREA_MULTIPLIER

    private val dragTouchSlopSquared: Int

    private val originalDownPosition: PointF = PointF()
    private var isDragging: Boolean = false
    private var receivedInitialTouchEvents = false

    private var selectedReaction: View? = null

    /**
     * [ReactionBubble.currentAnimator] сеттится в методе [ReactionBubble.animateAllReactionView] каждый раз,
     * когда меняется [ReactionBubble.selectedReaction]
     */
    private var currentAnimator: ValueAnimator? = null
        set(value) {
            field?.cancel()
            field = value
            field?.start()
        }

    private var ViewGroup.LayoutParams.size: Int
        get() = width
        set(value) {
            width = value
            height = value
        }

    private val binding: ReactionBubleBinding =
        ReactionBubleBinding.inflate(LayoutInflater.from(context), this, true)

    private val reactionViews = listOf(
        binding.lottie1,
        binding.lottie2,
        binding.lottie3,
        binding.lottie4,
        binding.lottie5,
        binding.lottie6,
        binding.lottie7,
        binding.lottie8,
        binding.lottie9
    )

    /** Начальное положение баббла реакций на экране по оси Y */
    private var initialReactionsBubblePositionY = 0

    /** Начальная высота баббла реакций на экране */
    private var initialReactionsBubbleHeight = 0

    /** Обработчик скролла плашки реакций */
    private val throttleSmoothScroller: ThrottleSmoothScroller

    /** Отлавливатель лонгтапа(чтобы знать, когда просто скроллим баббл, а когда анимируем увеличение реакции) */
    private val gestureLongTapDetector: GestureDetector

    init {
        mapDataWithViews()
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        dragTouchSlopSquared = touchSlop * touchSlop
        onMeasured {
            initialReactionsBubblePositionY = location(this).y - INCREASE_SIZE_VERTICAL_OFFSET_DP
            initialReactionsBubbleHeight = height
        }
        binding.scrollBubbleContainer.applyRoundedOutline(top = bigSize * -1)
        val scrollView = binding.scrollBubbleContainer
        throttleSmoothScroller = ThrottleSmoothScroller(scrollView)
        gestureLongTapDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(event: MotionEvent) {
                isMoveUpAnimationEnabled = true
                selectAndScrollReaction(event)
            }

            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                val sReaction = getReactionViewUnderFinger(event.rawX.toInt(), event.rawY.toInt())
                val reactionType = sReaction?.tag as? ReactionType?
                reactionType?.let {
                    context?.lightVibrate()
                    selectedReaction = sReaction
                    selectListener.invoke(reactionType)
                }
                return super.onSingleTapConfirmed(event)
            }
        })
    }

    private var startEvent: Point? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (touchEnabled.not()) return true
        gestureLongTapDetector.onTouchEvent(event)
        if (!isMoveUpAnimationEnabled) {
            binding.scrollBubbleContainer.bypassTouchEventThroughBlock(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleActiveAreaDragging(event)
                selectAndScrollReaction(event)
                startEvent = Point(event.x.toInt(), event.y.toInt())
            }

            MotionEvent.ACTION_MOVE -> {
                if (startEvent == null) {
                    startEvent = Point(event.x.toInt(), event.y.toInt())
                } else {
                    if (moveMoreFinger(event)) {
                        handleActiveAreaDragging(event)
                        selectAndScrollReaction(event)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                val reactionType = selectedReaction?.tag as? ReactionType?
                val isReactionLayoutUnderFinger = isActiveAreaUnderFinger(
                    x = event.rawX.toInt(),
                    y = event.rawY.toInt()
                )
                if (reactionType != null && isReactionLayoutUnderFinger) {
                    selectListener.invoke(reactionType)
                }
                setSelectedReaction(null, animateAllReactionView = true, isActionUp = true)
                val isDraggingStateValid = receivedInitialTouchEvents && isDragging.not()
                if (!isReactionLayoutUnderFinger || isDraggingStateValid) {
                    hide()
                    hideListener.invoke()
                }
                clearDraggingFlags()

                isMoveUpAnimationEnabled = false
                startEvent = null
            }

            MotionEvent.ACTION_CANCEL -> {
                setSelectedReaction(null)
                clearDraggingFlags()

                isMoveUpAnimationEnabled = false
            }
        }
        return true
    }

    fun hide(useDelay: Boolean = true) {
        disableTouch()
        if (useDelay) {
            doDelayed(CLOSE_DELAY) {
                standDownAnimation(STAND_UP_DOWN_ANIMATION_DURATION_MS) {
                    container.removeView(this)
                }
            }
        } else {
            container.removeView(this)
        }
    }

    private fun mapDataWithViews() {
        val reactions = ReactionType.currentValues(showMorningEvening)
        val isMorningEveningContains = reactions.contains(ReactionType.Evening) || reactions.contains(ReactionType.Morning)
        binding.lottie9.isVisible = showMorningEvening && isMorningEveningContains
        reactionViews.forEachIndexed { index, lottieAnimationView ->
            if (reactions.size == index) return@forEachIndexed
            val reactionType = reactions[index]
            if (showMorningEvening && isMorningEveningContains) {
                lottieAnimationView.isVisible = true
            }
            lottieAnimationView.tag = reactionType
            lottieAnimationView.setAnimation(reactionType.resourceNoBorder)
        }
    }

    private fun disableTouch() {
        touchEnabled = false
    }

    private fun enableTouch() {
        touchEnabled = true
    }

    /**
     * Manage variables connected to touch handling within expanded active area below reaction bubble.
     */
    private fun handleActiveAreaDragging(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                receivedInitialTouchEvents = true
                isDragging = false
                originalDownPosition.set(event.rawX, event.rawY)
            }

            MotionEvent.ACTION_MOVE -> {
                receivedInitialTouchEvents = true
                if (isDragging) return
                val deltaX = event.rawX - originalDownPosition.x
                val deltaY = event.rawY - originalDownPosition.y
                if (deltaX * deltaX + deltaY * deltaY > dragTouchSlopSquared) isDragging = true
            }
        }
    }

    private fun clearDraggingFlags() {
        isDragging = false
        receivedInitialTouchEvents = false
        originalDownPosition.set(0f, 0f)
    }

    /**
     * Когда пользователь нажимает пальцем или водит пальцем в контейнере с реациями,
     * то нажатия или перемешение курсора отлавливается в onTouchEvent ниже, и в
     * [ReactionBubble.setSelectedReaction] ставится нужная реакция
     * @param value выбранная реакция
     * @param animateAllReactionView нужно ли запускать анимацию увеличения
     */
    private fun setSelectedReaction(
        value: View?,
        animateAllReactionView: Boolean = false,
        isActionUp: Boolean = false
    ) {
        if (selectedReaction == value) return
        selectedReaction = value
        if (animateAllReactionView) {
            if (value != null || isActionUp) context?.lightVibrate()
            animateAllReactionView(selectedReaction)
        }
    }

    /**
     * Когда меняется переменная [ReactionBubble.selectedReaction], запускается [ReactionBubble.animateAllReactionView]:
     * 1. Рассчитываем (newViewSize) новые размеры каждой вьюхи для анимирования увеличения
     * (если палец на реакции) / уменьшения (если убрали палец с вью)
     * 2. Рассчитываем (newTranslationY) смещения по оси Y для каждой вьюхи
     * 3. устанавливаем новый [ReactionBubble.currentAnimator] который пробегается по списку вьюх с реакциями
     * и анимирует все вью
     */
    private fun animateAllReactionView(selectedView: View?) {
        val newViewSize: List<Pair<Int, Int>> = reactionViews.map { v ->
            v.layoutParams.size to if (v == selectedView) bigSize else stdSize
        }
        val newTranslationY: List<Pair<Int, Int>>? = if (isMoveUpAnimationEnabled) {
            reactionViews.map { v ->
                v.translationY.toInt() to if (v == selectedView) deltaY else 0
            }
        } else {
            null
        }
        currentAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = REACTION_ANIMATION_DURATION_MS
            addUpdateListener { animator: ValueAnimator? ->
                val progress = animator?.animatedValue as? Float ?: return@addUpdateListener
                reactionViews.forEachIndexed { i, v ->
                    val newSize = newViewSize.getOrNull(i)?.progressMove(progress)
                    val newTransY: Int? = if (isMoveUpAnimationEnabled) {
                        newTranslationY?.getOrNull(i)?.progressMove(progress)
                    } else {
                        null
                    }
                    if (newSize != null) {
                        val width = v.layoutParams.width
                        if (width != newSize) {
                            v.layoutParams.width = newSize
                            v.layoutParams.height = newSize
                        }
                    }
                    if (newTransY != null && isMoveUpAnimationEnabled) {
                        val translationY = v.translationY
                        val toFloat = newTransY.toFloat()
                        if (translationY != toFloat) {
                            v.translationY = toFloat
                        }
                    }
                    v.requestLayout()
                }
                disableClipOnParents(binding.reactionItemContainer)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (selectedView == reactionViews.last()) {
                        binding.scrollBubbleContainer.smoothScrollTo(reactionViews.last().x.toInt(), 0)
                    }
                }
            })
        }
    }

    private fun disableClipOnParents(v: View) {
        if (v.parent == null) {
            return
        }
        if (v is ViewGroup) {
            v.clipChildren = false
            v.clipToPadding = false
        }
        (v.parent as? View)?.let(::disableClipOnParents)
    }

    private fun progressMove(from: Int, to: Int, progress: Float): Int {
        return from + ((to - from) * progress).toInt()
    }

    private fun Pair<Int, Int>.progressMove(progress: Float): Int =
        progressMove(first, second, progress)

    private fun getReactionViewUnderFinger(x: Int, y: Int): View? {
        return reactionViews.lastOrNull { reactionView ->
            isViewUnderFinger(view = reactionView, x = x, y = y)
        }
    }

    private fun isViewUnderFinger(
        view: View,
        x: Int,
        y: Int,
        withActiveAreaHeightMultiplier: Boolean = false
    ): Boolean {
        val location = location(view)
        val locationFirst = location(binding.lottie1)
        val locationStart = if (locationFirst.x < 0 && !binding.lottie9.isVisible) {
            location.x + abs(locationFirst.x)
        } else {
            location.x
        }
        val locationEnd = if (locationFirst.x < 0) {
            location.x + view.width + abs(locationFirst.x)
        } else {
            location.x + view.width + ADDITIONAL_FINGER_AREA
        }
        val checkWidth = x in (locationStart until locationEnd)
        val fromHeight = initialReactionsBubblePositionY
        val areaMultiplier = if (withActiveAreaHeightMultiplier) activeAreaHeightMultiplier else 1f
        val areaHeight = (initialReactionsBubbleHeight * areaMultiplier).toInt() + view.height
        val checkHeight = y in (fromHeight until fromHeight + areaHeight)
        return checkWidth && checkHeight
    }

    private fun isActiveAreaUnderFinger(x: Int, y: Int): Boolean {
        return isViewUnderFinger(
            view = binding.scrollBubbleContainer,
            x = x,
            y = y,
            withActiveAreaHeightMultiplier = true
        )
    }

    private fun location(view: View): Point {
        val point = Point()
        val location = IntArray(2).also {
            view.getLocationOnScreen(it)
        }
        return point.apply {
            this.x = location[0]
            this.y = location[1]
        }
    }

    private fun checkAndScrollEdge(event: MotionEvent) {
        val scrollView = binding.scrollBubbleContainer
        val scrollViewXEnd = (scrollView.x + scrollView.width + ADDITIONAL_FINGER_AREA).toDouble()

        if (event.x in (scrollViewXEnd - stdSize * HALF_MULTIPLIER)..scrollViewXEnd) {
            throttleSmoothScroller.scroll(stdSize)
        } else if (event.x in scrollView.x..(scrollView.x + (stdSize * HALF_MULTIPLIER).toInt() +
                ADDITIONAL_FINGER_AREA)
        ) {
            throttleSmoothScroller.scroll(stdSize.unaryMinus())
        }
    }

    private fun moveMoreFinger(event: MotionEvent): Boolean {
        return abs((startEvent?.x ?: 0) - event.x) > (stdSize / 4) ||
            abs((startEvent?.y ?: 0) - event.y) > (stdSize / 4)
    }

    private fun selectAndScrollReaction(event: MotionEvent) {
        if (isMoveUpAnimationEnabled) {
            val selectedReaction = getReactionViewUnderFinger(event.rawX.toInt(), event.rawY.toInt())
            setSelectedReaction(selectedReaction, true)
            val isReactionLayoutUnderFinger = isActiveAreaUnderFinger(
                x = event.rawX.toInt(),
                y = event.rawY.toInt()
            )
            if (isReactionLayoutUnderFinger) {
                checkAndScrollEdge(event)
            }
            startEvent = Point(event.x.toInt(), event.y.toInt())
        }
    }

    companion object {

        fun show(
            position: Point,
            container: ViewGroup,
            isMoveUpAnimationEnabled: Boolean,
            contentActionBarType: ContentActionBar.ContentActionBarType,
            showMorningEvening: Boolean,
            selectListener: (ReactionType) -> Unit,
            hideListener: () -> Unit
        ): ReactionBubble {
            val bubbleInViewTree = container.children.find { it is ReactionBubble } as? ReactionBubble
            bubbleInViewTree?.setBubbleLayoutParams(position)
            if (bubbleInViewTree != null) {
                return bubbleInViewTree.apply {
                    enableTouch()
                }
            }
            bubbleInViewTree?.setBubbleColor(contentActionBarType)
            val bubble = ReactionBubble(
                container = container,
                showMorningEvening = showMorningEvening,
                selectListener = selectListener,
                hideListener = hideListener
            ).apply {
                setBubbleLayoutParams(position)
                setBubbleColor(contentActionBarType)
                container.addView(this)
                enableTouch()
                standUpAnimation(STAND_UP_DOWN_ANIMATION_DURATION_MS)
                setMoveUpAnimationEnabled(isMoveUpAnimationEnabled)
            }
            return bubble
        }

        private fun ReactionBubble.setBubbleColor(
            contentActionBarType: ContentActionBar.ContentActionBarType
        ) {
            when (contentActionBarType) {
                ContentActionBar.ContentActionBarType.DEFAULT -> {
                    binding.flBubbleContainer.backgroundTintList = null
                }
                ContentActionBar.ContentActionBarType.DARK -> {
                    binding.flBubbleContainer.setBackgroundTint(R.color.ui_dark_gray_background)
                }
                ContentActionBar.ContentActionBarType.BLUR -> {
                    val blurColor = ContextCompat.getColor(context, R.color.ui_blur_gray_background)
                    binding.flBubbleContainer.backgroundTintList = ColorStateList.valueOf(blurColor)
                }
            }
        }

        private fun ReactionBubble.setBubbleLayoutParams(position: Point) {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(position.x, position.y, 0, 0)
            }
        }

        private fun ReactionBubble.setMoveUpAnimationEnabled(isMoveUpAnimationEnabled: Boolean) {
            this.isMoveUpAnimationEnabled = isMoveUpAnimationEnabled
        }
    }
}

package com.numplates.nomera3.modules.reaction.ui.custom

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ReactionBottomMenuItemBinding
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterStyle
import com.numplates.nomera3.modules.reaction.ui.util.ReactionStyleMapper

const val SLIDE_UP_Y_AXIS_10 = -10

@SuppressLint("ViewConstructor")
class ReactionBottomMenuItem(
    context: Context,
    private val selectListener: (ReactionType) -> Unit
) : FrameLayout(context) {

    // флаг - надо ли приподнимать иконку реакции,
    // текущая верстка ReactionBubble такая, что
    // надо
    var isMoveUpAnimationEnabled = true

    // размер до которого увеличивается вью реакции
    private var bigSize = 48.dp

    // стандартный размер вью реакции
    private var stdSize = 32.dp

    // так как вьюха с реакцией увеличивается, то нужно сдвигать эту вью вверх или вниз по оси У,
    // чтобы нижний край вью оставался на месте (см. видео в документации как надувается вью с
    // с реакцией):
    // 1. если нужно сдвигать вниз по оси Y, то deltaY = stdSize - bigSize
    // 2. если нужно сдвигать вверх по оси Y, то deltaY = bigSize - stdSize
    // 3. если верстка сделана так, что вью с реакцией внутри контейнера (например, FrameLayout) с
    //    атрибутом gravity == "center_horizontal|bottom", то сдвигать вью по оси Y не нужно. Проблема
    //    сдвига решается в этом случае на уровне верстки, поэтому ставим
    //    isMoveUpAnimationEnabled = false

    // stdSize - bigSize значение не подходить к BottomSheetDialog-у,
    // так как значение очень большое и оно сдвигает вюхи(реакции) на большие расстояние
    private var deltaY = (SLIDE_UP_Y_AXIS_10).dp //stdSize - bigSize

    private val reactionCounterFormatter = ReactionCounterFormatter(
        context.getString(R.string.thousand_lowercase_label),
        context.getString(R.string.million_lowercase_label),
        oneAllow = true,
        thousandAllow = true
    )

    private val binding: ReactionBottomMenuItemBinding =
        ReactionBottomMenuItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    private val reactionViews = listOf(
        binding.lottie1,
        binding.lottie2,
        binding.lottie3,
        binding.lottie4,
        binding.lottie5,
        binding.lottie6,
        binding.lottie7,
        binding.lottie8
    )

    private val reactionLabels = listOf(
        binding.lottieLabel1,
        binding.lottieLabel2,
        binding.lottieLabel3,
        binding.lottieLabel4,
        binding.lottieLabel5,
        binding.lottieLabel6,
        binding.lottieLabel7,
        binding.lottieLabel8
    )

    private val reactionMapText = hashMapOf<ReactionType, TextView>()
    private val reactionMapView = hashMapOf<ReactionType, LottieAnimationView>()

    init {
        mapDataWithViews()
        // selectedReaction = присвоить выбранную реакцию (непонятно где взять)
    }

    private fun mapDataWithViews() {
        reactionViews.forEachIndexed { index, lottieView ->
            val reactionType = ReactionType.currentValues()[index]

            val reactionLabel = reactionLabels[index]
            val reactionView = reactionViews[index]

            reactionMapText[reactionType] = reactionLabel
            reactionMapView[reactionType] = reactionView
            lottieView.tag = reactionType
            reactionView.setAnimation(reactionType.resourceNoBorder)
        }
    }

    fun setReaction(reactions: List<ReactionEntity>) {
        resetViews()

        reactions.forEach { reaction ->
            val reactionType = ReactionType.getByString(reaction.reactionType)
            val reactionText: TextView? = reactionMapText[reactionType]
            val style = ReactionStyleMapper.map(reaction.isMine).style

            reactionText?.let {
                reactionText.text = reactionCounterFormatter.format(
                    value = reaction.count
                )

                TextViewCompat.setTextAppearance(reactionText, style)
            }
        }
    }

    private fun resetViews() {
        reactionLabels.forEach { reactionText ->
            reactionText.text = String.empty()
            TextViewCompat.setTextAppearance(reactionText, ReactionCounterStyle.Init.style)
        }
    }

    /*
    * Далее код для анимации вью с реакциями
    * */
    // когда пользователь нажимает пальцем или водит пальцем в контейнере с реациями,
    // то нажатия или перемешение курсора отлавливается в onTouchEvent ниже, и в
    // selectedReaction ставится нужная реакция и запускается анимация увеличения
    private var selectedReaction: View? = null
        set(value) {


            if (field == value) {
                return
            }

            field = value

            animateAllReactionView(field)
        }

    // currentAnimator сеттится в методе animateAllReactionView,
    // каждый раз когда меняется selectedReaction
    private var currentAnimator: ValueAnimator? = null
        set(value) {
            field?.cancel()
            field = value
            field?.start()
        }

    // ловим нажатия и перемещения пальца, через метод
    // getReactionViewUnderFinger определяем есть ли под
    // пальцем вью с реакцией
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                selectedReaction = getReactionViewUnderFinger(event.rawX, event.rawY)
            }
            MotionEvent.ACTION_UP -> {
                val reactionType = selectedReaction?.tag as? ReactionType?
                if (reactionType != null) {
                    selectListener.invoke(reactionType)
                    selectedReaction = null
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                selectedReaction = null
            }
        }

        return true
    }

    // когда меняется переменная selectedReaction, запускается этот метод animateAllReactionView:
    // 1. рассчитываем (newViewSize) новые размеры каждой вьюхи для анимирования увеличения / уменьшения
    // 2. рассчитываем (newTranslationY) смещения по оси Y для каждой вьюхи
    // 3. устанавливаем новый currentAnimator который пробегается по списку вьюх с реакциями
    //    и анимирует все вью
    private fun animateAllReactionView(selectedView: View?) {
        // 1. рассчитываем (newViewSize) новые размеры каждой вьюхи для
        // анимирования увеличения (если палец на реакции) / уменьшения
        // (если убрали палец с вью)
        val newViewSize: List<Pair<Int, Int>> = reactionViews.map {
            it.layoutParams.size to if (it == selectedView) bigSize else stdSize
        }

        // 2. рассчитываем (newTranslationY) смещения по оси Y для каждой вьюхи
        val newTranslationY: List<Pair<Int, Int>>? = if (isMoveUpAnimationEnabled) {
            reactionViews?.map {
                it.translationY.toInt() to if (it == selectedView) deltaY else 0
            }
        } else {
            null
        }

        // 3. устанавливаем новый currentAnimator который пробегается по списку вьюх с реакциями
        //    и анимирует все вью
        currentAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = REACTION_ANIMATION_DURATION_MS
            addUpdateListener { animator: ValueAnimator? ->
                val progress = animator?.animatedValue as? Float

                reactionViews.forEachIndexed { i, v ->
                    val newSize = newViewSize?.getOrNull(i)?.progressMove(progress!!)
                    val newTransY: Int? = if (isMoveUpAnimationEnabled) {
                        newTranslationY?.getOrNull(i)?.progressMove(progress!!)
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
                if (progress == 1f) {
                    disableClipOnParents(binding.reactionItemContainer)
                }
            }
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
        if (v.parent is View) {
            disableClipOnParents(v.parent as View)
        }
    }

    // метод для анимаций, исп в animateAllReactionView
    private var ViewGroup.LayoutParams.size: Int
        get() = width
        set(value) {
            width = value
            height = value
        }

    // метод для анимаций, исп в animateAllReactionView
    private fun progressMove(from: Int, to: Int, progress: Float): Int {
        return from + ((to - from) * progress).toInt()
    }

    // метод для анимаций, исп в animateAllReactionView
    private fun Pair<Int, Int>.progressMove(progress: Float): Int =
        progressMove(first, second, progress)

    // метод для анимаций, исп в animateAllReactionView
    private fun getReactionViewUnderFinger(x: Float, y: Float): View? {
        return reactionViews?.firstOrNull { reactionView: View ->
            val location = location(reactionView)
            val xView = location.x
            val yView = location.y

            x >= xView && x < xView + reactionView.width && y >= yView && y < yView + reactionView.height
        }
    }

    // метод для анимаций, исп в animateAllReactionView
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
}

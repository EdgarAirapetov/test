package com.numplates.nomera3.modules.redesign.fragments.main.map.weather

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.meera.core.utils.graphics.getScreenWidth
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewMapPointInfoBinding
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoScrollingTextUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoWidgetState
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoWidgetUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.WeatherUiModel
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.animateAlpha
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream


class MeeraMapPointInfoWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var uiModel = MapPointInfoWidgetUiModel(MapPointInfoWidgetState.Hidden)
    private val animationViewFileMap: MutableMap<LottieAnimationView, String?> = mutableMapOf()
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_map_point_info, this, true)
        .let(MeeraViewMapPointInfoBinding::bind)

    private var isFirstStart = true
    var isSingleLine = true
    init {
        gone()
    }

    fun setUiModel(
        uiModel: MapPointInfoWidgetUiModel
    ) {
        if (uiModel == this.uiModel) return
        when (uiModel.state) {
            is MapPointInfoWidgetState.Shown -> post { handleShownState(uiModel.state) }
            else -> Unit
        }
        handleStateChange(oldState = this.uiModel.state, newState = uiModel.state)
        this.uiModel = uiModel
    }

    fun setSingleLineWeather(isSingle: Boolean = false) {
        isSingleLine = isSingle
        val constraintLayout: ConstraintLayout = binding.layoutMapPointInfoExpanded.clViewMapPointExpanded
        val textView: LottieAnimationView = binding.layoutMapPointInfoExpanded.lavMapPointInfoWeatherAnimation
        val constraintSet = ConstraintSet().apply {
            clone(constraintLayout)
        }

        val state =  uiModel.state as? MapPointInfoWidgetState.Shown.ExtendedDetailed
        if (isSingle) {
            with(constraintSet) {
                connect(
                    textView.id,
                    ConstraintSet.BOTTOM,
                    R.id.tv_map_point_info_weather_description,
                    ConstraintSet.BOTTOM
                )
                connect(textView.id, ConstraintSet.TOP, R.id.tv_map_point_info_weather_description, ConstraintSet.TOP)
                connect(textView.id, ConstraintSet.END, R.id.tv_map_point_info_weather_description, ConstraintSet.START)
                applyTo(constraintLayout)
                binding.layoutMapPointInfoExpanded.tvMapPointInfoAddressPrimary.gone()
                binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScroll.gone()
                binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScrollSecondary.text =
                    "${state?.primaryAddress?.orEmpty()}, ${state?.secondaryAddress?.orEmpty()}"
            }
        } else {
            with(constraintSet) {
                clear(textView.id, ConstraintSet.BOTTOM)
                connect(textView.id, ConstraintSet.TOP, R.id.tv_map_point_info_address_primary, ConstraintSet.TOP)
                connect(textView.id, ConstraintSet.END, R.id.tv_map_point_info_weather_description, ConstraintSet.END)
                applyTo(constraintLayout)
            }

            binding.layoutMapPointInfoExpanded.tvMapPointInfoAddressPrimary.text = state?.primaryAddress
            binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScrollSecondary.text = state?.secondaryAddress.orEmpty()
            adjustTextSizeExpanded()
        }
        adjustTextSizeExpandedSecondary()
    }

    private fun handleStateChange(oldState: MapPointInfoWidgetState, newState: MapPointInfoWidgetState) {
        when {
            oldState != MapPointInfoWidgetState.Hidden && newState == MapPointInfoWidgetState.Hidden -> {
                animateExit(this)
            }

            oldState == MapPointInfoWidgetState.Hidden && newState != MapPointInfoWidgetState.Hidden -> {
                with(binding) {
                    layoutMapPointInfoExpanded.root.gone()
                    layoutMapPointInfoCollapsed.root.gone()
                    this@MeeraMapPointInfoWidget.visible()
                    when (newState) {
                        is MapPointInfoWidgetState.Shown.Collapsed -> animateEnter(layoutMapPointInfoCollapsed.root)
                        else -> animateEnter(layoutMapPointInfoExpanded.root)
                    }
                }
            }

            oldState is MapPointInfoWidgetState.Shown.Collapsed && newState !is MapPointInfoWidgetState.Shown.Collapsed -> {
                with(binding) {
                    animateEnter(layoutMapPointInfoExpanded.root)
                    animateExit(layoutMapPointInfoCollapsed.root)
                }
            }

            oldState !is MapPointInfoWidgetState.Shown.Collapsed && newState is MapPointInfoWidgetState.Shown.Collapsed -> {
                with(binding) {
                    animateEnter(layoutMapPointInfoCollapsed.root)
                    animateExit(layoutMapPointInfoExpanded.root)
                }
            }
        }
    }

    private fun animateExit(view: View) {
        view.animateAlpha(
            from = ALPHA_VISIBLE,
            to = ALPHA_INVISIBLE,
            duration = STATE_CHANGE_ANIMATION_DURATION_MS
        ) {
            view.alpha = ALPHA_VISIBLE
            view.gone()
        }
    }

    private fun animateEnter(view: View) {
        view.visible()
        view.animateAlpha(
            from = ALPHA_INVISIBLE,
            to = ALPHA_VISIBLE,
            duration = STATE_CHANGE_ANIMATION_DURATION_MS
        ) {
            view.alpha = ALPHA_VISIBLE
        }
    }

    private fun handleShownState(state: MapPointInfoWidgetState.Shown) {
        when (state) {
            is MapPointInfoWidgetState.Shown.Collapsed -> handleCollapsed(state)
            is MapPointInfoWidgetState.Shown.ExtendedDetailed -> handleShownExpandedDetailed(state)
            is MapPointInfoWidgetState.Shown.ExtendedGeneral -> handleShownExpandedGeneral(state)
        }
    }

    private fun handleCollapsed(state: MapPointInfoWidgetState.Shown.Collapsed) {
        with(binding.layoutMapPointInfoCollapsed) {
            tvMapPointInfoAddressPrimary.text = state.primaryAddress
            tvMapPointInfoTime.text = state.timeString
            ivMapPointInfoDivider.visible()
            tvMapPointInfoTime.visible()
            handleWeather(
                descriptionView = tvMapPointInfoWeatherDescription,
                animationView = lavMapPointInfoWeatherAnimation,
                weather = state.weather,
                isExpanded = false
            )
        }
        isFirstStart = false
        adjustTextScrollingCollapsed()
    }

    private fun handleShownExpandedGeneral(state: MapPointInfoWidgetState.Shown.ExtendedGeneral) {
        with(binding.layoutMapPointInfoExpanded) {
            mpistvMapPointInfoAddressScrollSecondary.text = state.primaryAddress
            val scrollUiModel = MapPointInfoScrollingTextUiModel(
                text = state.primaryAddress,
                textSizeSp = TEXT_SIZE_SMALL_SP
            )
            tvMapPointInfoAddressSecondary.setUiModel(scrollUiModel, false)
            tvMapPointInfoAddressSecondary.visible()
            ivMapPointInfoDivider.gone()
            tvMapPointInfoTime.gone()
            tvMapPointInfoWeatherDescription.gone()
            lavMapPointInfoWeatherAnimation.gone()
        }
    }

    private fun handleShownExpandedDetailed(state: MapPointInfoWidgetState.Shown.ExtendedDetailed) {
        with(binding.layoutMapPointInfoExpanded) {
            tvMapPointInfoAddressPrimary.text = state.primaryAddress
            mpistvMapPointInfoAddressScrollSecondary.text = state.secondaryAddress
            tvMapPointInfoTime.text = state.timeString
            handleWeather(
                descriptionView = tvMapPointInfoWeatherDescription,
                animationView = lavMapPointInfoWeatherAnimation,
                weather = state.weather,
                isExpanded = true
            )
            if (state.withMeeraLogo && state.showSecondaryAddress) {
                if (tvMapPointInfoAddressPrimary.isGone) {
                    mpistvMapPointInfoAddressScrollSecondary.text = "${state.primaryAddress}, ${state.secondaryAddress}"
                }
                tvMapPointInfoAddressSecondary.visible()
            }
            if(state.showSecondaryAddress) {
                tvMapPointInfoAddressSecondary.visible()
                tvMapPointInfoTime.visible()
                ivMapPointInfoDivider.visible()
                lavMapPointInfoWeatherAnimation.visible()
                tvMapPointInfoWeatherDescription.visible()
                adjustTextSizeExpandedSecondary()
            } else {
                val scrollUiModel = MapPointInfoScrollingTextUiModel(
                    text = state.primaryAddress,
                    textSizeSp = TEXT_SIZE_SMALL_SP
                )
                tvMapPointInfoAddressSecondary.setUiModel(scrollUiModel, false)
                tvMapPointInfoTime.gone()
                tvMapPointInfoWeatherDescription.gone()
                lavMapPointInfoWeatherAnimation.gone()
            }
        }
        isFirstStart = false
    }

    private fun handleWeather(
        descriptionView: TextView,
        animationView: LottieAnimationView,
        weather: WeatherUiModel?,
        isExpanded: Boolean
    ) {
        animationView.repeatCount = LottieDrawable.INFINITE
        if (weather != null) {
            val lastAnimationFilePath = animationViewFileMap[animationView]
            if (lastAnimationFilePath != weather.animation.absolutePath) {
                if (lastAnimationFilePath != null) {
                    animateViewExit(animationView) {
                        animateWeatherEnter(
                            animationView = animationView,
                            descriptionView = descriptionView,
                            animation = weather.animation
                        )
                    }
                } else {
                    animateWeatherEnter(
                        animationView = animationView,
                        descriptionView = descriptionView,
                        animation = weather.animation
                    )
                }
            }
            animationView.visible()
            if (!isExpanded) {
                descriptionView.text = weather.temperature
            } else {
                descriptionView.text = weather.description
            }
            descriptionView.visible()
        } else {
            descriptionView.gone()
            animationView.gone()
        }
    }

    private fun animateWeatherEnter(
        animationView: LottieAnimationView,
        descriptionView: TextView,
        animation: File
    ) {
        try {
            animationView.setAnimation(ZipInputStream(FileInputStream(animation)), null)
            animationView.frame = 0
            animateViewEnter(animationView) {
                animationViewFileMap[animationView] = animation.absolutePath
                animationView.playAnimation()
            }
        } catch (t: Throwable) {
            Timber.e(t)
            animationViewFileMap[animationView] = null
            descriptionView.gone()
            animationView.gone()
        }
    }

    private fun animateViewExit(
        view: View,
        onAnimationEnd: () -> Unit = {}
    ) {
        ValueAnimator.ofFloat(WEATHER_SCALE_ENTER, WEATHER_SCALE_EXIT).apply {
            duration = WEATHER_EXIT_ANIMATION_DURATION_MS
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                view.scaleX = animatedValue
                view.scaleY = animatedValue
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
        ValueAnimator.ofFloat(ALPHA_VISIBLE, ALPHA_INVISIBLE).apply {
            duration = WEATHER_EXIT_ANIMATION_DURATION_MS - WEATHER_EXIT_ALPHA_DELAY_MS
            startDelay = WEATHER_EXIT_ALPHA_DELAY_MS
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                view.alpha = animatedValue
            }
            start()
        }
    }

    private fun animateViewEnter(
        view: LottieAnimationView,
        onAnimationEnd: () -> Unit = {}
    ) {
        ValueAnimator.ofFloat(WEATHER_SCALE_EXIT, WEATHER_SCALE_ENTER).apply {
            duration = WEATHER_ENTER_ANIMATION_DURATION_MS
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                view.scaleX = animatedValue
                view.scaleY = animatedValue
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
        ValueAnimator.ofFloat(ALPHA_INVISIBLE, ALPHA_VISIBLE).apply {
            duration = WEATHER_ENTER_ANIMATION_DURATION_MS
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                view.alpha = animatedValue
            }
            start()
        }
    }

    @Suppress("detekt:UnusedPrivateMember")
    private fun adjustTextSizeExpanded() {
        binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScroll.gone()
        val hMeasureSpec = MeasureSpec.makeMeasureSpec(getScreenWidth(context as Activity), MeasureSpec.EXACTLY)
        with(binding.layoutMapPointInfoExpanded.tvMapPointInfoAddressPrimary) {
            visible()
            textSize = TEXT_SIZE_LARGE_SP
            binding.root.measure(hMeasureSpec, MeasureSpec.UNSPECIFIED)
            if (lineCount > MAX_LINE_COUNT_TEXT_LARGE) {
                textSize = TEXT_SIZE_MEDIUM_SP
                binding.root.measure(hMeasureSpec, MeasureSpec.UNSPECIFIED)
                if (lineCount > MAX_LINE_COUNT_TEXT_MEDIUM) {
                    textSize = TEXT_SIZE_SMALL_SP
                    binding.root.measure(hMeasureSpec, MeasureSpec.UNSPECIFIED)
                    if (lineCount > MAX_LINE_COUNT_TEXT_SMALL) {
                        gone()
                        val scrollUiModel = MapPointInfoScrollingTextUiModel(
                            text = text.toString(),
                            textSizeSp = TEXT_SIZE_MEDIUM_SP
                        )
                        binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScroll.setUiModel(scrollUiModel)
                        binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScroll.visible()
                    }
                }
            }
        }
    }
    @Suppress("detekt:UnusedPrivateMember")
    private fun adjustTextSizeExpandedSecondary() {
        binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScrollSecondary.invisible()
        val hMeasureSpec = MeasureSpec.makeMeasureSpec(getScreenWidth(context as Activity), MeasureSpec.EXACTLY)
        with(binding.layoutMapPointInfoExpanded.tvMapPointInfoAddressSecondary) {
            visible()
            binding.root.measure(hMeasureSpec, MeasureSpec.UNSPECIFIED)
            if (binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScrollSecondary.lineCount > MAX_LINE_COUNT_TEXT_LARGE) {
                val text = binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScrollSecondary.text

                val result = if (text == "null, null") {
                    ""
                } else {
                    text.toString()
                }
                val scrollUiModel = MapPointInfoScrollingTextUiModel(
                    text = result,
                    textSizeSp = TEXT_SIZE_SMALL_SP
                )
                binding.layoutMapPointInfoExpanded.tvMapPointInfoAddressSecondary.visible()
                binding.layoutMapPointInfoExpanded.tvMapPointInfoAddressSecondary.setUiModel(scrollUiModel)
                binding.layoutMapPointInfoExpanded.tvMapPointInfoAddressSecondary.requestLayout()
            } else {
                val text = binding.layoutMapPointInfoExpanded.mpistvMapPointInfoAddressScrollSecondary.text
                val result = if (text == "null, null") {
                    ""
                } else {
                    text.toString()
                }
                val scrollUiModel = MapPointInfoScrollingTextUiModel(
                    text = result,
                    textSizeSp = TEXT_SIZE_SMALL_SP
                )
                binding.layoutMapPointInfoExpanded.tvMapPointInfoAddressSecondary.setUiModel(scrollUiModel,false)
            }
        }
    }


    @Suppress("detekt:UnusedPrivateMember")
    private fun adjustTextScrollingCollapsed() {
        binding.layoutMapPointInfoCollapsed.mpistvMapPointInfoAddressScroll.gone()
        val hMeasureSpec = MeasureSpec.makeMeasureSpec(getScreenWidth(context as Activity), MeasureSpec.EXACTLY)
        with(binding.layoutMapPointInfoCollapsed.tvMapPointInfoAddressPrimary) {
            visible()
            binding.root.measure(hMeasureSpec, MeasureSpec.UNSPECIFIED)
            if (lineCount > MAX_LINE_COUNT_TEXT_MEDIUM) {
                gone()
                val scrollUiModel = MapPointInfoScrollingTextUiModel(
                    text = text.toString(),
                    textSizeSp = TEXT_SIZE_SMALL_SP
                )
                binding.layoutMapPointInfoCollapsed.mpistvMapPointInfoAddressScroll.setUiModel(scrollUiModel)
                binding.layoutMapPointInfoCollapsed.mpistvMapPointInfoAddressScroll.visible()
            }
        }
    }

    companion object {
        private const val TEXT_SIZE_LARGE_SP = 28f
        private const val TEXT_SIZE_MEDIUM_SP = 16f
        private const val TEXT_SIZE_SMALL_SP = 16f
        private const val MAX_LINE_COUNT_TEXT_LARGE = 1
        private const val MAX_LINE_COUNT_TEXT_MEDIUM = 1
        private const val MAX_LINE_COUNT_TEXT_SMALL = 2
        private const val ALPHA_INVISIBLE = 0f
        private const val ALPHA_VISIBLE = 1f
        private const val STATE_CHANGE_ANIMATION_DURATION_MS = 300L
        private const val WEATHER_EXIT_ANIMATION_DURATION_MS = 267L
        private const val WEATHER_EXIT_ALPHA_DELAY_MS = 133L
        private const val WEATHER_ENTER_ANIMATION_DURATION_MS = 367L
        private const val WEATHER_SCALE_ENTER = 1f
        private const val WEATHER_SCALE_EXIT = 0.8f
    }
}

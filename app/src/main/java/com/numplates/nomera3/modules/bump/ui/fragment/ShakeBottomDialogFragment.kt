package com.numplates.nomera3.modules.bump.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.empty
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentShakeBottomDialogBinding
import com.numplates.nomera3.modules.bump.ui.entity.ShakeBottomDialogUiEffect
import com.numplates.nomera3.modules.bump.ui.viewmodel.ShakeBottomDialogViewModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.utils.NToast
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random

private const val SHAKE_DURATION = 210L
private const val SHAKE_RADIUS = 20
private const val SHAKE_VIEW_SIZE = 2

class ShakeBottomDialogFragment : BaseBottomSheetDialogFragment<FragmentShakeBottomDialogBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentShakeBottomDialogBinding
        get() = FragmentShakeBottomDialogBinding::inflate

    private var shakeBottomSheetBehavior: BottomSheetBehavior<*>? = null
    private val viewModel by viewModels<ShakeBottomDialogViewModel> {
        App.component.getViewModelFactory()
    }

    private var locationShakeDescription = IntArray(SHAKE_VIEW_SIZE)
    private var locationShakeLabel = IntArray(SHAKE_VIEW_SIZE)
    private var locationShakeLocationEnableDescription = IntArray(SHAKE_VIEW_SIZE)

    private val actionMode: String by lazy {
        arguments?.getString(SHOW_SHAKE_FROM_KEY, String.empty()).orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStateByArgument()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObservers()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentTheme

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.setDialogDismissed()
    }

    fun showErrorToast(@StringRes errorMessage: Int) {
        NToast.with(act)
            .text(getString(errorMessage))
            .typeError()
            .inView(dialog?.window?.decorView)
            .show()
    }

    private fun initView() {
        initBehavior()
        initListeners()
        initShakeAnimator()
    }

    private fun initListeners() {
        binding?.ivShakeClose?.setOnClickListener {
            dismiss()
        }
        binding?.mbTurnOnAccurateLocation?.setThrottledClickListener {
            sendUserToAppSettings()
            dismiss()
        }
    }

    private fun initBehavior() {
        val dialog = dialog as BottomSheetDialog
        val mainContainer = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        mainContainer?.let { frameLayout ->
            shakeBottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
            shakeBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initObservers() {
        initStateObserver()
        initEffectObserver()
    }

    private fun shakeDescription() {
        if (binding?.tvShakeDescription?.animationEnded().isFalse()) return
        val randomPositionX =
            Random.nextInt((locationShakeDescription[0] - SHAKE_RADIUS), (locationShakeDescription[0] + SHAKE_RADIUS))
                .toFloat()
        val randomPositionY =
            Random.nextInt((locationShakeDescription[1] - SHAKE_RADIUS), (locationShakeDescription[1] + SHAKE_RADIUS))
                .toFloat()
        binding?.tvShakeDescription?.animate()
            ?.translationX(randomPositionX)
            ?.translationY(randomPositionY)
            ?.setDuration(SHAKE_DURATION)
            ?.withEndAction(::shakeDescription)
            ?.start()
    }

    private fun shakeLabel() {
        if (binding?.tvShakeLabel?.animationEnded().isFalse()) return
        val randomPositionX =
            Random.nextInt((locationShakeLabel[0] - SHAKE_RADIUS), (locationShakeLabel[0] + SHAKE_RADIUS)).toFloat()
        val randomPositionY =
            Random.nextInt((locationShakeLabel[1] - SHAKE_RADIUS), (locationShakeLabel[1] + SHAKE_RADIUS)).toFloat()
        binding?.tvShakeLabel?.animate()
            ?.translationX(randomPositionX)
            ?.translationY(randomPositionY)
            ?.setDuration(SHAKE_DURATION)
            ?.withEndAction(::shakeLabel)
            ?.start()
    }

    private fun shakeLocationEnableDescription() {
        if (binding?.tvShakeLocationEnableDescription?.animationEnded().isFalse()) return
        val randomPositionX = Random.nextInt(
            (locationShakeLocationEnableDescription[0] - SHAKE_RADIUS),
            (locationShakeLocationEnableDescription[0] + SHAKE_RADIUS)
        ).toFloat()
        val randomPositionY = Random.nextInt(
            (locationShakeLocationEnableDescription[1] - SHAKE_RADIUS),
            (locationShakeLocationEnableDescription[1] + SHAKE_RADIUS)
        ).toFloat()
        binding?.tvShakeLocationEnableDescription?.animate()
            ?.translationX(randomPositionX)
            ?.translationY(randomPositionY)
            ?.setDuration(SHAKE_DURATION)
            ?.withEndAction(::shakeLocationEnableDescription)
            ?.start()
    }

    private fun View.animationEnded(): Boolean {
        return animation?.hasEnded() ?: true
    }

    private fun initShakeAnimator() {
        binding?.tvShakeDescription?.getLocationOnScreen(locationShakeDescription)
        binding?.tvShakeLabel?.getLocationOnScreen(locationShakeLabel)
        binding?.tvShakeLocationEnableDescription?.getLocationOnScreen(locationShakeLocationEnableDescription)
        if (actionMode == DIALOG_OPENED_BY_SHAKE) {
            animateShakeViews()
            viewModel.startAnimationTimer()
        }
    }

    private fun initStateObserver() {
        viewModel.shakeDialogState.observe(viewLifecycleOwner) { state ->
            binding?.tvShakeLabel?.text = getString(state.shakeLabelTextRes)
            binding?.tvShakeDescription?.text = getString(state.shakeMessageTextRes)
            binding?.mbTurnOnAccurateLocation?.isVisible = state.isShowTurnOnAccurateLocationButton
            binding?.tvShakeLocationEnableDescription?.isVisible = state.isShowShakeLocationEnableDescription
        }
    }

    private fun initEffectObserver() {
        viewModel.shakeDialogUiEffect
            .flowWithLifecycle(lifecycle)
            .onEach(::handleUiEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleUiEffect(effect: ShakeBottomDialogUiEffect) {
        when (effect) {
            ShakeBottomDialogUiEffect.AnimateFadeUiEffect -> {
                animateFadeIn()
            }
            ShakeBottomDialogUiEffect.AnimateShakeUiEffect -> {
                animateShakeViews()
            }
            ShakeBottomDialogUiEffect.ResetShakeViewsPosition -> {
                resetShakeViewsPosition()
            }
        }
    }

    private fun resetShakeViewsPosition() {
        binding?.tvShakeLabel?.resetShakeAnimation()
        binding?.tvShakeDescription?.resetShakeAnimation()
        binding?.tvShakeLocationEnableDescription?.resetShakeAnimation()
    }

    private fun animateShakeViews() {
        shakeDescription()
        shakeLabel()
        shakeLocationEnableDescription()
    }

    private fun animateFadeIn() {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.duration = FADE_ANIMATION_DURATION
        binding?.tvShakeLabel?.startAnimation(fadeIn)
        binding?.tvShakeDescription?.startAnimation(fadeIn)
        viewModel.setUsersNotFoundUiState()
    }

    private fun View.resetShakeAnimation() {
        this.animate()
            ?.translationX(0f)
            ?.translationY(0f)
            ?.setDuration(SHAKE_DURATION)
            ?.start()
    }

    private fun setStateByArgument() {
        viewModel.setSelectedOpenType(actionMode)
    }

    companion object {
        const val SHAKE_BOTTOM_DIALOG_TAG = "shakeBottomDialog"
        const val DIALOG_OPENED_BY_SHAKE = "dialogOpenedByShake"
        const val DIALOG_OPENED_FROM_SOMEWHERE_ELSE = "dialogOpenedFromSomeWhereElse"

        private const val SHOW_SHAKE_FROM_KEY = "showShakeFrom"

        private const val FADE_ANIMATION_DURATION = 1000L

        @JvmStatic
        fun show(
            fragmentManager: FragmentManager,
            openPlace: String
        ): ShakeBottomDialogFragment {
            val instance = ShakeBottomDialogFragment()
            instance.arguments = bundleOf(SHOW_SHAKE_FROM_KEY to openPlace)
            instance.show(
                fragmentManager,
                SHAKE_BOTTOM_DIALOG_TAG
            )
            return instance
        }
    }
}

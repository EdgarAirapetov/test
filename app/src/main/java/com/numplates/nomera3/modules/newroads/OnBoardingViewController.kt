package com.numplates.nomera3.modules.newroads

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPostRoadsLayoutBinding
import com.numplates.nomera3.modules.onboarding.OnBoardingViewControllerButtonsListener
import com.numplates.nomera3.modules.onboarding.OnboardingFragment
import timber.log.Timber

class OnBoardingViewController(
    private val binding: FragmentPostRoadsLayoutBinding?,
    private val childFragmentManager: FragmentManager,
    private val viewModel: MainPostRoadsViewModel
) {

    private val bottomSheetView = binding?.bsOnBoarding
    private val isOnBoardingVisible: Boolean
        get() = binding?.bsOnBoarding?.isVisible ?: false
    private var offsetFlag = 1f
    private var isNeedCloseOnBoarding = false
    private val sheetBehavior: BottomSheetBehavior<ViewGroup> by lazy {
        val scroll = bottomSheetView ?: error("FragmentPostRoadsLayoutBinding has not been initialized yet")
        BottomSheetBehavior.from(scroll)
    }

    private var initSheetY = 0f
    /**
     * Используется что бы определить каким образом был закрыт онбординг, клик по кнопке закрыть или свайп вних
     * */
    private var closeClickedFlag = false

    fun showOnBoarding(withWelcomeScreen: Boolean = false, isHidden: Boolean = false) {
        initBottomSheet(isHidden)
        initOnBoardingFragment(withWelcomeScreen)
        initListeners()
    }

    fun onPanelDragging() {
        if (isOnBoardingVisible) {
            bottomSheetView?.gone()
        } else {
            bottomSheetView?.visible()
        }
    }

    fun onAppBarOpen() {
        if (viewModel.isNeedToShowOnBoarding()) {
            animateShowBottomSheetMenu()
        }
    }

    fun getOnBoardingState() = sheetBehavior.state

    fun onAppBarClosed() {
        if (isOnBoardingVisible && viewModel.isNeedToShowOnBoarding()) {
            animateHideBottomSheetMenu()
        }
    }

    fun expandOnBoarding() {
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideOnBoarding() {
        binding?.vOnBoardingBtmSheetBg?.gone()
        binding?.bsOnBoarding?.gone()
        val fragment = childFragmentManager.findFragmentByTag(OnboardingFragment::class.java.simpleName)
        fragment?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .commitNowAllowingStateLoss()
        }
        showBottomMenu()
    }

    private fun initBottomSheet(isHidden: Boolean) {
        binding?.vOnBoardingBtmSheetBg?.visible()
        binding?.bsOnBoarding?.visible()
        sheetBehavior.state = if (isHidden) BottomSheetBehavior.STATE_HIDDEN else BottomSheetBehavior.STATE_EXPANDED
        sheetBehavior.peekHeight = binding?.bottomBarPostlist?.height!! + 24.dp
        initSheetY = binding.vOnBoardingBtmSheetBg.y
    }

    private fun initOnBoardingFragment(withWelcomeScreen: Boolean) {
        val onBoardingFragment = OnboardingFragment.getInstance(withWelcomeScreen)
        childFragmentManager.beginTransaction()
            .add(R.id.vpContent, onBoardingFragment, OnboardingFragment::class.java.simpleName)
            .commitNowAllowingStateLoss()
    }

    private fun initListeners() {
        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    setColapsedState()
                    viewModel.setNeedToRegisterShakeEvent(true)
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.peekHeight = 0
                    addListeners()
                    viewModel.setNeedToRegisterShakeEvent(false)
                    hideBottomMenu()
                }
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    hideKeyBoard()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding?.vOnBoardingBtmSheetBg?.alpha = slideOffset
                if (offsetFlag < slideOffset && binding?.bottomBarPostlist?.visibility == View.VISIBLE) {
                    hideBottomMenu()
                    binding.bsOnBoarding.isNeedShowContinueText(false)
                }
            }
        })

        binding?.bsOnBoarding?.setContinueTextListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            getFragmentButtonsListener()?.onContinueBtnClicked()
        }

        binding?.bsOnBoarding?.setCloseBtnListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            closeClickedFlag = true
            viewModel.onBoardingCloseClicked()
            getFragmentButtonsListener()?.onCloseBtnClicked()
            hideKeyBoard()
        }
    }

    private fun setColapsedState() {
        binding?.let {
            logDownSwipe()
            sheetBehavior.setPeekHeight(it.bottomBarPostlist.height + 24.dp + 12.dp, true)
            removeListeners()
            binding.bsOnBoarding.isNeedShowContinueText(true)
            viewModel.onBoardingCollapsed()
            showBottomMenu()
            offsetFlag = 0.000f
            if (isNeedCloseOnBoarding) binding.bsOnBoarding.gone()
        }
    }

    private fun logDownSwipe() {
        if (!closeClickedFlag && sheetBehavior.peekHeight == 0) {
            getFragmentButtonsListener()?.onDownSwiped()
            viewModel.onDownSwiped()
        } else {
            closeClickedFlag = false
        }
    }

    private fun hideBottomMenu() {
        binding?.bottomBarPostlist
            ?.animate()
            ?.translationY(100.dp.toFloat())
            ?.setInterpolator(DecelerateInterpolator())
            ?.duration = MainPostRoadsFragment.SHOW_HIDE_TOP_BOTTOM_PANEL_TIME
        binding?.bottomBarPostlist?.animation?.start()
    }

    private fun showBottomMenu() {
        binding?.bottomBarPostlist?.animate()
            ?.translationY(0f)
            ?.setInterpolator(DecelerateInterpolator())
            ?.duration = MainPostRoadsFragment.SHOW_HIDE_TOP_BOTTOM_PANEL_TIME
        binding?.bottomBarPostlist?.animation?.start()
    }

    private fun addListeners() {
        binding?.vOnBoardingBtmSheetBg?.isClickable = true
        binding?.vOnBoardingBtmSheetBg?.isFocusable = true
        binding?.vOnBoardingBtmSheetBg?.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun hideKeyBoard() {
        try {
            val imm =
                binding?.appbar?.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun removeListeners() {
        binding?.vOnBoardingBtmSheetBg?.setOnClickListener(null)
        binding?.vOnBoardingBtmSheetBg?.isClickable = false
        binding?.vOnBoardingBtmSheetBg?.isFocusable = false
    }

    private fun animateHideBottomSheetMenu() {
        bottomSheetView
            ?.animate()
            ?.setStartDelay(50)
            ?.translationY(150.dp.toFloat())
            ?.setInterpolator(DecelerateInterpolator())
            ?.setDuration(MainPostRoadsFragment.SHOW_HIDE_TOP_BOTTOM_PANEL_TIME)
            ?.start()
    }

    private fun animateShowBottomSheetMenu() {
        bottomSheetView
            ?.animate()
            ?.setStartDelay(50)
            ?.translationY(0.dp.toFloat())
            ?.setInterpolator(DecelerateInterpolator())
            ?.setDuration(MainPostRoadsFragment.SHOW_HIDE_TOP_BOTTOM_PANEL_TIME)
            ?.start()
    }

    private fun getFragmentButtonsListener(): OnBoardingViewControllerButtonsListener? {
        val fragment =
            childFragmentManager.findFragmentByTag(OnboardingFragment::class.java.simpleName)
        return fragment as? OnBoardingViewControllerButtonsListener
    }
}

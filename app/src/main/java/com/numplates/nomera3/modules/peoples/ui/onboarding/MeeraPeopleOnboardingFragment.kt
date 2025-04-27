package com.numplates.nomera3.modules.peoples.ui.onboarding

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentPeopleOnboardingBinding
import com.numplates.nomera3.modules.peoples.ui.onboarding.adapter.MeeraPeopleOnboardingPagerAdapter
import com.numplates.nomera3.modules.peoples.ui.onboarding.adapter.ONBOARDING_PEOPLE_SECOND_SCREEN_POSITION
import com.numplates.nomera3.modules.peoples.ui.utils.RefreshOnboardingHeightHandler
import com.numplates.nomera3.modules.peoples.ui.viewmodel.PeopleOnboardingViewModel
import timber.log.Timber

class MeeraPeopleOnboardingFragment : UiKitBottomSheetDialog<MeeraFragmentPeopleOnboardingBinding>(),
    RefreshOnboardingHeightHandler {

    private var peopleOnboardingPagerAdapter: MeeraPeopleOnboardingPagerAdapter? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private val viewModel by viewModels<PeopleOnboardingViewModel> {
        App.component.getViewModelFactory()
    }

    private val peopleOnboardingMode: String by lazy {
        arguments?.getString(KEY_IS_SHOW_FOR_THE_FIRST_TIME, String.empty()) ?: String.empty()
    }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentPeopleOnboardingBinding
        get() = MeeraFragmentPeopleOnboardingBinding::inflate

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        needShowToolbar = false,
        needShowCloseButton = false,
        dialogStyle = R.style.BottomSheetDialogTransparentTheme
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnboarding()
        initArgs(savedInstanceState)
        initListeners()
        initObservers()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.setDialogDismissed()
    }

    override fun onRefreshPagerHeight(height: Int) {
        contentBinding?.vgPeopleOnboardingPager?.animateHeight(
            newHeight = height,
            duration = ONBOARDING_HEIGHT_CHANGE_ANIMATION_DURATION
        )
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun initObservers() {
        observeViewState()
    }

    private fun initArgs(savedInstanceState: Bundle?) {
        if (peopleOnboardingMode.isNotEmpty() && savedInstanceState == null) setStateByArgument()
    }

    private fun setStateByArgument() {
        viewModel.setPageIndicatorVisibility(peopleOnboardingMode)
        val buttonTextStringRes: Int
        val titleStringRes: Int
        when (peopleOnboardingMode) {
            ONBOARDING_SHOW_FIRST_TIME_ACTION -> {
                buttonTextStringRes = R.string.next
                titleStringRes = R.string.general_people_section
            }
            JUST_SHOW_ONBOARDING_ACTION -> {
                buttonTextStringRes = R.string.people_onboarding_wow_cool
                titleStringRes = R.string.general_recommendations
            }
            else -> return
        }
        setButtonText(buttonTextStringRes)
        setTitle(titleStringRes)
    }

    private fun observeViewState() {
        viewModel.peopleOnboardingState.observe(viewLifecycleOwner) { state ->
            setButtonText(state.buttonTextRes)
            setPageIndicatorVisibility(state.pageIndicatorVisibility)
        }
    }

    private fun setPageIndicatorVisibility(isVisible: Boolean) {
        if (contentBinding?.stlPeopleOnboarding?.isVisible == isVisible) return
        contentBinding?.stlPeopleOnboarding?.isVisible = isVisible
    }

    private fun setButtonText(@StringRes textRes: Int) {
        val textResult = try {
            activity?.getString(textRes)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
        textResult?.let { textNotNull ->
            contentBinding?.btnOk?.text = textNotNull
        } ?: run {
            Timber.d("PeopleOnboardingFragment text res is not found!")
        }
    }

    private fun setTitle(@StringRes titleRes: Int) {
        contentBinding?.nbPeopleOnboarding?.title = getString(titleRes)
    }

    private fun initOnboarding() {
        initOnboardingContent()
        initBehavior()
    }

    @SuppressLint("WrongConstant")
    private fun initOnboardingContent() {
        peopleOnboardingPagerAdapter = MeeraPeopleOnboardingPagerAdapter(
            fragment = this,
            supportUserId = viewModel.getAdminSupportId()
        )
        peopleOnboardingPagerAdapter?.addPeopleOnboardingFragments(getContentByArgument())
        setupSliderTabLayout()
        contentBinding?.vgPeopleOnboardingPager?.apply {
            adapter = peopleOnboardingPagerAdapter
            offscreenPageLimit = ONBOARDING_PAGE_LIMIT
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSliderTabLayout() {
        val tabsCount = getContentByArgument().size
        if (tabsCount == 1) {
            contentBinding?.stlPeopleOnboarding?.gone()
            return
        }
        contentBinding?.stlPeopleOnboarding?.tabCount = tabsCount
    }

    // TODO: исправить после обновления тз по onboarding
    private fun getContentByArgument(): List<Fragment> {
//        return emptyList()
        return if (peopleOnboardingMode == ONBOARDING_SHOW_FIRST_TIME_ACTION) {
            listOf(
                MeeraPeopleOnboardingFirstStepFragment(),
                MeeraPeopleOnboardingSecondStepFragment()
            )
        } else {
            listOf(MeeraPeopleOnboardingSecondStepFragment())
        }
    }

    private fun initListeners() {
        contentBinding?.nbPeopleOnboarding?.closeButtonClickListener = {
            dismiss()
        }
        contentBinding?.vgPeopleOnboardingPager?.registerOnPageChangeCallback(createPagerScrollListener())
        contentBinding?.btnOk?.setThrottledClickListener { handleNextButtonClick() }
    }

    private fun handleNextButtonClick() {
        val currentPosition = contentBinding?.vgPeopleOnboardingPager?.currentItem ?: 0
        when (peopleOnboardingPagerAdapter?.getItemByPosition(currentPosition)) {
            is MeeraPeopleOnboardingFirstStepFragment -> smoothScrollToPosition(ONBOARDING_PEOPLE_SECOND_SCREEN_POSITION)
            is MeeraPeopleOnboardingSecondStepFragment -> dismiss()
        }
    }

    private fun smoothScrollToPosition(position: Int) {
        contentBinding?.vgPeopleOnboardingPager?.currentItem = position
    }

    private fun initBehavior() {
        contentBinding?.vgPeopleOnboardingRoot?.let {
            val dialog = dialog as BottomSheetDialog
            val mainContainer = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            mainContainer?.let { frameLayout ->
                bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior?.skipCollapsed = true
            }
        }
    }

    private fun createPagerScrollListener() = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            contentBinding?.stlPeopleOnboarding?.setSelectedTabIndex(position)
            handlePageScrollChanged(position)
        }
    }

    private fun handlePageScrollChanged(pagePosition: Int) {
        when (peopleOnboardingPagerAdapter?.getItemByPosition(pagePosition)) {
            is MeeraPeopleOnboardingFirstStepFragment -> viewModel.setButtonTextState(R.string.next)
            is MeeraPeopleOnboardingSecondStepFragment -> viewModel.setButtonTextState(R.string.people_onboarding_wow_cool)
        }
    }

    companion object {
        private const val KEY_IS_SHOW_FOR_THE_FIRST_TIME = "keyIsShowForFirstTimeOnboarding"
        private const val ONBOARDING_PAGE_LIMIT = 1
        private const val ONBOARDING_HEIGHT_CHANGE_ANIMATION_DURATION = 250L

        // Состояние, когда онбординг открывается в первый раз
        const val ONBOARDING_SHOW_FIRST_TIME_ACTION = "ONBOARDING_SHOW_FIRST_TIME_ACTION"

        // // Состояние, когда онбординг открывается по клику. В данном кейсе скрывается
        // PageIndicator и блокируется swipe
        const val JUST_SHOW_ONBOARDING_ACTION = "JUST_SHOW_ONBOARDING"

        @JvmStatic
        fun create(actionMode: String): MeeraPeopleOnboardingFragment {
            val fragment = MeeraPeopleOnboardingFragment()
            val args = bundleOf(KEY_IS_SHOW_FOR_THE_FIRST_TIME to actionMode)
            fragment.arguments = args
            return fragment
        }
    }
}


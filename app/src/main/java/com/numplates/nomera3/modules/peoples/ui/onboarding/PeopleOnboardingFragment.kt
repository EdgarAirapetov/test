package com.numplates.nomera3.modules.peoples.ui.onboarding

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.click
import com.meera.core.extensions.empty
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPeopleOnboardingBinding
import com.numplates.nomera3.modules.peoples.ui.onboarding.adapter.ONBOARDING_PEOPLE_SECOND_SCREEN_POSITION
import com.numplates.nomera3.modules.peoples.ui.onboarding.adapter.PeopleOnboardingPagerAdapter
import com.numplates.nomera3.modules.peoples.ui.utils.RefreshOnboardingHeightHandler
import com.numplates.nomera3.modules.peoples.ui.viewmodel.PeopleOnboardingViewModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import timber.log.Timber

class PeopleOnboardingFragment : BaseBottomSheetDialogFragment<FragmentPeopleOnboardingBinding>(),
    RefreshOnboardingHeightHandler {

    private var peopleOnboardingPagerAdapter: PeopleOnboardingPagerAdapter? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private val viewModel by viewModels<PeopleOnboardingViewModel> {
        App.component.getViewModelFactory()
    }

    private val peopleOnboardingMode: String by lazy {
        arguments?.getString(KEY_IS_SHOW_FOR_THE_FIRST_TIME, String.empty()) ?: String.empty()
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPeopleOnboardingBinding
        get() = FragmentPeopleOnboardingBinding::inflate

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
        binding?.vgPeopleOnboardingPager?.animateHeight(
            newHeight = height,
            duration = ONBOARDING_HEIGHT_CHANGE_ANIMATION_DURATION
        )
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentTheme

    private fun initObservers() {
        observeViewState()
    }

    private fun initArgs(savedInstanceState: Bundle?) {
        if (peopleOnboardingMode.isNotEmpty() && savedInstanceState == null) setStateByArgument()
    }

    private fun setStateByArgument() {
        viewModel.setPageIndicatorVisibility(peopleOnboardingMode)
        val buttonTextStringRes =  when (peopleOnboardingMode) {
            ONBOARDING_SHOW_FIRST_TIME_ACTION -> R.string.next
            JUST_SHOW_ONBOARDING_ACTION -> R.string.people_onboarding_wow_cool
            else -> return
        }
        setButtonText(buttonTextStringRes)
    }

    private fun observeViewState() {
        viewModel.peopleOnboardingState.observe(viewLifecycleOwner) { state ->
            setButtonText(state.buttonTextRes)
            setPageIndicatorVisibility(state.pageIndicatorVisibility)
        }
    }

    private fun setPageIndicatorVisibility(isVisible: Boolean) {
        if (binding?.spiPeopleOnboarding?.isVisible == isVisible) return
        binding?.spiPeopleOnboarding?.isVisible = isVisible
    }

    private fun setButtonText(@StringRes textRes: Int) {
        val textResult = try {
            activity?.getString(textRes)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
        textResult?.let { textNotNull ->
            binding?.tvBtnOk?.text = textNotNull
        } ?: run {
            Timber.d("PeopleOnboardingFragment text res is not found!")
        }
    }

    private fun initOnboarding() {
        initOnboardingContent()
        initBehavior()
    }

    private fun initOnboardingContent() {
        peopleOnboardingPagerAdapter = PeopleOnboardingPagerAdapter(
            fragment = this,
            supportUserId = viewModel.getAdminSupportId()
        )
        peopleOnboardingPagerAdapter?.addPeopleOnboardingFragments(getContentByArgument())
        binding?.vgPeopleOnboardingPager?.apply {
            adapter = peopleOnboardingPagerAdapter
            offscreenPageLimit = ONBOARDING_PAGE_LIMIT
            isNestedScrollingEnabled = false
            val pagerRecycler = getRecyclerView() ?: return
            binding?.spiPeopleOnboarding?.attachToRecyclerView(pagerRecycler)
        }
    }

    private fun getContentByArgument(): List<Fragment> {
        return if (peopleOnboardingMode == ONBOARDING_SHOW_FIRST_TIME_ACTION) {
            listOf(
                PeopleOnboardingFirstStepFragment(),
                PeopleOnboardingSecondStepFragment()
            )
        } else {
            listOf(PeopleOnboardingSecondStepFragment())
        }
    }

    private fun ViewPager2.getRecyclerView(): RecyclerView? {
        return try {
            this[0] as? RecyclerView
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun initListeners() {
        binding?.ivPeopleOnboardingClose?.click {
            dismiss()
        }
        binding?.vgPeopleOnboardingPager?.registerOnPageChangeCallback(createPagerScrollListener())
        binding?.tvBtnOk?.click { handleNextButtonClick() }
    }

    private fun handleNextButtonClick() {
        val currentPosition = binding?.vgPeopleOnboardingPager?.currentItem ?: 0
        when (peopleOnboardingPagerAdapter?.getItemByPosition(currentPosition)) {
            is PeopleOnboardingFirstStepFragment -> smoothScrollToPosition(ONBOARDING_PEOPLE_SECOND_SCREEN_POSITION)
            is PeopleOnboardingSecondStepFragment -> dismiss()
        }
    }

    private fun smoothScrollToPosition(position: Int) {
        binding?.vgPeopleOnboardingPager?.currentItem = position
    }

    private fun initBehavior() {
        binding?.vgPeopleOnboardingRoot?.let {
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
            handlePageScrollChanged(position)
        }
    }

    private fun handlePageScrollChanged(pagePosition: Int) {
        when (peopleOnboardingPagerAdapter?.getItemByPosition(pagePosition)) {
            is PeopleOnboardingFirstStepFragment -> viewModel.setButtonTextState(R.string.next)
            is PeopleOnboardingSecondStepFragment -> viewModel.setButtonTextState(R.string.people_onboarding_wow_cool)
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
        fun create(actionMode: String): PeopleOnboardingFragment {
            val fragment = PeopleOnboardingFragment()
            val args = bundleOf(KEY_IS_SHOW_FOR_THE_FIRST_TIME to actionMode)
            fragment.arguments = args
            return fragment
        }
    }
}

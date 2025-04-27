package com.numplates.nomera3.modules.userprofile.profilestatistics.ui.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.dp
import com.meera.core.extensions.font
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.textColor
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentProfileStatisticsContainerBinding
import com.numplates.nomera3.databinding.ViewProfileStatisticsStepBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudePropertyProfileStatisticsCloseType
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.ProfileStatisticsViewState
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlideModel
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.TYPE_VIEWS
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.TYPE_VISITORS
import com.numplates.nomera3.modules.userprofile.profilestatistics.ui.adapter.ProfileStatisticsAdapter
import com.numplates.nomera3.modules.userprofile.profilestatistics.ui.viewmodel.ProfileStatisticsContainerViewModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.utils.bottomsheet.BottomSheetCloseUtil
import timber.log.Timber


class ProfileStatisticsContainerBottomSheetFragment :
    BaseBottomSheetDialogFragment<FragmentProfileStatisticsContainerBinding>() {

    companion object {
        private const val DIALOG_PEEK_HEIGHT = 566
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProfileStatisticsContainerBinding
        get() = FragmentProfileStatisticsContainerBinding::inflate

    private val adapter by lazy { ProfileStatisticsAdapter(this) }

    private val statisticsViewModel: ProfileStatisticsContainerViewModel by viewModels()

    private val bottomSheetCloseUtil = BottomSheetCloseUtil(object : BottomSheetCloseUtil.Listener {
        override fun bottomSheetClosed(method: BottomSheetCloseUtil.BottomSheetCloseMethod) {
            val closeType = when (method) {
                BottomSheetCloseUtil.BottomSheetCloseMethod.SWIPE ->
                    AmplitudePropertyProfileStatisticsCloseType.CLOSE_SWIPE
                BottomSheetCloseUtil.BottomSheetCloseMethod.TAP_OUTSIDE ->
                    AmplitudePropertyProfileStatisticsCloseType.TAP
                BottomSheetCloseUtil.BottomSheetCloseMethod.CLOSE_BUTTON ->
                    AmplitudePropertyProfileStatisticsCloseType.CLOSE
                BottomSheetCloseUtil.BottomSheetCloseMethod.BACK_BUTTON ->
                    AmplitudePropertyProfileStatisticsCloseType.TAP
            }
            binding?.vpSlides?.currentItem?.let { position ->
                statisticsViewModel.dialogIsBeingClosed(closeType, position + 1)
            }
        }
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetCloseUtil.reset()
        initViews()
        observeViewState()
    }

    override fun onBackKeyPressed() {
        super.onBackKeyPressed()
        bottomSheetCloseUtil.onBackButtonPressed()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        bottomSheetCloseUtil.onCancel()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        bottomSheetCloseUtil.onDismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let {
                val bottomSheetBehavior = BottomSheetBehavior.from(it)
                bottomSheetBehavior.peekHeight = DIALOG_PEEK_HEIGHT.dp
                bottomSheetBehavior.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        bottomSheetCloseUtil.onStateChanged(newState)
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        //ignored
                    }
                })
            }
        }
        return dialog
    }

    private fun initViews() {
        binding?.apply {
            vpSlides.adapter = this@ProfileStatisticsContainerBottomSheetFragment.adapter
            vpSlides.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding?.vgSteps?.post {
                        val count = adapter.itemCount
                        createDotsWithCurrentStep(count, position)
                    }
                    statisticsViewModel.pageChanged(position)
                }
            })
            val recyclerView = vpSlides.getRecyclerView()
            recyclerView?.isNestedScrollingEnabled = false
            recyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
            ivClose.setOnClickListener {
                bottomSheetCloseUtil.onCloseButtonPressed()
                dismiss()
            }
        }
    }

    private fun observeViewState() {
        statisticsViewModel.viewStateLiveData.observe(this) { viewState ->
            when (viewState) {
                is ProfileStatisticsViewState.Data -> {
                    val slides = viewState.slidesListModel.slides
                    if (adapter.itemCount == 0) {
                        createFragments(slides)
                    }
                    setupButton(slides, viewState.currentSlideIndex)
                }
                ProfileStatisticsViewState.Empty -> {
                    dismiss()
                }
            }
        }
    }

    private fun createFragments(slides: List<SlideModel>) {
        val fragments = mutableListOf<Fragment>()

        slides.forEach { slide ->
            when (slide.type) {
                null -> fragments.add(ProfileStatisticsIntroFragment.newInstance(slide))
                TYPE_VISITORS -> fragments.add(ProfileStatisticsVisitorsFragment.newInstance(slide))
                TYPE_VIEWS -> fragments.add(ProfileStatisticsViewsFragment.newInstance(slide))
            }
        }

        adapter.setFragments(fragments)
    }

    private fun createDotsWithCurrentStep(count: Int, currentSlideIndex: Int) {
        binding?.vgSteps?.removeAllViews()
        (0 until count).forEach { index ->
            val view = ViewProfileStatisticsStepBinding.inflate(LayoutInflater.from(requireContext())).root
            if (index == currentSlideIndex) {
                view.isSelected = true
            }
            binding?.vgSteps?.addView(view)
        }
    }

    private fun setupButton(slides: List<SlideModel>, currentSlideIndex: Int) {
        val currentSlide = slides.getOrNull(currentSlideIndex) ?: return
        binding?.btnNext?.apply {
            currentSlide.button.text.let { text = it }
            when {
                !currentSlide.button.link.isNullOrBlank() -> {
                    textColor(R.color.white)
                    setBackgroundTint(R.color.ui_purple)
                    setOnClickListener {
                        statisticsViewModel.navigatingToCreatingPost()
                        act?.openLink(currentSlide.button.link)
                    }
                    font(R.font.source_sanspro_semibold)
                }
                currentSlideIndex == slides.size - 1 -> {
                    textColor(R.color.black)
                    setBackgroundTint(R.color.colorBackgroundSecondary)
                    setOnClickListener {
                        statisticsViewModel.dialogIsBeingClosed(
                            closeType = AmplitudePropertyProfileStatisticsCloseType.BUTTON,
                            screenNumber = currentSlideIndex + 1
                        )
                        dismiss()
                    }
                    font(R.font.source_sanspro_regular)
                }
                else -> {
                    textColor(R.color.black)
                    setBackgroundTint(R.color.colorBackgroundSecondary)
                    setOnClickListener {
                        binding?.vpSlides?.let { viewPager ->
                            viewPager.currentItem = viewPager.currentItem + 1
                        }
                    }
                    font(R.font.source_sanspro_regular)
                }
            }
        }
    }

    private fun ViewPager2.getRecyclerView(): RecyclerView? {
        try {
            val field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            field.isAccessible = true
            return field.get(this) as RecyclerView
        } catch (e: NoSuchFieldException) {
            Timber.e(e)
        } catch (e: IllegalAccessException) {
            Timber.e(e)
        }
        return null
    }

}

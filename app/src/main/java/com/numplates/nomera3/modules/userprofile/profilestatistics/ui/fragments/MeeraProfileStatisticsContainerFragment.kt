package com.numplates.nomera3.modules.userprofile.profilestatistics.ui.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.base.viewbinding.viewBinding
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentProfileStatisticsContainerBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudePropertyProfileStatisticsCloseType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.ProfileStatisticsViewState
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlideModel
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.TYPE_VIEWS
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.TYPE_VISITORS
import com.numplates.nomera3.modules.userprofile.profilestatistics.ui.adapter.ProfileStatisticsAdapter
import com.numplates.nomera3.modules.userprofile.profilestatistics.ui.viewmodel.ProfileStatisticsContainerViewModel
import com.numplates.nomera3.presentation.utils.bottomsheet.BottomSheetCloseUtil
import timber.log.Timber

const val KEY_SLIDE = "slide"

class MeeraProfileStatisticsContainerFragment :
    BottomSheetDialogFragment(R.layout.meera_fragment_profile_statistics_container) {

    private val binding by viewBinding(MeeraFragmentProfileStatisticsContainerBinding::bind)

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
            binding.vpSlides.currentItem.let { position ->
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
                bottomSheetBehavior.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        bottomSheetCloseUtil.onStateChanged(newState)
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
                })
            }
        }
        return dialog
    }

    private fun initViews() {
        binding.apply {
            vpSlides.adapter = adapter
            vpSlides.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    statisticsViewModel.pageChanged(position)
                }
            })
            val recyclerView = vpSlides.getRecyclerView()
            recyclerView?.isNestedScrollingEnabled = false
            recyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
            btnClose.setOnClickListener {
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
                    setupSlider(slides.size, viewState.currentSlideIndex)
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
                null -> fragments.add(MeeraProfileStatisticsIntroFragment.newInstance(slide))
                TYPE_VISITORS -> fragments.add(MeeraProfileStatisticsVisitorsViewsFragment.newInstance(slide))
                TYPE_VIEWS -> fragments.add(MeeraProfileStatisticsVisitorsViewsFragment.newInstance(slide))
            }
        }

        adapter.setFragments(fragments)
    }

    private fun setupSlider(count: Int, currentIndex: Int) {
        if (binding.stlSteps.tabCount != count) {
            binding.stlSteps.setTabsEnabled(false)
            binding.stlSteps.tabCount = count
        }
        binding.stlSteps.setSelectedTabIndex(currentIndex)
    }

    private fun setupButton(slides: List<SlideModel>, currentSlideIndex: Int) {
        val currentSlide = slides.getOrNull(currentSlideIndex) ?: return
        binding.btnNext.apply {
            currentSlide.button.text.let { text = it }
            when {
                !currentSlide.button.link.isNullOrBlank() -> {
                    buttonType = ButtonType.FILLED
                    setOnClickListener {
                        statisticsViewModel.navigatingToCreatingPost()
                        (activity as? MeeraAct?)?.emitDeeplinkCall(currentSlide.button.link)
                    }
                }

                currentSlideIndex == slides.size - 1 -> {
                    buttonType = ButtonType.OUTLINE
                    setOnClickListener {
                        statisticsViewModel.dialogIsBeingClosed(
                            closeType = AmplitudePropertyProfileStatisticsCloseType.BUTTON,
                            screenNumber = currentSlideIndex + 1
                        )
                        dismiss()
                    }
                }

                else -> {
                    buttonType = ButtonType.OUTLINE
                    setOnClickListener {
                        binding.vpSlides.currentItem += 1
                    }
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

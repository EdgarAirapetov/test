package com.numplates.nomera3.modules.holidays.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentHolidayIntroDialogBinding
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.meera.core.extensions.click
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setHtmlText
import javax.inject.Inject

class HolidayIntroDialog: BaseBottomSheetDialogFragment<FragmentHolidayIntroDialogBinding>() {

    @Inject
    lateinit var holidayInfoHelper: HolidayInfoHelper

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    private val viewModel by viewModels<HolidayDialogViewModel>()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHolidayIntroDialogBinding
        get() = FragmentHolidayIntroDialogBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val onBoarding = holidayInfoHelper.currentHoliday().onBoardingEntity
        onBoarding.title?.let { binding?.tvTitle?.setHtmlText(it) }
        binding?.ivIcon?.loadGlide(onBoarding.icon)
        onBoarding.description?.let { binding?.tvDesc?.setHtmlText(it) }
        binding?.tvBtn?.text = onBoarding.buttonText

        binding?.tvBtn?.click { dismiss() }
        binding?.ivClose?.click { dismiss() }
        binding?.llHolidayRoot?.let {
            val dialog = dialog as BottomSheetDialog
            val mainContainer = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            bottomSheetBehavior = BottomSheetBehavior.from(mainContainer!!)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onDismissDialog()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentTheme
}
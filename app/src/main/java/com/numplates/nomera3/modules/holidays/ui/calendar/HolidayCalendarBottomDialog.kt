package com.numplates.nomera3.modules.holidays.ui.calendar

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.click
import com.meera.core.extensions.drawable
import com.meera.core.extensions.setHtmlText
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentHolidaySpecialDialogBindingBinding
import com.numplates.nomera3.modules.appInfo.ui.OnDismissListener
import com.numplates.nomera3.modules.holidays.data.entity.HolidayVisitsEntity
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

class HolidayCalendarBottomDialog : BaseBottomSheetDialogFragment<FragmentHolidaySpecialDialogBindingBinding>() {

    var onDismissListener: OnDismissListener? = null
    var onShowListener: (() -> Unit)? = null
    var onOpenGiftsListener: (() -> Unit)? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHolidaySpecialDialogBindingBinding
        get() = FragmentHolidaySpecialDialogBindingBinding::inflate

    private var visits: HolidayVisits? = null

    fun setVisits(visits: HolidayVisits) {
        this.visits = visits
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.tvIconDesc?.text = "${visits?.visitDays.toString()} " +
            "${getString(R.string.holiday_calendar_visit_days_from)} ${visits?.goalDays}"

        binding?.ivClose?.click { dismiss() }
        binding?.tvBtn?.click { dismiss() }
        visits?.let { visits ->
            when (visits.status) {
                HolidayVisitsEntity.STATUS_IN_PROGRESS -> visits.visitDays?.let {
                    showNewPresent(it.toInt())
                }
                HolidayVisitsEntity.STATUS_DAY_SKIPPED -> visits.visitDays?.let {
                    binding?.tvDesc?.setHtmlText(getString(R.string.new_year_calendar_description_hinted))
                    showNewPresent(it.toInt())
                }
                HolidayVisitsEntity.STATUS_ACHIEVED -> visits.visitDays?.let {
                    binding?.tvDesc?.text = getString(R.string.new_year_calendar_description_achieved)
                    binding?.tvBtn?.text = getString(R.string.new_year_calendar_button_achieved)
                    binding?.tvBtn?.background = context?.drawable(R.drawable.background_profile_update_btn)
                    binding?.tvBtn?.click { onOpenGiftsListener?.invoke() }
                    showNewPresent(it.toInt())
                }
                else -> Unit
            }

        }
        onShowListener?.invoke()
    }

    override fun onResume() {
        super.onResume()
        setDialogExpanded()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentTheme

    private fun showNewPresent(day: Int) {
        when (day) {
            DAY_COUNT_1 -> showPresent1()
            DAY_COUNT_2 -> showPresent2()
            DAY_COUNT_3 -> showPresent3()
            DAY_COUNT_4 -> showPresent4()
            DAY_COUNT_5 -> showPresent5()
            DAY_COUNT_6 -> showPresent6()
            DAY_COUNT_7 -> showPresent7()
        }
    }

    private fun showPresent1() {
        binding?.ivCollectedPresents?.setImageResource(R.drawable.ic_new_year_present_1)
    }

    private fun showPresent2() {
        binding?.ivCollectedPresents?.setImageResource(R.drawable.ic_new_year_present_2)
    }

    private fun showPresent3() {
        binding?.ivCollectedPresents?.setImageResource(R.drawable.ic_new_year_present_3)
    }

    private fun showPresent4() {
        binding?.ivCollectedPresents?.setImageResource(R.drawable.ic_new_year_present_4)
    }

    private fun showPresent5() {
        binding?.ivCollectedPresents?.setImageResource(R.drawable.ic_new_year_present_5)
    }

    private fun showPresent6() {
        binding?.ivCollectedPresents?.setImageResource(R.drawable.ic_new_year_present_6)
    }

    private fun showPresent7() {
        binding?.ivCollectedPresents?.setImageResource(R.drawable.ic_new_year_present_7)
    }

    private fun setDialogExpanded() {
        dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
    }

    companion object {
        const val DAY_COUNT_1 = 1
        const val DAY_COUNT_2 = 2
        const val DAY_COUNT_3 = 3
        const val DAY_COUNT_4 = 4
        const val DAY_COUNT_5 = 5
        const val DAY_COUNT_6 = 6
        const val DAY_COUNT_7 = 7
    }
}

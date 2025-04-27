package com.numplates.nomera3.modules.holidays.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.meera.core.databinding.MeeraHolidayCalendarDialogBinding
import com.meera.core.extensions.empty
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.holidays.data.entity.HolidayVisitsEntity
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits

private const val FRAGMENT_NAME = "MeeraHolidayCalendarBottomDialog"

class MeeraHolidayCalendarBottomDialog : UiKitBottomSheetDialog<MeeraHolidayCalendarDialogBinding>() {

    private var data = MeeraHolidayCalendarBottomData()

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraHolidayCalendarDialogBinding
        get() = MeeraHolidayCalendarDialogBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(false)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(data.headerRes))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    fun setData(data: MeeraHolidayCalendarBottomData) {
        this.data = data
    }

    private fun initViews() {
        contentBinding?.apply {
            ivGiftPic.loadGlide(getGiftPic())
            tvCountDay.text = getCountDayText()
            tvGiftDescription.text = getDescriptionText()
            vConfirmationBtn.text = getConfirmButtonText()
            vConfirmationBtn.setThrottledClickListener {
                data.confirmBtnClicked.invoke()
                setButtonClickState()
            }
        }
    }

    private fun getCountDayText(): String {
        return data.currentDay + getString(R.string.meera_holiday_calendar_visit_days_from) + data.totalDay
    }

    private fun getGiftPic(): Int {
       return data.imageRes
    }

    private fun getDescriptionText(): String {
        return when {
            data.descriptionRes != ResourcesCompat.ID_NULL -> getString(data.descriptionRes)
            data.description.isNotEmpty() -> data.description
            else -> String.empty()
        }
    }

    private fun getConfirmButtonText(): String {
        return when {
            data.confirmBtnTextRes != ResourcesCompat.ID_NULL -> getString(data.confirmBtnTextRes)
            data.confirmBtnText.isNotEmpty() -> data.confirmBtnText
            else -> String.empty()
        }
    }

    private fun setButtonClickState() {
        dismiss()
    }
}

class MeeraHolidayCalendarBottomDialogBuilder {

    private var isCancelable = false
    private var data = MeeraHolidayCalendarBottomData()
    private var visits: HolidayVisits? = null
    private var showGiftBtnClicked: () -> Unit = {}
    private var longLiveLollipopsBtnClicked: () -> Unit = {}

    fun setVisits(visits: HolidayVisits): MeeraHolidayCalendarBottomDialogBuilder {
        this.visits = visits
        return this
    }

    fun setShowGiftBtnClickListener(showGiftBtnClicked: () -> Unit): MeeraHolidayCalendarBottomDialogBuilder {
        this.showGiftBtnClicked = showGiftBtnClicked
        return this
    }

    fun setLongLiveLollipopsBtnClickListener(longLiveLollipopsBtnClicked: () -> Unit): MeeraHolidayCalendarBottomDialogBuilder {
        this.longLiveLollipopsBtnClicked = longLiveLollipopsBtnClicked
        return this
    }

    private fun showNewPresent(day: Int) {
        when (day) {
            HolidayCalendarBottomDialog.DAY_COUNT_1 -> data = data.copy(imageRes = R.drawable.ic_new_year_present_1)
            HolidayCalendarBottomDialog.DAY_COUNT_2 -> data = data.copy(imageRes = R.drawable.ic_new_year_present_2)
            HolidayCalendarBottomDialog.DAY_COUNT_3 -> data = data.copy(imageRes = R.drawable.ic_new_year_present_3)
            HolidayCalendarBottomDialog.DAY_COUNT_4 -> data = data.copy(imageRes = R.drawable.ic_new_year_present_4)
            HolidayCalendarBottomDialog.DAY_COUNT_5 -> data = data.copy(imageRes = R.drawable.ic_new_year_present_5)
            HolidayCalendarBottomDialog.DAY_COUNT_6 -> data = data.copy(imageRes = R.drawable.ic_new_year_present_6)
            HolidayCalendarBottomDialog.DAY_COUNT_7 -> data = data.copy(imageRes = R.drawable.ic_new_year_present_7)
        }
    }

    private fun initDialogData(){
        data = data.copy(headerRes = R.string.meera_composite_gift_collecting_lollipops)

        visits?.let { visits ->
            when (visits.status) {
                HolidayVisitsEntity.STATUS_IN_PROGRESS -> visits.visitDays?.let {visitDay ->
                    data = data.copy(
                        descriptionRes = R.string.meera_new_year_calendar_description_hinted,
                        currentDay = visits.visitDays.toString(),
                        totalDay = visits.goalDays.toString(),
                        confirmBtnTextRes = R.string.meera_new_year_calendar_button_long_live_lollipops,
                        confirmBtnClicked = longLiveLollipopsBtnClicked
                    )
                    showNewPresent(visitDay.toInt())
                }
                HolidayVisitsEntity.STATUS_DAY_SKIPPED -> visits.visitDays?.let {
                    data = data.copy(
                        descriptionRes = R.string.meera_new_year_calendar_description_hinted,
                        currentDay = visits.visitDays.toString(),
                        totalDay = visits.goalDays.toString(),
                        confirmBtnTextRes = R.string.meera_new_year_calendar_button_long_live_lollipops,
                        confirmBtnClicked = longLiveLollipopsBtnClicked
                    )
                    showNewPresent(it.toInt())
                }
                HolidayVisitsEntity.STATUS_ACHIEVED -> visits.visitDays?.let {
                    data = data.copy(
                        descriptionRes = R.string.new_year_calendar_description_achieved,
                        confirmBtnTextRes = R.string.new_year_calendar_button_achieved,
                        confirmBtnType = ButtonType.FILLED,
                        confirmBtnClicked = showGiftBtnClicked
                    )
                    showNewPresent(it.toInt())
                }
                else -> Unit
            }
        }
    }

    fun show(fm: FragmentManager): MeeraHolidayCalendarBottomDialog {
        val dialog = MeeraHolidayCalendarBottomDialog()
        initDialogData()
        dialog.setData(data)
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, FRAGMENT_NAME)
        return dialog
    }
}

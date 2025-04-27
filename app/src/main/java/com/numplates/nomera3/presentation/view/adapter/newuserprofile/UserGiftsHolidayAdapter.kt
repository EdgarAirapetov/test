package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.holidays.ui.calendar.HolidayCalendarBottomDialog

class UserGiftsHolidayAdapter: RecyclerView.Adapter<UserGiftsHolidayAdapter.ViewHolder>() {

    var showMoreButtonClicked: (() -> Unit)? = null

    private var visitCount = 0

    @SuppressLint("NotifyDataSetChanged")
    fun setVisits(visits: Int) {
        visitCount = visits
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       return ViewHolder(LayoutInflater.from(parent.context)
               .inflate(R.layout.item_holiday_gifts, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =  holder.bind()

    override fun getItemCount() = 1

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val clContainer: ConstraintLayout? = view.findViewById(R.id.clContainer)
        private val ivPresent: ImageView? = itemView.findViewById(R.id.iv_present)

        fun bind() {
            when (visitCount) {
                HolidayCalendarBottomDialog.DAY_COUNT_1 -> showCandy1()
                HolidayCalendarBottomDialog.DAY_COUNT_2 -> showCandy2()
                HolidayCalendarBottomDialog.DAY_COUNT_3 -> showCandy3()
                HolidayCalendarBottomDialog.DAY_COUNT_4 -> showCandy4()
                HolidayCalendarBottomDialog.DAY_COUNT_5 -> showCandy5()
                HolidayCalendarBottomDialog.DAY_COUNT_6 -> showCandy6()
                HolidayCalendarBottomDialog.DAY_COUNT_7 -> showCandy7()
            }
            clContainer?.click { showMoreButtonClicked?.invoke() }
        }

        private fun showCandy1() {
            ivPresent?.setImageResource(R.drawable.ic_new_year_present_1)
        }

        private fun showCandy2() {
            ivPresent?.setImageResource(R.drawable.ic_new_year_present_2)
        }

        private fun showCandy3() {
            ivPresent?.setImageResource(R.drawable.ic_new_year_present_3)
        }

        private fun showCandy4() {
            ivPresent?.setImageResource(R.drawable.ic_new_year_present_4)
        }

        private fun showCandy5() {
            ivPresent?.setImageResource(R.drawable.ic_new_year_present_5)
        }

        private fun showCandy6() {
            ivPresent?.setImageResource(R.drawable.ic_new_year_present_6)
        }

        private fun showCandy7() {
            ivPresent?.setImageResource(R.drawable.ic_new_year_present_7)
        }
    }
}

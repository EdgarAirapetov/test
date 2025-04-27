package com.numplates.nomera3.modules.purchase.ui.vip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.purchase.ui.model.SimplePurchaseUiModel

class BottomSheetVipDialog : BottomSheetDialogFragment() {

    private lateinit var callback: OnBottomSheetCallback

    interface OnBottomSheetCallback {
        fun onDialogShow()
        fun onPurchaseVip(marketId: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.bottom_sheet_vip_selector, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener {
            callback.onDialogShow()
        }
    }

    fun setCallback(callback: OnBottomSheetCallback) {
        this.callback = callback
    }

    fun renderViews(purchases: List<SimplePurchaseUiModel>) {
        // Week
        val week = view?.findViewById<LinearLayout>(R.id.weekVipItem)
        val weekDescription = view?.findViewById<TextView>(R.id.tvWeekDescription)
        val weekPrice = view?.findViewById<TextView>(R.id.tvWeekPrice)
        if (purchases.isEmpty()) return
        weekDescription?.text = purchases[0].description
        weekPrice?.text = purchases[0].price
        week?.setOnClickListener {
            callback.onPurchaseVip(purchases[0].marketId)
        }

        // Month
        val month = view?.findViewById<LinearLayout>(R.id.monthVipItem)
        val monthDescription = view?.findViewById<TextView>(R.id.tvMonthDescription)
        val monthPrice = view?.findViewById<TextView>(R.id.tvMonthPrice)
        monthDescription?.text = purchases[1].description
        monthPrice?.text = purchases[1].price
        month?.setOnClickListener {
            callback.onPurchaseVip(purchases[1].marketId)
        }

        // Three month
        val threeMonth = view?.findViewById<LinearLayout>(R.id.threeMonthVipItem)
        val threeMonthDescription = view?.findViewById<TextView>(R.id.tvThreeMonthDescription)
        val threeMonthPrice = view?.findViewById<TextView>(R.id.tvThreeMonthPrice)
        threeMonthDescription?.text = purchases[2].description
        threeMonthPrice?.text = purchases[2].price
        threeMonth?.setOnClickListener {
            callback.onPurchaseVip(purchases[2].marketId)
        }

        // One year
        val year = view?.findViewById<LinearLayout>(R.id.yearVipItem)
        val yearDescription = view?.findViewById<TextView>(R.id.tvYearDescription)
        val yearPrice = view?.findViewById<TextView>(R.id.tvYearPrice)
        yearDescription?.text = purchases[3].description
        yearPrice?.text = purchases[3].price
        year?.setOnClickListener {
            callback.onPurchaseVip(purchases[3].marketId)
        }
    }

}

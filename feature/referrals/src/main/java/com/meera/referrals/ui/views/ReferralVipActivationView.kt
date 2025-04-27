package com.meera.referrals.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.referrals.R
import com.meera.referrals.databinding.ReferralVipActivationLayoutBinding

class ReferralVipActivationView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : ConstraintLayout(context, attributeSet, defStyleAtr) {

    private val binding: ReferralVipActivationLayoutBinding =
        ReferralVipActivationLayoutBinding.inflate(LayoutInflater.from(context), this)

    fun setMonths(months: Int) {
        binding.tvReferralVipActivationDesc.text =
            resources.getString(R.string.referral_available_vips, months)
    }

}

package com.meera.referrals.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.applyRoundedOutline
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.setMargins
import com.meera.referrals.R
import com.meera.referrals.databinding.ReferralUserCounterViewBinding
import com.meera.referrals.ui.model.ReferralDataUIModel

private const val MAXIMUM_POINT_SIZE = 18
private const val DEFAULT_POINTS_COUNT = 4
private const val DEFAULT_POINTS_MARGIN = 4



class ReferralUserCounterView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : ConstraintLayout(context, attributeSet, defStyleAtr) {

    private val binding: ReferralUserCounterViewBinding =
        ReferralUserCounterViewBinding.inflate(LayoutInflater.from(context), this)

    fun setData(data: ReferralDataUIModel) {
        setText(data)
        setPoints(data.referrals.count)
    }

    private fun setText(data: ReferralDataUIModel) {
        val limit = data.referrals.limit
        val count = data.referrals.count
        val remainInvites = limit - count
        binding.tvCounterTitle.text =
            resources.getString(R.string.referral_remain_count, remainInvites)
    }

    private fun setPoints(count: Int) {
        val margin = dpToPx(DEFAULT_POINTS_MARGIN)
        val maxSize = dpToPx(MAXIMUM_POINT_SIZE)
        binding.vgInvitedUserIndicator.post {
            for (i in 0 until binding.vgInvitedUserIndicator.childCount - 1) {
                binding.vgInvitedUserIndicator.removeViewAt(0)
            }
            val pointSize = (binding.tvCounterTitle.width / DEFAULT_POINTS_COUNT) - margin * 2
            for (i in 1..DEFAULT_POINTS_COUNT) {
                val viewPoint = View(context)
                if (pointSize > maxSize) viewPoint.layoutParams = LayoutParams(maxSize, maxSize)
                else viewPoint.layoutParams = LayoutParams(pointSize, pointSize)
                viewPoint.setMargins(margin, 0, margin, 0)
                viewPoint.setBackgroundColor(
                    ContextCompat.getColor(
                        context, if (count > 0 && i <= count) {
                            R.color.ui_yellow
                        } else {
                            R.color.ui_yellow_light
                        }
                    )
                )
                viewPoint.applyRoundedOutline(radius = (pointSize / 2).toFloat())
                binding.vgInvitedUserIndicator.addView(viewPoint, binding.vgInvitedUserIndicator.childCount - 1)
            }
        }
    }

}

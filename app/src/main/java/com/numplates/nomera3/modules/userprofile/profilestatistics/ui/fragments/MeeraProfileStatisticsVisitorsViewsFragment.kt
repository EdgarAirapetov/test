package com.numplates.nomera3.modules.userprofile.profilestatistics.ui.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.meera.core.base.viewbinding.viewBinding
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentProfileStatisticsVisitorsViewsBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.ProfileStatisticsTrend
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlideModel
import java.text.DecimalFormat

private const val ARROW_UP = "\u2191"
private const val ARROW_DOWN = "\u2193"

class MeeraProfileStatisticsVisitorsViewsFragment : MeeraBaseFragment(R.layout.meera_fragment_profile_statistics_visitors_views) {

    private val binding by viewBinding(MeeraFragmentProfileStatisticsVisitorsViewsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        val slide = arguments?.getParcelable<SlideModel>(KEY_SLIDE) ?: return
        binding.apply {
            tvText.text = slide.text
            tvTitle.text = slide.title

            if (slide.count != null && slide.growth != null) {
                setupVisitorsText(slide.count, slide.trend)
            }
        }
    }

    private fun setupVisitorsText(count: Long, trend: ProfileStatisticsTrend) {
        val formattedCount = DecimalFormat("###,###,###,###").format(count).replace(",", " ")
        when (trend) {
            ProfileStatisticsTrend.POSITIVE -> {
                val arrow = ARROW_UP
                val spannableText = SpannableString("$formattedCount $arrow")
                val arrowColor =
                    ContextCompat.getColor(requireContext(), R.color.profile_statistics_green)
                val colorSpan = ForegroundColorSpan(arrowColor)
                spannableText.setSpan(
                    colorSpan,
                    spannableText.length - 1,
                    spannableText.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                binding.tvVisitorsCount.text = spannableText
            }
            ProfileStatisticsTrend.NEGATIVE -> {
                val arrow = ARROW_DOWN
                val spannableText = SpannableString("$formattedCount $arrow")
                val arrowColor =
                    ContextCompat.getColor(requireContext(), R.color.profile_statistics_red)
                val colorSpan = ForegroundColorSpan(arrowColor)
                spannableText.setSpan(
                    colorSpan,
                    spannableText.length - 1,
                    spannableText.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                binding.tvVisitorsCount.text = spannableText
            }
            ProfileStatisticsTrend.SAME -> {
                binding.tvVisitorsCount.text = count.toString()
            }
        }
    }

    companion object {
        fun newInstance(slideResponse: SlideModel): MeeraProfileStatisticsVisitorsViewsFragment {
            return MeeraProfileStatisticsVisitorsViewsFragment().apply {
                arguments = bundleOf(KEY_SLIDE to slideResponse)
            }
        }
    }
}

package com.numplates.nomera3.modules.userprofile.profilestatistics.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentProfileStatisticsIntroBinding
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlideModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible

class ProfileStatisticsIntroFragment : BaseFragmentNew<FragmentProfileStatisticsIntroBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProfileStatisticsIntroBinding
        get() = FragmentProfileStatisticsIntroBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        val slide = arguments?.getParcelable<SlideModel>(KEY_SLIDE) ?: return
        binding?.apply {
            if (slide.imageUrl.isNullOrEmpty()) {
                ivImage.gone()
            } else {
                ivImage.visible()
                Glide.with(ivImage)
                    .load(slide.imageUrl)
                    .error(R.drawable.ic_profile_statistics_intro)
                    .into(ivImage)
            }
            tvText.text = slide.text
            tvTitle.text = slide.title
        }
    }

    companion object {

        fun newInstance(slideResponse: SlideModel): ProfileStatisticsIntroFragment {
            return ProfileStatisticsIntroFragment().apply {
                arguments = bundleOf(KEY_SLIDE to slideResponse)
            }
        }

    }
}

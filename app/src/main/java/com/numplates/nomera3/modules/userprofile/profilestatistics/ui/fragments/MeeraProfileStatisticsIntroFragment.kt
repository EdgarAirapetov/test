package com.numplates.nomera3.modules.userprofile.profilestatistics.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentProfileStatisticsIntroBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlideModel

class MeeraProfileStatisticsIntroFragment : MeeraBaseFragment(R.layout.meera_fragment_profile_statistics_intro) {

    private val binding by viewBinding(MeeraFragmentProfileStatisticsIntroBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        val slide = arguments?.getParcelable<SlideModel>(KEY_SLIDE) ?: return
        binding.apply {
            if (slide.imageUrl.isNullOrEmpty()) {
                ivImage.gone()
            } else {
                ivImage.visible()
                Glide.with(ivImage)
                    .load(slide.imageUrl)
                    .error(R.drawable.ic_profile_statistics_pic)
                    .into(ivImage)
            }
            tvText.text = slide.text
            tvTitle.text = slide.title
        }
    }

    companion object {

        fun newInstance(slideResponse: SlideModel): MeeraProfileStatisticsIntroFragment {
            return MeeraProfileStatisticsIntroFragment().apply {
                arguments = bundleOf(KEY_SLIDE to slideResponse)
            }
        }

    }
}

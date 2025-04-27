package com.numplates.nomera3.modules.onboarding

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.numplates.nomera3.databinding.OnBoardingSheetContainerBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible

class OnBoardingContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: OnBoardingSheetContainerBinding =
        OnBoardingSheetContainerBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
    }

    fun isNeedShowContinueText(isShow: Boolean){
        if (isShow) binding.tvContinueView.visible()
        else binding.tvContinueView.gone()
    }

    fun setCloseBtnListener(btnListener: () -> Unit) {
        binding.ivOnBoardingClose.setOnClickListener { btnListener() }
    }

    fun setContinueTextListener(textListener: () -> Unit) {
        binding.tvContinueView.setOnClickListener { textListener() }
    }
}
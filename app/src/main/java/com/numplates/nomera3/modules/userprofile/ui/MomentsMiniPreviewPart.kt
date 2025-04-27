package com.numplates.nomera3.modules.userprofile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewMomentsMiniPreviewPartBinding

class MomentsMiniPreviewPart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMomentsMiniPreviewPartBinding.inflate(LayoutInflater.from(context), this)

    init {
        context.withStyledAttributes(attrs, R.styleable.MomentsMiniPreviewPart) {

            val isViewed = getBoolean(R.styleable.MomentsMiniPreviewPart_isViewed, false)
            if (isViewed) {
                with(binding) {
                    vMomentsFirst.setBackgroundTint(R.color.colorGray9298A0)
                    vMomentsSecond.setBackgroundTint(R.color.colorGray9298A0)
                    vMomentsThird.setBackgroundTint(R.color.colorGray9298A0)
                }
            }
        }
    }

    fun setMomentsMiniPreview(moments: List<String>) {
        with(binding) {
            val allIcons = arrayOf(sivMutualFirst, sivMutualSecond, sivMutualThird)
            val allIconsGroups = arrayOf(group1, group2, group3)
            allIcons.forEachIndexed { index, avatar ->
                moments.getOrNull(index)?.let { previewUrl ->
                    allIconsGroups.getOrNull(index)?.visible()
                    avatar.loadGlide(previewUrl)
                } ?: run {
                    allIconsGroups.getOrNull(index)?.gone()
                }
            }
        }
    }
}

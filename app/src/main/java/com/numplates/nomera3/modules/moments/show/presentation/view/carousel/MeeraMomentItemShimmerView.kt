package com.numplates.nomera3.modules.moments.show.presentation.view.carousel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.numplates.nomera3.databinding.ItemMomentBlankShimmerBinding
import com.numplates.nomera3.databinding.ItemMomentPlaceShimmerBinding
import com.numplates.nomera3.databinding.ItemMomentUserShimmerBinding

class MeeraMomentItemShimmerView @JvmOverloads constructor(
    context: Context,
    type: MomentShimmerType = MomentShimmerType.UserShimmer,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init {
        when (type) {
            MomentShimmerType.UserShimmer -> ItemMomentUserShimmerBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            MomentShimmerType.PlaceShimmer -> ItemMomentPlaceShimmerBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            MomentShimmerType.BlankShimmer -> ItemMomentBlankShimmerBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
        }
    }
}

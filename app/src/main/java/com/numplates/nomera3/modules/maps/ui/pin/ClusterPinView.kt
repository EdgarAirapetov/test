package com.numplates.nomera3.modules.maps.ui.pin

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewClusterBinding
import com.numplates.nomera3.modules.maps.ui.pin.model.ClusterPinUiModel

class ClusterPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding: ViewClusterBinding = LayoutInflater.from(context)
        .inflate(R.layout.view_cluster, this, false)
        .apply(::addView)
        .let(ViewClusterBinding::bind)

    fun show(clusterPinUiModel: ClusterPinUiModel) {
        listOf(
            binding.ivClusterUserTop,
            binding.ivClusterUserMiddle,
            binding.ivClusterUserBottom
        ).forEachIndexed { index, imageView ->
            val bitmap = clusterPinUiModel.userAvatars.getOrNull(index)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageDrawable(getDefaultUserDrawable())
            }
        }
        binding.tvClusterCapacity.text = clusterPinUiModel.capacity
    }

    private fun getDefaultUserDrawable(): Drawable? = ContextCompat.getDrawable(context, R.drawable.fill_8_round)
}

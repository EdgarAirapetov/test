package com.numplates.nomera3.modules.userprofile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.ViewMomentsMiniPreviewBinding

class MomentsMiniPreview @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMomentsMiniPreviewBinding.inflate(LayoutInflater.from(context), this)

    private var onCreateClickListener: (() -> Unit)? = null

    fun setMomentsPreview(momentsMiniPreviewModel: MomentsMiniPreviewModel) {
        with(binding) {

            val firstThreeMoments = momentsMiniPreviewModel.momentsPreviews.take(MOMENTS_MAX_COUNT)

            val newMoments = firstThreeMoments.filter { it.viewed.not() }.map { it.url }
            val viewedMoments = firstThreeMoments.filter { it.viewed }.map { it.url }

            partNewMoments.setMomentsMiniPreview(newMoments)
            partNewMoments.isGone = newMoments.isEmpty()

            partViewedMoments.setMomentsMiniPreview(viewedMoments)
            partViewedMoments.isGone = viewedMoments.isEmpty()

            if (momentsMiniPreviewModel.isMe) {
                if (firstThreeMoments.size >= MOMENTS_MAX_COUNT) {
                    sivBigPlus.gone()
                    sivSmallPlus.visible()
                } else {
                    sivBigPlus.visible()
                    sivSmallPlus.gone()
                }
                sivBigPlus.setThrottledClickListener { onCreateClickListener?.invoke() }
                sivSmallPlus.setThrottledClickListener { onCreateClickListener?.invoke() }
            } else {
                sivBigPlus.gone()
                sivSmallPlus.invisible()
                vSmallPlusBack.invisible()
            }
        }
    }

    fun setOnCreateClickListener(onCreateClickListener: (() -> Unit)) {
        this.onCreateClickListener = onCreateClickListener
    }

    companion object {
        const val MOMENTS_MAX_COUNT = 3
    }
}

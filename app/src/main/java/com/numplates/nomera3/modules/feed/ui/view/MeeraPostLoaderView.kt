package com.numplates.nomera3.modules.feed.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.withStyledAttributes
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.px
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.PostLoaderLayoutBinding

private const val POST_LOADER_NO_SIZE_PERCENTAGE = Float.MIN_VALUE
private const val LOADER_MAX_SIZE_DEFAULT_DP = 112f
private const val LOADER_MIN_SIZE_DEFAULT_DP = 54f

/**
 * Loader layout that changes its' size depending on the size of parent.
 *
 * Manages the size of animated view and cancel button.
 */
class MeeraPostLoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context, attrs, defStyleAttr
) {

    private val binding = PostLoaderLayoutBinding.inflate(LayoutInflater.from(context), this)

    /**
     * What size the view will try to take up based on parent's height.
     * Has higher priority than [widthPercentage].
     */
    var heightPercentage: Float = POST_LOADER_NO_SIZE_PERCENTAGE
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
                invalidate()
            }
        }

    /**
     * What size the view will try to take up based on parent's height
     * Has lower priority than [heightPercentage].
     */
    var widthPercentage: Float = POST_LOADER_NO_SIZE_PERCENTAGE
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
                invalidate()
            }
        }

    var maxSize: Float = LOADER_MAX_SIZE_DEFAULT_DP.px
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
                invalidate()
            }
        }

    var minSize: Float = LOADER_MIN_SIZE_DEFAULT_DP.px
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
                invalidate()
            }
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.PostLoaderView) {
            heightPercentage = getFloat(R.styleable.PostLoaderView_plv_percentageHeight, POST_LOADER_NO_SIZE_PERCENTAGE)
            widthPercentage = getFloat(R.styleable.PostLoaderView_plv_percentageWidth, POST_LOADER_NO_SIZE_PERCENTAGE)
            maxSize = getDimension(R.styleable.PostLoaderView_plv_maxSize, LOADER_MAX_SIZE_DEFAULT_DP.px)
            minSize = getDimension(R.styleable.PostLoaderView_plv_minSize, LOADER_MIN_SIZE_DEFAULT_DP.px)
        }
    }

    fun getAnimationView(): LottieAnimationView = binding.lavLoader

    fun getCancelView(): ImageView = binding.postCancelMediaDownload

}

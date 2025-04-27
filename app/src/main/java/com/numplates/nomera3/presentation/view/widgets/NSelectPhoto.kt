package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import timber.log.Timber

class NSelectPhoto : ConstraintLayout {
    private lateinit var ivImage: ImageView
    private lateinit var ivAddImageBtn: ImageView
    private lateinit var ivCloseImage: ImageView
    private lateinit var pbProgress: ProgressBar

    private var src = -1
    var hasContent = false
    var isLoading = false

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        inflate(context, R.layout.layout_add_image_view, this)

        ivImage = findViewById(R.id.ivVehicle)
        ivAddImageBtn = findViewById(R.id.ivAddVehicle)
        ivCloseImage = findViewById(R.id.ivCloseImage)
        pbProgress = findViewById(R.id.pb_nselect_photo)

        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.NSelectPhoto,
            0, 0
        )
        src = typedArray.getResourceId(R.styleable.NSelectPhoto_srcImage, -1)
        if (src != -1) {
            ivImage.setImageResource(src)
            ivImage.visible()
        } else {
            imageRemoved()
        }

    }

    fun getImage() = ivImage

    fun setOnAddClickListener(listener: OnClickListener) {
        ivAddImageBtn.setOnClickListener(listener)
    }

    fun setOnCloseClickListener(listener: OnClickListener) {
        ivCloseImage.setOnClickListener(listener)
    }

    fun imageRemoved() {
        hasContent = false
        ivCloseImage.gone()
        if (src != -1) {
            ivImage.setImageResource(src)
        } else ivImage.invisible()
        ivAddImageBtn.visible()
    }

    fun imageSetUp() {
        Timber.e("imageSetUp")
        ivCloseImage.visible()
        ivImage.visible()
        ivAddImageBtn.gone()
        setProgress(false)
    }

    fun loadImageWithGlide(url: String) {
        hasContent = true
        Glide.with(context)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    hasContent = false
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    imageSetUp()
                    return false
                }

            })
            .into(ivImage)
    }

    fun setProgress(hasProgress: Boolean) {
        isLoading = if (hasProgress && !hasContent) {
            pbProgress.visible()
            ivAddImageBtn.gone()
            true
        } else {
            pbProgress.gone()
            false
        }
    }


    fun loadImageResource(resId: Int) {
        Glide.with(context)
            .load(resId)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    hasContent = false
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    imageSetUp()
                    return false
                }

            })
            .into(ivImage)
    }
}

package com.numplates.nomera3.modules.gift_coffee.ui.coffee_select

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.utils.graphics.DrawableAlwaysCrossFadeFactory
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.CoffeeSelectItemBinding


private const val DEFAULT_COFFEE_NAME = "undefined"
@DrawableRes private const val DEFAULT_IMAGE = R.drawable.close_coffee_small

class CoffeeButtonCustomView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private val binding: CoffeeSelectItemBinding =
            CoffeeSelectItemBinding.inflate(
                    LayoutInflater.from(context),
                    this,
                    true
            )

    private val selectColor = ContextCompat.getColor(context, R.color.ui_purple_text)
    private val regularColor = ContextCompat.getColor(context, R.color.ui_gray_80)
    private val coffeeLabel: String
    private val coffeeImage: Drawable

    init {
        val typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.CoffeeButtonCustomView)

        coffeeImage = typedArray.getDrawable(R.styleable.CoffeeButtonCustomView_image_resource)
                ?: ContextCompat.getDrawable(context, DEFAULT_IMAGE)!!

        coffeeLabel = typedArray.getString(R.styleable.CoffeeButtonCustomView_coffee_label)
                ?: DEFAULT_COFFEE_NAME

        typedArray.recycle()

        binding.coffeeItemText.text = coffeeLabel

        Glide.with(binding.root.context)
            .load(coffeeImage)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .priority(Priority.IMMEDIATE)
            .transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
            .into(binding.coffeeItemImage)

        regular()
    }

    fun select() {
        binding.coffeeItemText.setTextColor(selectColor)
        binding.root.setBackgroundResource(R.drawable.coffee_item_selected_background)
    }

    fun regular() {
        binding.coffeeItemText.setTextColor(regularColor)
        binding.root.setBackgroundResource(R.drawable.coffee_item_regular_background)
    }
}

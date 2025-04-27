package com.numplates.nomera3.modules.uploadpost.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.extensions.setVisible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewPostSelectBackgroundItemBinding
import com.numplates.nomera3.modules.appInfo.ui.entity.PostBackgroundItemUiModel

class PostSelectBackgroundItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): LinearLayout(context, attrs, defStyle) {

    private var  postBackgroundItemUiModel: PostBackgroundItemUiModel? = null

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_post_select_background_item, this, false)
        .apply(::addView)
        .let(ViewPostSelectBackgroundItemBinding::bind)

    fun bind(postBackgroundItemUiModel: PostBackgroundItemUiModel, onBackgroundSelected: (PostBackgroundItemUiModel) -> Unit) {
        this.postBackgroundItemUiModel = postBackgroundItemUiModel

        initBackground(url = postBackgroundItemUiModel.previewUrl)
        setSelectedItem(isSelected = postBackgroundItemUiModel.isSelected)

        setOnClickListener {
            if (postBackgroundItemUiModel.isSelected) return@setOnClickListener

            onBackgroundSelected.invoke(postBackgroundItemUiModel)
        }
    }

    fun updatePayload(postBackgroundItemUiModel: PostBackgroundItemUiModel) {
        this.postBackgroundItemUiModel = postBackgroundItemUiModel

        setSelectedItem(isSelected = postBackgroundItemUiModel.isSelected)
    }

    fun setSelectedItem(isSelected: Boolean) {
        binding.vSelected.setVisible(isSelected)
    }

    private fun initBackground(url: String) {
        Glide.with(context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivPostSelectBackgroundItem)
    }
}

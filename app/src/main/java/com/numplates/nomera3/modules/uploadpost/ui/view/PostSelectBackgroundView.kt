package com.numplates.nomera3.modules.uploadpost.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewPostSelectBackgroundBinding
import com.numplates.nomera3.modules.appInfo.ui.entity.PostBackgroundItemUiModel
import com.numplates.nomera3.modules.uploadpost.ui.adapter.PostBackgroundAdapter

class PostSelectBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_post_select_background, this, false)
        .apply(::addView)
        .let(ViewPostSelectBackgroundBinding::bind)

    private var backgroundsAdapter: PostBackgroundAdapter? = null
    private var onBackgroundSelected: (PostBackgroundItemUiModel, Int?) -> Unit = { _, _ -> }

    fun bind(
        postBackgrounds: List<PostBackgroundItemUiModel>,
        initBackground: PostBackgroundItemUiModel?,
        onBackgroundSelected: (PostBackgroundItemUiModel, Int?) -> Unit,
        onHideBackgrounds: () -> Unit
    ) {
        this.onBackgroundSelected = onBackgroundSelected

        initBackgroundsList(postBackgrounds)
        initHide(onHideBackgrounds)
        initBackground?.let {
            backgroundsAdapter?.selectItem(initBackground)
            onBackgroundSelected.invoke(initBackground, null)
        }
    }

    fun show(inputSelection: Int, onFinish: () -> Unit = {}) {
        visible()
        val animate = TranslateAnimation(width.toFloat(), 0f, 0f, 0f)
        animate.duration = 200
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = Unit

            override fun onAnimationEnd(animation: Animation?) {
                onFinish.invoke()
            }

            override fun onAnimationRepeat(animation: Animation?) = Unit
        })
        startAnimation(animate)

        selectInitialBackground(inputSelection)
    }

    fun hide(onFinish: () -> Unit = {}) {
        val animate = TranslateAnimation(0f, width.toFloat(), 0f, 0f)
        animate.duration = 200
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = Unit

            override fun onAnimationEnd(animation: Animation?) {
                invisible()
                onFinish.invoke()
            }
            override fun onAnimationRepeat(animation: Animation?) = Unit
        })
        startAnimation(animate)
    }

    private fun initBackgroundsList(
        postBackgrounds: List<PostBackgroundItemUiModel>
    ) {
        binding.rvPostSelectBgList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            backgroundsAdapter = PostBackgroundAdapter(
                onBackgroundSelected = { selectedBackground ->
                    backgroundsAdapter?.selectItem(selectedBackground)
                    onBackgroundSelected.invoke(selectedBackground, null)
                }
            )
            adapter = backgroundsAdapter
        }

        backgroundsAdapter?.submitList(postBackgrounds)
    }

    private fun selectInitialBackground(inputSelection: Int) {
        backgroundsAdapter?.apply {
            val selectedBackground: PostBackgroundItemUiModel?

            val selectedItem = getSelectedItem()
            selectedBackground = if (selectedItem != null) {
                selectedItem
            } else {
                selectItem(position = 0)
                getSelectedItem()
            }

            selectedBackground?.let { onBackgroundSelected.invoke(selectedBackground, inputSelection) }
        }
    }

    private fun initHide(onHideBackgrounds: () -> Unit) {
        binding.ivPostSelectBgClose.setOnClickListener {
            onHideBackgrounds.invoke()
        }
    }
}

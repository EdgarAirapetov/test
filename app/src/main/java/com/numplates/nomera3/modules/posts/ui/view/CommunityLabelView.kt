package com.numplates.nomera3.modules.posts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewCommunityLabelBinding
import com.numplates.nomera3.modules.posts.ui.model.CommunityLabelEvent
import com.numplates.nomera3.modules.posts.ui.model.CommunityLabelUiModel

class CommunityLabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var eventListener: ((CommunityLabelEvent) -> Unit)? = null

    private val binding: ViewCommunityLabelBinding = LayoutInflater.from(context)
        .inflate(R.layout.view_community_label, this, false)
        .apply(::addView)
        .let(ViewCommunityLabelBinding::bind)

    fun setEventListener(listener: ((CommunityLabelEvent) -> Unit)?) {
        eventListener = listener
    }

    fun bind(uiModel: CommunityLabelUiModel) {
        val post = uiModel.post
        binding.tvCommunityPostGroupName.text = post.groupName
        if (post.groupId != null) {
            binding.tvCommunityPostGroupName.setThrottledClickListener {
                eventListener?.invoke(CommunityLabelEvent.CommunityClicked(post.groupId))
            }
        } else {
            binding.tvCommunityPostGroupName.setOnClickListener(null)
        }
    }

    fun clearResources(){
        binding.tvCommunityPostGroupName.setOnClickListener(null)
        eventListener = null
    }
}

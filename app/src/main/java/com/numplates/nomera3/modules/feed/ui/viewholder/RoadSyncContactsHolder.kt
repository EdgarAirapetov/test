package com.numplates.nomera3.modules.feed.ui.viewholder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.ItemRoadSyncContactsBinding
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.util.divider.IDividedPost

class RoadSyncContactsHolder(
    private val binding: ItemRoadSyncContactsBinding,
    private val callback: PostCallback
) : ViewHolder(binding.root), IDividedPost {

    override fun isVip() = false

    init {
        binding.btnSyncContacts.setThrottledClickListener { callback.onSyncContactsClicked() }
    }

}

package com.numplates.nomera3.modules.feed.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.buttons.UiKitButton
import com.numplates.nomera3.databinding.MeeraItemRoadSyncContactsBinding
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.util.divider.IDividedPost

class MeeraRoadSyncContactsHolder(
    binding: MeeraItemRoadSyncContactsBinding
) : RecyclerView.ViewHolder(binding.root), IDividedPost, PostCallbackHolder {

    override fun isVip() = false
    private var buttonSyncContacts: UiKitButton? = null
    private var postCallback: MeeraPostCallback? = null

    init {
        buttonSyncContacts = binding.btnSyncContacts.apply {
            setThrottledClickListener { postCallback?.onSyncContactsClicked() }
        }
    }

    override fun initCallback(meeraPostCallback: MeeraPostCallback?) {
        this.postCallback = meeraPostCallback
    }

    fun clearResources() {
        buttonSyncContacts?.setOnClickListener(null)
        buttonSyncContacts = null
    }
}

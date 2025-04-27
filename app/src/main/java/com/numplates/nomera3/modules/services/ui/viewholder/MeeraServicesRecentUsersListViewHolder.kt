package com.numplates.nomera3.modules.services.ui.viewholder

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemRecentUsersBinding
import com.numplates.nomera3.modules.services.ui.adapter.MeeraServicesRecentUsersAdapter
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecentUsersUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction

class MeeraServicesRecentUsersListViewHolder(
    binding: MeeraItemRecentUsersBinding,
    private val actionListener: (MeeraServicesUiAction) -> Unit,
) : ViewHolder(binding.root) {

    private var usersAdapter: MeeraServicesRecentUsersAdapter? = null

    init {
        binding.rvSearchRecent.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            usersAdapter = MeeraServicesRecentUsersAdapter(actionListener)
            adapter = usersAdapter
        }
        binding.btnClearRecent.setThrottledClickListener {
            actionListener.invoke(MeeraServicesUiAction.ClearRecentUsersClick)
        }
    }

    fun bind(item: MeeraServicesRecentUsersUiModel) {
        usersAdapter?.submitList(item.users)
    }

}

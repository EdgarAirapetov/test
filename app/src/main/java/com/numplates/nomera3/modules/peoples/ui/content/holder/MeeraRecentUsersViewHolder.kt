package com.numplates.nomera3.modules.peoples.ui.content.holder

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemRecentUsersBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.adapter.MeeraRecentUsersAdapter
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUsersUiEntity

class MeeraRecentUsersViewHolder(
    private val binding: MeeraItemRecentUsersBinding,
    private val actionListener: (FriendsContentActions) -> Unit,
) : BasePeoplesViewHolder<RecentUsersUiEntity, MeeraItemRecentUsersBinding>(binding) {

    private var usersAdapter: MeeraRecentUsersAdapter? = null

    init {
        binding.rvSearchRecent.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            usersAdapter = MeeraRecentUsersAdapter(actionListener)
            adapter = usersAdapter
        }
        binding.btnClearRecent.setThrottledClickListener {
            actionListener.invoke(FriendsContentActions.ClearRecentUsersUiAction)
        }
    }

    override fun bind(item: RecentUsersUiEntity) {
        super.bind(item)
        usersAdapter?.submitList(item.users)
    }

}

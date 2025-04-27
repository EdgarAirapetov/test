package com.numplates.nomera3.modules.peoples.ui.content.holder

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.SearchRecentBlockBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.adapter.RecentUsersAdapter
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUsersUiEntity

class RecentUsersViewHolder(
    private val binding: SearchRecentBlockBinding,
    private val actionListener: (FriendsContentActions) -> Unit,
) : BasePeoplesViewHolder<RecentUsersUiEntity, SearchRecentBlockBinding>(binding) {

    private var usersAdapter: RecentUsersAdapter? = null

    init {
        binding.searchRecentRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            usersAdapter = RecentUsersAdapter(actionListener)
            adapter = usersAdapter
        }
        binding.clearButton.setThrottledClickListener {
            actionListener.invoke(FriendsContentActions.ClearRecentUsersUiAction)
        }
    }

    override fun bind(item: RecentUsersUiEntity) {
        super.bind(item)
        usersAdapter?.submitList(item.users)
    }

}

package com.numplates.nomera3.modules.services.ui.viewholder

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemRecentUsersBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.services.ui.adapter.MeeraServicesCommunitiesAdapter
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesCommunitiesUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.viewmodel.MeeraServicesViewModel
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

private const val DEFAULT_THRESHOLD = 10

class MeeraServicesCommunityListViewHolder(
    private val binding: MeeraItemRecentUsersBinding,
    private val actionListener: (MeeraServicesUiAction) -> Unit,
    private val paginationHandler: RecommendedPeoplePaginationHandler
) : ViewHolder(binding.root) {

    private var communitiesAdapter: MeeraServicesCommunitiesAdapter? = null
    private var recyclerViewPaginator: RecyclerViewPaginator? = null

    init {
        initViews()
        initList()
    }

    fun bind(item: MeeraServicesCommunitiesUiModel) {
        binding.tvCount.text = item.totalCount.toString()
        communitiesAdapter?.submitList(item.communities)
    }

    fun updateCommunities(item: MeeraServicesCommunitiesUiModel) {
        binding.tvCount.text = item.totalCount.toString()
        communitiesAdapter?.submitList(item.communities)
    }

    private fun initViews() {
        binding.apply {
            btnClearRecent.setThrottledClickListener { actionListener.invoke(MeeraServicesUiAction.CommunitiesClick) }
            tvRecentUsers.text = root.context.getString(R.string.my_communities)
            btnClearRecent.text = root.context.getString(R.string.recommended_people_show_all_text)
            tvCount.visible()
        }
    }

    private fun initList() {
        communitiesAdapter = MeeraServicesCommunitiesAdapter(actionListener)
        binding.rvSearchRecent.apply {
            adapter = communitiesAdapter
            layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            initPagination()
        }
    }

    private fun initPagination() {
        recyclerViewPaginator = RecyclerViewPaginator(
            recyclerView = binding.rvSearchRecent,
            loadMore = { page ->
                paginationHandler.loadMore(
                    offsetCount = communitiesAdapter?.itemCount ?: (page * MeeraServicesViewModel.DEFAULT_PAGE_LIMIT),
                    rootAdapterPosition = absoluteAdapterPosition
                )
            },
            onLast = {
                paginationHandler.onLast()
            },
            isLoading = {
                paginationHandler.isLoading()
            }
        ).apply {
            this.threshold = DEFAULT_THRESHOLD
        }
    }
}

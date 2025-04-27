package com.numplates.nomera3.modules.services.ui.viewholder

import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.SnapHelper
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemRecommendedPeopleListBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.peoples.ui.content.decorator.BloggerMediaContentOffSetsDecorator
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.preloader.getRelatedUsersPreload
import com.numplates.nomera3.modules.peoples.ui.utils.ResetPaginationPageHandler
import com.numplates.nomera3.modules.services.ui.adapter.MeeraServicesRecommendedPeopleAdapter
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecommendedPeopleUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.viewmodel.MeeraServicesViewModel
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

private const val RECOMMENDED_PEOPLE_LIST_MARGIN = 12
private const val DEFAULT_THRESHOLD = 10

class MeeraServicesRecommendedUsersListViewHolder(
    private val binding: MeeraItemRecommendedPeopleListBinding,
    private val actionListener: (MeeraServicesUiAction) -> Unit,
    private val paginationHandler: RecommendedPeoplePaginationHandler
) : ViewHolder(binding.root), ResetPaginationPageHandler {

    private var adapter: MeeraServicesRecommendedPeopleAdapter? = null
    private var recyclerViewPaginator: RecyclerViewPaginator? = null

    init {
        initList()
        initImagePreload()
        binding.btnShowAll.setThrottledClickListener { actionListener.invoke(MeeraServicesUiAction.PeoplesClick) }
    }

    fun bind(item: MeeraServicesRecommendedPeopleUiModel) {
        adapter?.submitList(item.users)
    }

    override fun resetCurrentPage() {
        recyclerViewPaginator?.resetCurrentPage()
    }

    fun submitRecommendations(newList: List<RecommendedPeopleUiEntity>) {
        adapter?.submitList(newList)
    }

    fun getContext() = binding.root.context

    fun resetPage() = recyclerViewPaginator?.resetCurrentPage()

    fun scrollToUser(userId: Long) {
        val position = adapter?.currentList?.indexOfFirst { it.userId == userId } ?: return
        if (position < 0) return
        binding.rvRecommendedPeople.smoothScrollToPosition(position)
    }

    private fun initList() {
        adapter = MeeraServicesRecommendedPeopleAdapter(actionListener)
        binding.rvRecommendedPeople.adapter = adapter
        binding.rvRecommendedPeople.setHasFixedSize(true)
        binding.rvRecommendedPeople.addItemDecoration(
            BloggerMediaContentOffSetsDecorator(
                mTop = RECOMMENDED_PEOPLE_LIST_MARGIN,
                mLeft = RECOMMENDED_PEOPLE_LIST_MARGIN
            )
        )
        binding.rvRecommendedPeople.isNestedScrollingEnabled = false
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvRecommendedPeople)
        initPagination()
    }

    private fun initPagination() {
        recyclerViewPaginator = RecyclerViewPaginator(
            recyclerView = binding.rvRecommendedPeople,
            loadMore = { page ->
                paginationHandler.loadMore(
                    offsetCount = adapter?.itemCount ?: (page * MeeraServicesViewModel.DEFAULT_PAGE_LIMIT),
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

    private fun initImagePreload() {
        val relatedUsersAdapter = adapter ?: return
        val preloader = getRelatedUsersPreload(relatedUsersAdapter)
        binding.rvRecommendedPeople.addOnScrollListener(preloader)
    }
}

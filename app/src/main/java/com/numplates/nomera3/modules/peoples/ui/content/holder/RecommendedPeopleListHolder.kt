package com.numplates.nomera3.modules.peoples.ui.content.holder

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.numplates.nomera3.databinding.ItemRecommendedPeopleListBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.peoples.ui.content.adapter.RecommendedPeopleAdapter
import com.numplates.nomera3.modules.peoples.ui.content.decorator.BloggerMediaContentOffSetsDecorator
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.preloader.getRelatedUsersPreload
import com.numplates.nomera3.modules.peoples.ui.utils.ResetPaginationPageHandler
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

private const val RECOMMENDED_PEOPLE_LIST_MARGIN = 12
private const val DEFAULT_THRESHOLD = 50

class RecommendedPeopleListHolder(
    private val binding: ItemRecommendedPeopleListBinding,
    private val actionListener: (FriendsContentActions) -> Unit,
    private val paginationHandler: RecommendedPeoplePaginationHandler
) : BasePeoplesViewHolder<RecommendedPeopleListUiEntity, ItemRecommendedPeopleListBinding>(binding),
    ResetPaginationPageHandler {

    private var adapter: RecommendedPeopleAdapter? = null
    private var recyclerViewPaginator: RecyclerViewPaginator? = null

    init {
        initList()
        initImagePreload()
    }

    override fun bind(item: RecommendedPeopleListUiEntity) {
        super.bind(item)
        adapter?.submitList(item.recommendedPeopleList)
        binding.tvRecommendedPeople.isVisible = item.showPossibleFriendsText
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
        adapter = RecommendedPeopleAdapter(actionListener)
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
                    offsetCount = page,
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

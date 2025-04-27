package com.numplates.nomera3.modules.peoples.ui.content.holder

import androidx.core.view.isVisible
import androidx.recyclerview.widget.SnapHelper
import com.meera.core.extensions.invisible
import com.numplates.nomera3.databinding.MeeraItemRecommendedPeopleListBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.peoples.ui.content.adapter.MeeraRecommendedPeopleAdapter
import com.numplates.nomera3.modules.peoples.ui.content.decorator.BloggerMediaContentOffSetsDecorator
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.preloader.getRelatedUsersPreload
import com.numplates.nomera3.modules.peoples.ui.utils.ResetPaginationPageHandler
import com.numplates.nomera3.modules.peoples.ui.viewmodel.MeeraPeoplesViewModel
import com.numplates.nomera3.presentation.view.ui.StartSnapHelper
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator


private const val RECOMMENDED_PEOPLE_LIST_MARGIN = 12
private const val DEFAULT_THRESHOLD = 50

class MeeraRecommendedPeopleListHolder(
    private val binding: MeeraItemRecommendedPeopleListBinding,
    private val actionListener: (FriendsContentActions) -> Unit,
    private val paginationHandler: RecommendedPeoplePaginationHandler
) : BasePeoplesViewHolder<RecommendedPeopleListUiEntity, MeeraItemRecommendedPeopleListBinding>(binding),
    ResetPaginationPageHandler {

    private var adapter: MeeraRecommendedPeopleAdapter? = null
    private var recyclerViewPaginator: RecyclerViewPaginator? = null

    init {
        initList()
        initImagePreload()
    }

    override fun bind(item: RecommendedPeopleListUiEntity) {
        super.bind(item)
        adapter?.submitList(item.recommendedPeopleList)
        binding.tvRecommendedPeople.isVisible = item.showPossibleFriendsText
        binding.btnShowAll.invisible()
    }

    override fun resetCurrentPage() {
        recyclerViewPaginator?.resetCurrentPage()
    }

    fun submitRecommendations(newList: List<RecommendedPeopleUiEntity>) {
        adapter?.submitList(newList)
    }

    fun getContext() = binding.root.context

    fun scrollToUser(userId: Long) {
        val position = adapter?.currentList?.indexOfFirst { it.userId == userId } ?: return
        if (position < 0) return
        binding.rvRecommendedPeople.smoothScrollToPosition(position)
    }

    private fun initList() {
        adapter = MeeraRecommendedPeopleAdapter(actionListener)
        binding.rvRecommendedPeople.adapter = adapter
        binding.rvRecommendedPeople.setHasFixedSize(true)
        binding.rvRecommendedPeople.addItemDecoration(
            BloggerMediaContentOffSetsDecorator(
                mLeft = RECOMMENDED_PEOPLE_LIST_MARGIN
            )
        )
        binding.rvRecommendedPeople.isNestedScrollingEnabled = false
        val snapHelper: SnapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvRecommendedPeople)
        initPagination()
    }

    private fun initPagination() {
        recyclerViewPaginator = RecyclerViewPaginator(
            recyclerView = binding.rvRecommendedPeople,
            loadMore = { page ->
                paginationHandler.loadMore(
                    offsetCount = adapter?.itemCount ?: (page * MeeraPeoplesViewModel.DEFAULT_PAGE_LIMIT),
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

package com.numplates.nomera3.modules.reactionStatistics.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentReactionsStatisticsPageBinding
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment.Companion.ARG_ENTITY_ID
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment.Companion.ARG_ENTITY_TYPE
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment.Companion.ARG_PAGE_REACTION
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment.Companion.ARG_PAGE_VIEWERS
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionUserUiEntity
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment

class MeeraReactionsPageFragment : MeeraBaseFragment(
    layout = R.layout.fragment_reactions_statistics_page
) {

    private val binding by viewBinding(FragmentReactionsStatisticsPageBinding::bind)
    private val entityId: Long by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getLong(ARG_ENTITY_ID)
    }

    private val entityType: ReactionsEntityType by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().get(ARG_ENTITY_TYPE) as ReactionsEntityType
    }

    private val isViewersPage: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getBoolean(ARG_PAGE_VIEWERS)
    }

    private val reaction: String? by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_PAGE_REACTION)
    }

    private val adapterMeera: MeeraReactionsAdapter by lazy { MeeraReactionsAdapter(::onUserClicked) }

    fun getRecyclerView() = binding.rvReactions

    private val viewModel by viewModels<ReactionsPageViewModel> {
        App.component.getViewModelFactory()
    }

    private var _clickListener: ((userEntity: ReactionUserUiEntity) -> Unit)? = null

    private fun onUserClicked(userEntity: ReactionUserUiEntity) {
        _clickListener?.invoke(userEntity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.let {
            it.rvReactions.adapter = adapterMeera

            it.rvReactions.addOnScrollListener(
                object : RecyclerPaginationListener(it.rvReactions.layoutManager as LinearLayoutManager) {
                    override fun loadMoreItems() = viewModel.loadMore()

                    override fun isLastPage(): Boolean = viewModel.isLastPage()

                    override fun isLoading(): Boolean = viewModel.isLoading()
                })
        }
        initPage()
        initObservers()
    }

    private fun initPage() {
        if (isViewersPage) {
            viewModel.initViewersPage(entityId, entityType)
        } else {
            viewModel.initReactionsPage(entityId, entityType, reaction)
        }
    }

    fun initObservers() {
        viewModel.liveReactionUsersState.observe(viewLifecycleOwner) {
            binding.sflReactionUsers.gone()
            adapterMeera.submitList(it)
        }
    }

    companion object {
        fun getInstance(
            entityId: Long,
            entityType: ReactionsEntityType,
            reaction: String? = null,
            viewsPage: Boolean = false,
            clickListener: (userEntity: ReactionUserUiEntity) -> Unit
        ) =
            MeeraReactionsPageFragment().apply {
                arguments = bundleOf(
                    ARG_ENTITY_ID to entityId,
                    ARG_ENTITY_TYPE to entityType,
                    ARG_PAGE_REACTION to reaction,
                    ARG_PAGE_VIEWERS to viewsPage,
                )
                _clickListener = clickListener
            }
    }
}

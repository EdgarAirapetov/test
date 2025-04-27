package com.numplates.nomera3.modules.reactionStatistics.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.gone
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.FragmentReactionsStatisticsPageBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment.Companion.ARG_ENTITY_ID
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment.Companion.ARG_ENTITY_TYPE
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment.Companion.ARG_PAGE_REACTION
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment.Companion.ARG_PAGE_VIEWERS
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionUserUiEntity
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment

class ReactionsPageFragment : BaseFragmentNew<FragmentReactionsStatisticsPageBinding>() {


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

    private val adapter: ReactionsAdapter by lazy { ReactionsAdapter(::onUserClicked) }

    private val adapterMeera: MeeraReactionsAdapter by lazy { MeeraReactionsAdapter(::onUserClicked) }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentReactionsStatisticsPageBinding
        get() = FragmentReactionsStatisticsPageBinding::inflate

    fun getRecyclerView() = binding?.rvReactions

    private val viewModel by viewModels<ReactionsPageViewModel> {
        App.component.getViewModelFactory()
    }

    fun onUserClicked(userEntity: ReactionUserUiEntity) {
        add(
            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(IArgContainer.ARG_USER_ID, userEntity.userId),
            Arg(IArgContainer.ARG_PAGER_PROFILE, false),
            Arg(IArgContainer.ARG_OPEN_FROM_REACTIONS, true),
            Arg(ARG_ENTITY_ID, entityId),
            Arg(ARG_ENTITY_TYPE, entityType),
            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.STATISTIC_REACTIONS.property)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let {
            checkAppRedesigned (
                isRedesigned = {
                    it.rvReactions.adapter = adapterMeera
                },
                isNotRedesigned = {
                    it.rvReactions.adapter = adapter
                }
            )


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
            binding?.sflReactionUsers?.gone()
            checkAppRedesigned(
                isRedesigned = {
                    adapterMeera.submitList(it)
                },
                isNotRedesigned = {
                    adapter.submitList(it)
                }
            )
        }
    }

    companion object {
        fun getInstance(
            entityId: Long,
            entityType: ReactionsEntityType,
            reaction: String? = null,
            viewsPage: Boolean = false
        ) =
            ReactionsPageFragment().apply {
                arguments = bundleOf(
                    ARG_ENTITY_ID to entityId,
                    ARG_ENTITY_TYPE to entityType,
                    ARG_PAGE_REACTION to reaction,
                    ARG_PAGE_VIEWERS to viewsPage,
                )
            }
    }
}

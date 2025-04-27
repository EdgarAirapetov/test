package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonError
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMyFriendListFragmentBinding
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FRIEND_LIST_MODE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PAGER_PROFILE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendsListAction
import com.numplates.nomera3.presentation.view.adapter.newfriends.MeeraIncomingFriendsListAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraCellIncomingShimmerAdapter
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.viewmodel.MeeraMyFriendListFragmentViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsListViewEvents

private const val COUNT_SHIMMER_ITEM = 8

class MeeraIncomingFriendListFragment : MeeraBaseFragment(
    layout = R.layout.meera_my_friend_list_fragment,
), MeeraPullToRefreshLayout.OnRefreshListener {

    companion object {

        fun newInstance(
            mode: Int,
            userID: Long,
            openedType: FriendsHostOpenedType = FriendsHostOpenedType.OTHER
        ): MeeraIncomingFriendListFragment {
            val args = Bundle()
            args.putInt(ARG_FRIEND_LIST_MODE, mode)
            args.putLong(ARG_USER_ID, userID)
            args.putSerializable(ARG_FRIENDS_HOST_OPENED_FROM, openedType)
            val fragment = MeeraIncomingFriendListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val binding by viewBinding(MeeraMyFriendListFragmentBinding::bind)
    private lateinit var viewModel: MeeraMyFriendListFragmentViewModel

    private var mode: Int = GetFriendsListUseCase.FRIENDS
    private var userID: Long? = null

    private var adapter: MeeraIncomingFriendsListAdapter? = null
    private val shimmerAdapter = MeeraCellIncomingShimmerAdapter()
    private val listShimmer = List(COUNT_SHIMMER_ITEM) { "" }
    var friendInteractor: IOnIncomingFriendsInteractor? = null

    private val openedType: FriendsHostOpenedType by lazy {
        val arg = arguments?.get(ARG_FRIENDS_HOST_OPENED_FROM) as? FriendsHostOpenedType
        arg ?: FriendsHostOpenedType.OTHER
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->

            args.get(ARG_FRIEND_LIST_MODE)?.let {
                mode = it as Int
            }

            args.get(ARG_USER_ID)?.let {
                userID = it as Long
            } ?: kotlin.run {
                userID = viewModel.getUserUid()
            }
        }

        viewModel = ViewModelProviders.of(this)
            .get(MeeraMyFriendListFragmentViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initListeners()
        initObservers()
        initSwipeRefresh()

        viewModel.init(userID, mode)

        binding?.rvShimmerFriendsList?.visible()
        binding?.rvMyFriendsList?.gone()
    }

    private fun initRecycler() {
        binding?.rvMyFriendsList?.layoutManager = LinearLayoutManager(context)
        adapter = MeeraIncomingFriendsListAdapter(::initFriendClickListener)
        binding?.rvMyFriendsList?.adapter = adapter

        binding?.rvShimmerFriendsList?.layoutManager = LinearLayoutManager(context)
        binding?.rvShimmerFriendsList?.adapter = shimmerAdapter
        shimmerAdapter.submitList(listShimmer)
    }

    private fun initSwipeRefresh() {
        binding?.srMyFriendsList?.setOnRefreshListener(this)
        binding?.srMyFriendsList?.setRefreshEnable(true)
    }

    private fun initObservers() {
        viewModel.liveIncomingFriends.observe(viewLifecycleOwner, Observer {
            binding.placeholderNoFriendsSearchResult.gone()
            adapter?.submitList(it)
            binding?.srMyFriendsList?.setRefreshing(false)
            binding?.rvShimmerFriendsList?.gone()
            binding?.rvMyFriendsList?.visible()
            friendInteractor?.onInComing(it.size)
            if (adapter?.itemCount == 0 && it.size == 0)
                showPlaceHolderIfEmptyAdapter()
            else binding?.placeholderNoFriends?.gone()
        })

        viewModel.liveSearch.observe(viewLifecycleOwner, Observer {
            binding?.placeholderNoFriends?.gone()
            adapter?.submitList(it)
            if (it.size > 0) {
                binding.placeholderNoFriendsSearchResult.gone()
            } else {
                binding.placeholderNoFriendsSearchResult.visible()
            }
        })

        viewModel.liveEvent.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is FriendsListViewEvents.OnClearSearchAdapter -> {
                    }

                    is FriendsListViewEvents.OnErrorAddFriend -> {
                        showCommonError(getText(R.string.error_try_later), requireView())
                    }

                    is FriendsListViewEvents.OnErrorRemoveFriend -> {
                        showCommonError(getText(R.string.error_try_later), requireView())
                    }

                    is FriendsListViewEvents.OnFriendRejected -> {
                        friendInteractor?.onInComing(adapter?.itemCount ?: 0)
                    }

                    is FriendsListViewEvents.HasIncomingRequests -> {
                        showPlaceHolderIfEmptyAdapter()
                    }

                    is FriendsListViewEvents.NoIncomigRequests -> {
                        showPlaceHolderIfEmptyAdapter()
                    }

                    is FriendsListViewEvents.HasFriendsEvent -> {
                        showPlaceHolderIfEmptyAdapter()
                    }

                    is FriendsListViewEvents.NoFriendsEvent -> {
                        showPlaceHolderIfEmptyAdapter()
                    }

                    else -> return@let
                }
            }
        })
    }

    private fun initFriendClickListener(action: FriendsListAction) {
        when (action) {
            is FriendsListAction.OpenProfileClick -> {
                findNavController().safeNavigate(
                    resId = R.id.action_meeraIncomingFriendListFragment_to_userInfoFragment,
                    bundle = bundleOf(
                        ARG_USER_ID to action.userId,
                        ARG_PAGER_PROFILE to false,
                        ARG_TRANSIT_FROM to AmplitudePropertyWhere.FRIEND.property
                    )
                )
            }

            is FriendsListAction.ConfirmFriendClick -> {
                viewModel.onActionClicked(friend = action.model, openedType = openedType)
            }

            is FriendsListAction.RejectFriendClick -> {
                viewModel.rejectFriend(friend = action.model, openedType = openedType)
            }

            else -> Unit
        }
    }

    private fun showPlaceHolderIfEmptyAdapter() {
        if (adapter?.itemCount == 0) {
            binding?.tvPlaceholderDescription?.text = getString(R.string.incoming_friends_list_is_empty)
            binding?.btnSearchFriends?.gone()
            binding?.placeholderNoFriends?.visible()
        } else {
            binding?.placeholderNoFriends?.gone()
        }
    }

    fun search(query: String) {
        viewModel.search(query)
    }

    override fun onRefresh() {
        binding?.rvShimmerFriendsList?.gone()
        binding?.rvMyFriendsList?.visible()
        adapter?.submitList(emptyList())
        viewModel.onRefresh()
    }

    private fun initListeners() {
        binding?.btnSearchFriends?.setOnClickListener {
            viewModel.logMyFriendsPeopleSelected()
//            add(
//                PeoplesCommunitiesContainerFragment(),
//                Act.LIGHT_STATUSBAR
//            )
        }
    }

    interface IOnIncomingFriendsInteractor {

        fun onInComing(count: Int) {}
        fun onNewFriend() {}
    }
}

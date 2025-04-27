package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentMyFriendListNewBinding
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesCommunitiesContainerFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FRIEND_LIST_MODE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PAGER_PROFILE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendsListAdapter
import com.numplates.nomera3.presentation.view.fragments.dialogs.createSubscribedFriendRemovalDialog
import com.numplates.nomera3.presentation.view.fragments.dialogs.createUnsubscribedFriendRemovalDialog
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.FRIEND_REQUEST_PAGE_LIMIT
import com.numplates.nomera3.presentation.viewmodel.MyFriendListFragmentNewViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsListViewEvents
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import timber.log.Timber

class MyFriendListFragmentNew : BaseFragmentNew<FragmentMyFriendListNewBinding>() {

    companion object {

        fun newInstance(
            mode: Int,
            userID: Long,
            openedType: FriendsHostOpenedType = FriendsHostOpenedType.OTHER
        ): MyFriendListFragmentNew {
            val args = Bundle()
            args.putInt(ARG_FRIEND_LIST_MODE, mode)
            args.putLong(ARG_USER_ID, userID)
            args.putSerializable(ARG_FRIENDS_HOST_OPENED_FROM, openedType)
            val fragment = MyFriendListFragmentNew()
            fragment.arguments = args
            return fragment
        }

    }

    private lateinit var viewModel: MyFriendListFragmentNewViewModel

    private var mode: Int = GetFriendsListUseCase.FRIENDS
    private var userID: Long? = null

    private lateinit var searchAdapter: FriendsListAdapter
    private lateinit var adapter: FriendsListAdapter

    private var isSearch = false
    private var searchQueryLen = 0
    var friendInteractor: IOnMyFriendsInteractor? = null

    private val openedType: FriendsHostOpenedType by lazy {
        val arg = arguments?.get(ARG_FRIENDS_HOST_OPENED_FROM) as? FriendsHostOpenedType
        arg ?: FriendsHostOpenedType.OTHER
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MyData: $openedType")
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
            .get(MyFriendListFragmentNewViewModel::class.java)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initListeners()
        initObservers()
        initSwipeRefresh()

        viewModel.init(userID, mode)

        binding?.pbMyFriends?.visible()
    }

    private fun initRecycler() {
        binding?.rvMyFriendsList?.layoutManager = LinearLayoutManager(context)
        adapter = FriendsListAdapter(mutableListOf())
        binding?.rvMyFriendsList?.adapter = adapter
        binding?.rvMyFriendsList?.addItemDecoration(
            HorizontalLineDivider(
                dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.shared_divider_item_shape)!!,
                paddingLeft = 20.dp,
                paddingRight = 20.dp
            )
        )
        searchAdapter = FriendsListAdapter(mutableListOf())
        searchAdapter.setSearch(true)

        val interactor = AdapterInteractor()
        adapter.interactor = interactor
        searchAdapter.interactor = interactor

        initPagination()
    }

    private fun initPagination() {
        if (mode == GetFriendsListUseCase.FRIENDS) {
            binding?.rvMyFriendsList?.let { rv ->
                RecyclerViewPaginator(
                    recyclerView = rv,
                    onLast = {
                        if (!isSearch) viewModel.isLastFriend()
                        else viewModel.isLastIncoming()
                    },
                    isLoading = {
                        if (!isSearch) viewModel.isLoadingFriend()
                        else viewModel.isLoadingIncoming()
                    },
                    loadMore = {
                        if (!isSearch) viewModel.requestAllFriends(FRIEND_REQUEST_PAGE_LIMIT, adapter.itemCount)
                    }
                ).apply {
                    /*
                    * https://nomera.atlassian.net/browse/BR-3306
                    * при скролле списка друзей начинал тормозить скролл, сделал threshold = 50,
                    * чтоб дозагрузка происходила позже. Возможно нужно еще увеличить threshold
                    * потому что запрашивается 1000 друзей?
                    * */
                    this.threshold = 50
                }
            }
        }
    }

    private fun initSwipeRefresh() {
        binding?.srMyFriendsList?.setOnRefreshListener {
            onRefresh()
        }
    }

    private fun initObservers() {
        viewModel.liveRemoveFriend.observe(viewLifecycleOwner, Observer {
            if (isSearch) searchAdapter.removeItem(it)
            adapter.removeItem(it)

            if (adapter.itemCount == 0) binding?.placeholderNoFriends?.visible()
        })

        viewModel.liveAllFriends.observe(viewLifecycleOwner, Observer {
            adapter.addElements(it)
            binding?.srMyFriendsList?.isRefreshing = false
            binding?.pbMyFriends?.gone()
            if (adapter.itemCount == 0 && it.size == 0) binding?.placeholderNoFriends?.visible()
            else binding?.placeholderNoFriends?.gone()
        })

        viewModel.liveIncomingFriends.observe(viewLifecycleOwner, Observer {
            adapter.addElements(it)
            binding?.srMyFriendsList?.isRefreshing = false
            binding?.pbMyFriends?.gone()
            friendInteractor?.onInComing(it.size)
        })

        viewModel.liveSearch.observe(viewLifecycleOwner, Observer {
            searchAdapter.clear()
            searchAdapter.addElements(it)

            if (it.isEmpty() && searchQueryLen > 0) binding?.placeholderNoFriendsSearchResult?.visible()
            else binding?.placeholderNoFriendsSearchResult?.gone()
        })

        viewModel.liveEvent.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is FriendsListViewEvents.OnClearSearchAdapter -> searchAdapter.clear()
                    is FriendsListViewEvents.OnErrorAddFriend -> {
                        NToast.with(view)
                            .text(getString(R.string.error_try_later))
                            .show()
                        Timber.e(" OnErrorAddFriend")
                    }
                    is FriendsListViewEvents.OnErrorRemoveFriend -> {
                        NToast.with(view)
                            .text(getString(R.string.error_try_later))
                            .show()
                        Timber.e(" OnErrorRemoveFriend")
                    }
                    is FriendsListViewEvents.OnFriendRejected -> {
                        friendInteractor?.onInComing(adapter.itemCount)
                    }

                    is FriendsListViewEvents.HasIncomingRequests -> {
                        binding?.placeholderNoIncomingFriends?.gone()
                    }
                    is FriendsListViewEvents.NoIncomigRequests -> {
                        binding?.placeholderNoIncomingFriends?.visible()
                    }
                    is FriendsListViewEvents.HasFriendsEvent -> {
                        binding?.placeholderNoFriends?.gone()
                    }
                    is FriendsListViewEvents.NoFriendsEvent -> {
                        binding?.placeholderNoFriends?.visible()
                    }

                    else -> return@let
                }
            }
        })

        viewModel.liveNewFriend.observe(viewLifecycleOwner, Observer {
            if (isSearch) searchAdapter.removeItem(it)
            adapter.removeItem(it)
            friendInteractor?.onInComing(adapter.itemCount)

            if (adapter.itemCount == 0) binding?.placeholderNoIncomingFriends?.visible()

            friendInteractor?.onNewFriend()

        })

        viewModel.liveDeleteFriend.observe(viewLifecycleOwner, Observer {
            if (isSearch) searchAdapter.removeItem(it)

            adapter.removeItem(it)

            showPlaceHolderIfEmptyAdapter()

        })
    }

    private fun showPlaceHolderIfEmptyAdapter() {
        if (adapter.itemCount == 0) {
            if (mode == GetFriendsListUseCase.FRIENDS) {
                binding?.placeholderNoFriends?.visible()
            } else if (mode == GetFriendsListUseCase.INCOMING) {
                binding?.placeholderNoIncomingFriends?.visible()
            }
        }
    }

    fun search(query: String) {
        searchQueryLen = query.length
        if (mode == GetFriendsListUseCase.FRIENDS) viewModel.searchFriendNetwork(query)
        else viewModel.search(query)
    }


    fun onStartSearch() {
        Timber.e("Start search")
        searchAdapter.clear()
        binding?.rvMyFriendsList?.adapter = searchAdapter
        binding?.srMyFriendsList?.isEnabled = false
        binding?.pbMyFriends?.gone()
        viewModel.onStartSearch()
        isSearch = true
        // Placeholder (no search results)
        binding?.placeholderNoIncomingFriends?.gone()
        binding?.placeholderNoFriends?.gone()
        //placeholder_no_friends_search_result.visible()
    }


    fun onCloseSearch() {
        binding?.srMyFriendsList?.isEnabled = true
        binding?.pbMyFriends?.gone()
        binding?.rvMyFriendsList?.adapter = adapter
        viewModel.onStopSearch()
        isSearch = false
        // Placeholder (no search results)

        binding?.placeholderNoFriendsSearchResult?.gone()
        showPlaceHolderIfEmptyAdapter()
    }

    fun onRefresh() {
        binding?.srMyFriendsList?.isRefreshing = false
        binding?.pbMyFriends?.gone()
        adapter.clear()
        viewModel.onRefresh()
    }

    private fun showActionMenu(friend: FriendModel) {
        val isSubscribedOn = friend.userSimple?.settingsFlags?.subscription_on ?: 0 == 1
        when (friend.type) {
            GetFriendsListUseCase.FRIENDS -> {
                if (isSubscribedOn) {
                    requireContext().createSubscribedFriendRemovalDialog(
                        cancelRequest = { viewModel.removeFriendSaveSubscription(friend) },
                        cancelRequestAndUnsubscribe = { viewModel.removeFriend(friend) }
                    ).show(childFragmentManager)
                } else {
                    requireContext().createUnsubscribedFriendRemovalDialog(
                        friend = friend,
                        cancelRequest = { viewModel.removeFriend(friend) }
                    ).show(childFragmentManager)
                }
            }
        }
    }

    private fun handleActionButtonsIncomingFriends(friend: FriendModel, isAccept: Boolean) {
        if (isAccept) viewModel.onActionClicked(friend = friend, openedType = openedType)
        else viewModel.rejectFriend(friend = friend, openedType = openedType)
    }

    private fun initListeners() {
        binding?.tvSearchFriends?.setOnClickListener {
            viewModel.logMyFriendsPeopleSelected()
            add(
                PeoplesCommunitiesContainerFragment(),
                Act.LIGHT_STATUSBAR
            )
        }
    }

    inner class AdapterInteractor : FriendsListAdapter.IFriendsListInteractor {
        override fun onActionClicked(friend: FriendModel) {
            // https://overflow.io/s/87FE6M?node=d19d7f4a&flight=off
            // https://overflow.io/s/87FE6M?node=7b97d82a
            // https://app.zeplin.io/project/5c5861194e168142a93344a1/screen/5edd66a847b46c984f9fa928
            showActionMenu(friend)
        }

        override fun onItemClicked(friend: FriendModel) {
            add(
                UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(ARG_USER_ID, friend.userModel.userId),
                Arg(ARG_PAGER_PROFILE, false),
                Arg(ARG_TRANSIT_FROM, AmplitudePropertyWhere.FRIEND.property)
            )
        }

        override fun onActionButtonsClicked(friend: FriendModel, isAccept: Boolean) {
            handleActionButtonsIncomingFriends(friend, isAccept)
        }
    }

    interface IOnMyFriendsInteractor {

        fun onInComing(count: Int) {}

        fun onNewFriend() {}
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyFriendListNewBinding
        get() = FragmentMyFriendListNewBinding::inflate
}

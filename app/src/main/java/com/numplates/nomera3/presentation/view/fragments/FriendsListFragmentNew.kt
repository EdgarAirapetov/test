package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentFriendsListNewBinding
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendsListAdapter
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.utils.NToast
import com.meera.core.extensions.visible
import com.meera.core.extensions.gone
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FRIEND_LIST_MODE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PAGER_PROFILE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.viewmodel.MyFriendListFragmentNewViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsListViewEvents
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

class FriendsListFragmentNew : BaseFragmentNew<FragmentFriendsListNewBinding>(), FriendsListAdapter.IFriendsListInteractor {

    private var paginator: RecyclerViewPaginator? = null
    private lateinit var viewModel: MyFriendListFragmentNewViewModel
    private lateinit var adapter: FriendsListAdapter
    private var mode = GetFriendsListUseCase.FRIENDS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            args.get(ARG_FRIEND_LIST_MODE)?.let {
                mode = it as Int
            }
        }

        viewModel = ViewModelProviders.of(this)
                .get(MyFriendListFragmentNewViewModel::class.java)
    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFriendsListNewBinding
        get() = FragmentFriendsListNewBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initObservers()
        binding?.srlFriendsListNew?.isRefreshing = true
        binding?.srlFriendsListNew?.setOnRefreshListener {
            adapter.clear()
            paginator?.resetCurrentPage()
            viewModel.onRefresh()
        }
        viewModel.init(viewModel.getUserUid(), mode)

    }

    private fun initObservers() {
        //Все друзья
        viewModel.liveAllFriends.observe(viewLifecycleOwner, Observer {
            adapter.addElements(it)
            binding?.srlFriendsListNew?.isRefreshing = false
            checkAdapterIsEmpty()
        })

        //Входящие
        viewModel.liveIncomingFriends.observe(viewLifecycleOwner, Observer {
            adapter.addElements(it)
            binding?.srlFriendsListNew?.isRefreshing = false
            checkAdapterIsEmpty()
        })

        //Черный список
        viewModel.liveBlackList.observe(viewLifecycleOwner, Observer {
            adapter.addElements(it)
            binding?.srlFriendsListNew?.isRefreshing = false

            // Placeholder
            if (it.isEmpty()) {
                binding?.placeholderNoFriendsBlacklist?.visible()
            } else
                binding?.placeholderNoFriendsBlacklist?.gone()
        })

        //Исходящие заявки
        viewModel.liveOutcomingFriends.observe(viewLifecycleOwner, Observer {
            adapter.addElements(it)
            binding?.srlFriendsListNew?.isRefreshing = false

            // Placeholder
            if (it.isEmpty()) {
                binding?.placeholderOutgoingRequest?.visible()
            } else
                binding?.placeholderOutgoingRequest?.gone()
        })

        viewModel.liveEvent.observe(viewLifecycleOwner, Observer {
            when (it) {
                is FriendsListViewEvents.OnClearSearchAdapter -> {
                    adapter.clear()
                }
                is FriendsListViewEvents.OnErrorAction -> {
                    binding?.srlFriendsListNew?.isRefreshing = false
                    NToast.with(view)
                            .text(getString(R.string.error_try_later))
                            .show()
                }
                else -> {}
            }
        })

        viewModel.liveRemoveItem.observe(viewLifecycleOwner, Observer {
            adapter.removeItem(it)
            checkAdapterIsEmpty()
        })

        viewModel.showCancelOutcomeFriendshipRequestDialog.observe(viewLifecycleOwner, Observer {
            ConfirmDialogBuilder()
                    .setHeader(getString(R.string.cancel_request_question))
                    .setDescription(getString(R.string.your_friend_request_will_be_canceled))
                    .setLeftBtnText(getString(R.string.close_caps))
                    .setRightBtnText(getString(R.string.cancel_caps))
                    .setRightClickListener {
                        viewModel.cancelOutcomeFriendshipRequest(it)
                    }
                    .show(childFragmentManager)

        })

        viewModel.showCancelOutcomeFriendshipRequestUnSubscribeDialog.observe(viewLifecycleOwner, Observer {
            ConfirmDialogBuilder()
                    .setHeader(getString(R.string.cancel_request_question))
                    .setDescription(getString(R.string.your_friend_request_will_be_canceled))
                    .setHorizontal(true)
                    .setTopBtnText(getString(R.string.cancel_request_caps))
                    .setMiddleBtnText(getString(R.string.cancel_request_unsubscribe_caps))
                    .setBottomBtnText(getString(R.string.cancel_caps))
                    .setTopClickListener {
                        viewModel.cancelOutcomeFriendshipRequest(it)
                    }
                    .setMiddleClickListener {
                        viewModel.cancelOutcomeFriendshipRequestUnSubscribe(it)
                    }
                    .show(childFragmentManager)
        })
    }

    private fun initRecycler() {
        adapter = FriendsListAdapter(mutableListOf())
        binding?.rvFriendsListNew?.layoutManager = LinearLayoutManager(context)
        binding?.rvFriendsListNew?.adapter = adapter
        adapter.interactor = this
    }

    private fun checkAdapterIsEmpty() {
//        if (adapter.itemCount == 0)
//            placeholder_default_friend_list.visible()
//        else placeholder_default_friend_list.gone()

        if (adapter.itemCount == 0) {
            if (mode == GetFriendsListUseCase.BLACKLIST) {
                binding?.placeholderNoFriendsBlacklist?.visible()
            } else if (mode == GetFriendsListUseCase.OUTCOMING) {
                binding?.placeholderOutgoingRequest?.visible()
            }
        }
    }

    //adapterInteractor
    override fun onItemClicked(friend: FriendModel) {
        add(UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(ARG_USER_ID, friend.userModel.userId),
                Arg(ARG_PAGER_PROFILE, false))
    }

    override fun onActionClicked(friend: FriendModel) {
        viewModel.onActionClicked(friend, openedType = FriendsHostOpenedType.OTHER)
    }

    override fun onActionButtonsClicked(friend: FriendModel, isAccept: Boolean) {
        /** STUB */
    }
}

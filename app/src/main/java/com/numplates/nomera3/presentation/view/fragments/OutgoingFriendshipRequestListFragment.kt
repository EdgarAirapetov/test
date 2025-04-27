package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.databinding.FragmentOutgoingFriendshipRequestListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PAGER_PROFILE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.fragments.dialogs.createSubscribedFriendCancelRequest
import com.numplates.nomera3.presentation.view.fragments.dialogs.createUnsubscribedFriendRemovalDialog
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.OutgoingFriendshipRequestListViewModel
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

// todo переделать под абстрактный класс
class OutgoingFriendshipRequestListFragment : BaseFragmentNew<FragmentOutgoingFriendshipRequestListBinding>(),
        OutgoingFriendshipRequestsClickListener {

    private val viewModel: OutgoingFriendshipRequestListViewModel by viewModels()

    // для списка результатов поиска
    private lateinit var searchResultsAdapter: OutgoingFriendshipRequestsAdapter

    // для основного списка
    private lateinit var requestsAdapter: OutgoingFriendshipRequestsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRequestsList()

        initSearchResultList()

        initPlaceholders()

        subscribeOnLiveData()

        binding?.rvFriendsListNew?.let {
            val requestsPaginator = RecyclerViewPaginator(
                    recyclerView = it,
                    onLast = {
                        viewModel.isRequestsEnd()
                    },
                    isLoading = {
                        viewModel.isRequestsLoading()
                    },
                    loadMore = {
                        viewModel.getOutgoingFriendRequestList(offset = requestsAdapter.itemCount)
                    }
            )
            requestsPaginator.endWithAuto = true
        }

        binding?.filteredOutgoingRequests?.let {
            val foundRequestsPaginator = RecyclerViewPaginator(
                    recyclerView = it,
                    onLast = {
                        viewModel.isFoundRequestsEnd()
                    },
                    isLoading = {
                        viewModel.isFoundRequestsLoading()
                    },
                    loadMore = {
                        viewModel.search(viewModel.getLastQuery(), searchResultsAdapter.itemCount)
                    }
            )
            foundRequestsPaginator.endWithAuto = true
        }

        viewModel.setRequestsPlaceholderVisible(isVisible = false)
        viewModel.setSearchResultVisible(isVisible = false)
        viewModel.setRequestsVisible(isVisible = true)

        viewModel.resetParams()
        viewModel.getOutgoingFriendRequestList(offset = 0)
    }

    private fun initPlaceholders() {
        binding?.placeholderOutgoingRequest?.gone()
        binding?.placeholderFoundOutgoingRequest?.gone()
    }

    private fun initRequestsList() {
        requestsAdapter = OutgoingFriendshipRequestsAdapter(itemClickListener = this)
        binding?.rvFriendsListNew?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = requestsAdapter
            addItemDecoration(
                    HorizontalLineDivider(
                            dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.shared_divider_item_shape)!!,
                            paddingLeft = 20.dp,
                            paddingRight = 20.dp
                    )
            )
        }

        binding?.srlFriendsListNew?.isRefreshing = true
        binding?.srlFriendsListNew?.setOnRefreshListener {
            requestsAdapter.clear()
            viewModel.resetParams()
            viewModel.getOutgoingFriendRequestList(offset = 0)
        }
    }

    private fun initSearchResultList() {
        searchResultsAdapter = OutgoingFriendshipRequestsAdapter(itemClickListener = this)
        binding?.filteredOutgoingRequests?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchResultsAdapter
            addItemDecoration(
                    HorizontalLineDivider(
                            dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.shared_divider_item_shape)!!,
                            paddingLeft = 20.dp,
                            paddingRight = 20.dp
                    )
            )
        }

        binding?.srlFriendsListFiltered?.isRefreshing = false
        binding?.srlFriendsListFiltered?.setOnRefreshListener {
            searchResultsAdapter.clear()
            viewModel.search(viewModel.getLastQuery(), searchResultsAdapter.itemCount)
        }
    }

    private fun subscribeOnLiveData() {
        // список исходящих заявок
        viewModel.requests.observe(viewLifecycleOwner, Observer { outRequests: MutableList<UserSimple>? ->
            outRequests?.let {
                if (it.size > 0) {
                    requestsAdapter.addElements(it)
                    binding?.srlFriendsListNew?.isRefreshing = false
                }
            }
        })

        // список найденных исходящих заявок
        viewModel.foundRequests.observe(viewLifecycleOwner, Observer { foundRequests: MutableList<UserSimple>? ->
            foundRequests?.let {
                if (it.size > 0) {
                    binding?.srlFriendsListFiltered?.isRefreshing = false
                    searchResultsAdapter.addElements(it)
                }
            }
        })

        // открыть диалог отмены заявки на дружбу на подписанного пользователя ?
        viewModel.showOutgoingFriendshipRequestSubscribedCancellationDialog.observe(viewLifecycleOwner, Observer {
            requireContext().createSubscribedFriendCancelRequest(
                            cancelRequest = { viewModel.cancelOutgoingFriendshipRequest(it) },
                            cancelRequestAndUnsubscribe = { viewModel.cancelOutgoingFriendshipRequestAndUnsubscribe(it) }
                    )
                    .show(childFragmentManager)
        })

        // открыть диалог отмены заявки на дружбу на НЕподписанного пользователя ?
        viewModel.showOutgoingFriendshipRequestUnSubscribedCancellationDialog.observe(viewLifecycleOwner, Observer {
            requireContext().createUnsubscribedFriendRemovalDialog(
                            cancelRequest = { viewModel.cancelOutgoingFriendshipRequest(it) }
                    )
                    .show(childFragmentManager)
        })

        // подписка на событие - результат отмены запроса на дружбу
        viewModel.showFriendshipCancellationResultView.observe(viewLifecycleOwner, Observer { isRequestCancelled ->
            if (isRequestCancelled.first) {
                if (binding?.rvFriendsListNew?.isVisible == true) {
                    requestsAdapter.removeItem(isRequestCancelled.second)
                } else {
                    searchResultsAdapter.removeItem(isRequestCancelled.second)
                }
            } else {
                NToast.with(act)
                        .typeError()
                        .text(getString(R.string.error_while_cancelling_request))
                        .show()
            }
        })

        // отображать список найденных заявок
        viewModel.showSearchResultView.observe(viewLifecycleOwner, Observer {
            binding?.filteredOutgoingRequests?.visibility = if (it) View.VISIBLE else View.GONE
            binding?.srlFriendsListFiltered?.visibility = if (it) View.VISIBLE else View.GONE
        })

        // отображать список заявок
        viewModel.showRequestsView.observe(viewLifecycleOwner, Observer {
            binding?.rvFriendsListNew?.visibility = if (it) View.VISIBLE else View.GONE
            binding?.srlFriendsListNew?.visibility = if (it) View.VISIBLE else View.GONE
        })

        // отображать плейсхолдер для списка заявок
        viewModel.showRequestsPlaceholderView.observe(viewLifecycleOwner, Observer {
            binding?.placeholderOutgoingRequest?.visibility = if (it) View.VISIBLE else View.GONE
        })

        // отображать плейсхолдер для списка найденных заявок
        viewModel.showSearchResultPlaceholderView.observe(viewLifecycleOwner, Observer {
            binding?.placeholderFoundOutgoingRequest?.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun openProfile(userSimple: UserSimple) {
        add(
            UserInfoFragment(),
            Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(ARG_USER_ID, userSimple.userId),
            Arg(ARG_PAGER_PROFILE, false),
            Arg(ARG_TRANSIT_FROM, AmplitudePropertyWhere.FRIEND.property)
        )
    }

    fun searchOutgoingFriendshipRequests(text: String?) {
        text?.run {
            searchResultsAdapter.clear()
            viewModel.search(this, searchResultsAdapter.itemCount)
        }
    }

    fun turnOffSearchUIMode() {
        viewModel.setSearchResultPlaceholderVisible(isVisible = false)
        viewModel.setSearchResultVisible(isVisible = false)

        viewModel.setRequestsPlaceholderVisible(isVisible = false)
        viewModel.setRequestsVisible(isVisible = true)

        searchResultsAdapter.clear()
        requestsAdapter.clear()

        viewModel.resetParams()
        viewModel.getOutgoingFriendRequestList(offset = 0)
    }

    fun turnOnSearchMode() {
        viewModel.setSearchResultPlaceholderVisible(isVisible = false)
        viewModel.setSearchResultVisible(isVisible = true)

        viewModel.setRequestsPlaceholderVisible(isVisible = false)
        viewModel.setRequestsVisible(isVisible = false)

        searchResultsAdapter.clear()
        requestsAdapter.clear()
    }

    override fun onItemClicked(userSimple: UserSimple) {
        openProfile(userSimple)
    }

    override fun onActionClicked(userSimple: UserSimple) {
        viewModel.openCancelOutGoingRequestDialog(userSimple)
    }

    fun resetSearch() {
        viewModel.resetSearch()
        searchResultsAdapter.clear()
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOutgoingFriendshipRequestListBinding
        get() = FragmentOutgoingFriendshipRequestListBinding::inflate
}


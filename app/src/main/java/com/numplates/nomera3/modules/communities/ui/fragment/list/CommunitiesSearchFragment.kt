package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.click
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.px
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentGroupsSearchBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityFollow
import com.numplates.nomera3.modules.communities.ui.adapter.CommunityListTitleAdapter
import com.numplates.nomera3.modules.communities.ui.adapter.CommunitySearchAdapterNew
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityViewEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunitySubscriptionViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunitiesSearchViewModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommunitiesSearchFragment: BaseFragmentNew<FragmentGroupsSearchBinding>() {

    private val searchViewModel by viewModels<CommunitiesSearchViewModel>()
    private val subscriptionViewModel by viewModels<CommunitySubscriptionViewModel>(
        factoryProducer = { App.component.getViewModelFactory() }
    )
    private lateinit var adapter: CommunitySearchAdapterNew
    private lateinit var searchAdapterPaginator: RecyclerViewPaginator
    private val disposables = CompositeDisposable()
    private var snackbarBottomMargin = 0
    //т.к в при инициализации RxText срабатывает с пустым значением ""
    private var isFirstInited = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBar()
        initAdapter()
        initObservers()
        initSearch()
        KeyboardHeightProvider(requireView()).observer = {
            snackbarBottomMargin = if (it > 0) it.px + 50.px else 50.px
        }
    }

    override fun onOpenTransitionFragment() {
        super.onOpenTransitionFragment()
        binding?.etSearchGroup?.requestFocus()
        binding?.etSearchGroup?.showKeyboard()
    }

    override fun onResume() {
        super.onResume()

        initSearch()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    private fun initBar() {
        //status bar
        val layoutParamsStatusBar = binding?.stausBar?.layoutParams as LinearLayout.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.stausBar?.layoutParams = layoutParamsStatusBar
        binding?.ivBackBtn?.click {
            binding?.etSearchGroup?.setText("")
            adapter.clear()
            act.onBackPressed()
        }
    }

    private fun initSearch() {
        binding?.etSearchGroup?.let { etSearchGroup ->
            disposables.add(RxTextView.textChanges(etSearchGroup)
                .map { text -> text.toString().lowercase(Locale.getDefault()).trim() }
                .debounce(350, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ text ->
                    if (isFirstInited) {
                        isFirstInited = false
                        return@subscribe
                    }
                    searchViewModel.setQuery(text)
                }, { error ->
                    Timber.e("ERROR: Observe text changes: $error")
                })
            )
            etSearchGroup.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    showClearSearchIcon(s?.isNotEmpty() == true)
                }

                override fun afterTextChanged(s: Editable?) = Unit

            })

            binding?.ivClearSearch?.click {
                etSearchGroup.setText("")
            }
        }
    }

    private fun showClearSearchIcon(show: Boolean) {
        binding?.ivClearSearch?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun initAdapter() {
        adapter = CommunitySearchAdapterNew(mutableListOf())
        adapter.setInteractor(object : CommunitySearchAdapterNew.IOnSearchGroup {
            override fun onGroupClicked(community: CommunityListItemUIModel) {
                add(
                    CommunityRoadFragment(),
                    Act.COLOR_STATUSBAR_BLACK_NAVBAR,
                    Arg(ARG_GROUP_ID, community.id)
                )
            }

            override fun onGroupJoinClicked(model: CommunityListItemUIModel, position: Int) {
                subscriptionViewModel.subscribeCommunity(
                    model,
                    AmplitudePropertyWhereCommunityFollow.ALL_COMMUNITY,
                    position
                )
            }
        })
        binding?.rvSearchResults?.let {
            searchAdapterPaginator = RecyclerViewPaginator(
                recyclerView = it,
                isLoading = { searchViewModel.isLoading() },
                loadMore = { searchViewModel.loadMoreSearch(adapter.itemCount) },
                onLast = { searchViewModel.onLast() }
            )
        }
        binding?.rvSearchResults?.layoutManager = LinearLayoutManager(context)
        binding?.rvSearchResults?.adapter = ConcatAdapter(
            CommunityListTitleAdapter(getString(R.string.general_search_results)),
            adapter
        )
    }

    private fun initObservers() {
        searchViewModel.liveSearchGroup.observe(viewLifecycleOwner, {
            it?.let {
                adapter.replace(it)

                if (it.isEmpty()) binding?.tvGroupsNotFound?.visible()
                else binding?.tvGroupsNotFound?.gone()
            }
        })

        searchViewModel.liveSearchProgress.observe(viewLifecycleOwner, ::showSearchProgress)
        searchViewModel.liveSearchGroupMore.observe(viewLifecycleOwner, {
            adapter.addItems(it)
        })
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                subscriptionViewModel.viewEvent.collect(::handleViewEvents)
            }
        }
    }

    private fun handleViewEvents(event: CommunityViewEvent) {
        when (event) {
            is CommunityViewEvent.SuccessSubscribeCommunity -> {
                onSubscribeGroup(event.position)
            }

            is CommunityViewEvent.SuccessUnsubscribeCommunity -> {
                onUnsubscribeGroup(event.position)
            }

            is CommunityViewEvent.SuccessUnsubscribePrivateCommunity -> {
                onUnSubscribePrivateGroup(event.position)
            }

            is CommunityViewEvent.SuccessSubscribePrivateCommunity -> {
                onSubscribePrivateGroup(event.position)
            }

            is CommunityViewEvent.FailureGetCommunityInfo -> {
                showErrorMessage(R.string.group_error_load_group_data)
            }

            is CommunityViewEvent.FailureSubscribeCommunity -> {
                showErrorMessage(R.string.group_error_subscribe_group)
            }

            is CommunityViewEvent.FailureUnsubscribeCommunity -> {
                showErrorMessage(R.string.group_error_unsubscribe_group)
            }
            else -> {}
        }
    }

    private fun onSubscribeGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, true)
            showSuccessMessage(R.string.group_subscription_success)
        }
    }

    private fun onSubscribePrivateGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, true)
            showSuccessMessage(R.string.group_private_subscription_success)
        }
    }

    private fun onUnsubscribeGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, false)
        }
    }

    private fun onUnSubscribePrivateGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, false)
        }
    }

    private fun showSuccessMessage(@StringRes messageRes: Int) {
        NSnackbar.with(requireView())
            .marginBottom(snackbarBottomMargin)
            .typeSuccess()
            .text(getString(messageRes))
            .show()
    }

    private fun showErrorMessage(@StringRes messageRes: Int) {
        NSnackbar.with(requireView())
            .marginBottom(snackbarBottomMargin)
            .typeError()
            .text(getString(messageRes))
            .show()
    }

    private fun showSearchProgress(inProgress: Boolean) {
        if (inProgress) binding?.progressBarSearch?.visible()
        else binding?.progressBarSearch?.gone()
    }

    private fun setSubscriptionStatus(position: Int, subscribed: Boolean) {
        adapter.setItemSubscriptionStatus(position, subscribed)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGroupsSearchBinding
        get() = FragmentGroupsSearchBinding::inflate

}

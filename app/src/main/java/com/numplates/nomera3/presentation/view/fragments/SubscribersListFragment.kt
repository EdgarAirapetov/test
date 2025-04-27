package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentSubscriptionsListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.SubscriptionAdapter
import com.numplates.nomera3.presentation.view.adapter.SubscriptionAdapter.SubscriptionAdapterModel
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.SubscribersViewModel
import com.numplates.nomera3.presentation.viewmodel.exception.NoUserIdException
import com.numplates.nomera3.presentation.viewmodel.viewevents.SubscriptionViewEvent
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val REQUEST_LIMIT = 50
private const val INPUT_DEBOUNCE_DELAY = 200L

// подписчики
class SubscribersListFragment : BaseFragmentNew<FragmentSubscriptionsListBinding>(), IOnBackPressed,
    SwipeRefreshLayout.OnRefreshListener {

    private var subscribersAdapter = SubscriptionAdapter() // list of subscribers
    private var searchAdapter = SubscriptionAdapter() //search subscribers
    private val subscribersViewModel by viewModels<SubscribersViewModel>()
    private var isSearchMode = false

    private val disposables = CompositeDisposable()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSubscriptionsListBinding
        get() = FragmentSubscriptionsListBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        configurePlaceHolder()
        initRecycler()
        initObservers()
    }

    override fun onStart() {
        super.onStart()
        initRx()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    private fun initViews() {
        binding?.ivSubscribersOpenReferralScreen?.gone()
        binding?.tvToolbarTitleSubscr?.text = getString(R.string.subscribers_list_screen_title)
        binding?.etSearchSubscription?.hint =
            getString(R.string.subscribers_list_screen_search_title)
    }

    private fun initListeners() {
        binding?.srLayoutSubscriptions?.setOnRefreshListener(this)

        //back btn
        binding?.ivBackArrow?.setOnClickListener {
            if (isSearchMode) {
                initCommonMode()
            } else exitScreen()
        }

        //search btn
        binding?.ivSearch?.setOnClickListener {
            if (!isSearchMode) initSearchMode()
            else binding?.etSearchSubscription?.setText("")
        }
    }


    /**
     * Configure empty subscription list placeholder
     * */
    private fun configurePlaceHolder() {
        binding?.placeholderEmptyList?.ivEmptyList?.setImageResource(R.drawable.ic_unsubscribe_gray)
        binding?.placeholderEmptyList?.tvEmptyList?.text =
            getString(R.string.subscribers_list_empty)
        binding?.placeholderEmptyList?.tvButtonEmptyList?.gone()
    }

    private fun initObservers() {
        subscribersViewModel.liveSubscribers.observe(viewLifecycleOwner, Observer {
            binding?.srLayoutSubscriptions?.isRefreshing = false
            handleSubscriptions(it)
        })

        subscribersViewModel.liveViewEvent.observe(viewLifecycleOwner, Observer {
            binding?.srLayoutSubscriptions?.isRefreshing = false
            when (it) {
                is SubscriptionViewEvent.ErrorWhileRequestingSubscriptions -> {
                    showListError()
                }

                is SubscriptionViewEvent.SuccessDeleteFromSubscription -> {
                    if (!isSearchMode)
                        subscribersAdapter.removeItem(it.deletedUser)
                    else searchAdapter.removeItem(it.deletedUser)
                }

                is SubscriptionViewEvent.ErrorWhileSearchSubscriptions -> {
                    showListError()
                }
                else -> {}
            }
        })
    }

    private fun showListError() {
        NToast.with(view)
            .typeError()
            .text(getString(R.string.error_while_getting_subscribers_list))
            .show()
    }


    /**
     * handle result from server. Add data to adapter
     * */
    private fun handleSubscriptions(subscriptions: List<UserSimple?>?) {
        //accumulate data to res list
        val res = mutableListOf<SubscriptionAdapterModel>()
        subscriptions?.forEach { userSimple ->
            userSimple?.let {
                res.add(
                    SubscriptionAdapterModel(
                        userSimple, false
                    )
                )
            }
        }

        //if not in search mode add data to subscriptionsAdapter else to searchAdapter
        if (!isSearchMode) {
            if (subscribersAdapter.itemCount == 0 && res.size == 0)
                showEmptySubscriptions()
            else {
                hideEmptySubscriptions()
                subscribersAdapter.addData(res)
            }
        } else {
            searchAdapter.addDataSearch(res)
        }
    }

    private fun initRecycler() {
        binding?.srLayoutSubscriptions?.isRefreshing = true
        val linearLayoutManager = LinearLayoutManager(context)
        binding?.rvSubscription?.layoutManager = linearLayoutManager
        binding?.rvSubscription?.adapter = subscribersAdapter
        /*
        * https://nomera.atlassian.net/browse/BR-3306 фикс по задаче, где
        * экран подписок открывается на той же позиции, которая сохранилась
        * при закрытии экрана подписчиков и наоборот. По возможности нужно
        * решить проблему по-другому.
        * */
        subscribersAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    linearLayoutManager.scrollToPosition(0)
                }
            }
        })

        //delete from subscriptions callback
        subscribersAdapter.onActionBtnClicked = { subscription ->
            showConfirmDialogUnsubscribe(subscription)
        }

        subscribersAdapter.onProfileAreaClickCallback = { selectedUser: SubscriptionAdapterModel ->
            try {
                openProfile(selectedUser)
            } catch (e: NoUserIdException) {
                Timber.e(e)
                // not sure that user id always != null
            }
        }

        searchAdapter.onActionBtnClicked = { subscription ->
            showConfirmDialogUnsubscribe(subscription)
        }

        searchAdapter.onProfileAreaClickCallback = { selectedUser: SubscriptionAdapterModel ->
            try {
                openProfile(selectedUser)
            } catch (e: NoUserIdException) {
                Timber.e(e)
                // not sure that user id always != null
            }
        }

        //init pagination
        RecyclerViewPaginator(
            recyclerView = binding?.rvSubscription!!,
            onLast = {
                if (!isSearchMode)
                    subscribersViewModel.onLastSubscriber()
                else subscribersViewModel.onLastSubscriberSearch()
            },
            isLoading = {
                if (!isSearchMode)
                    subscribersViewModel.onLoadingSubscriber()
                else subscribersViewModel.onLoadingSubscriberSearch()
            },
            loadMore = {
                if (!isSearchMode)
                    requestSubscribers()
                else requestSearch(binding?.etSearchSubscription?.text.toString())
            }
        )
    }

    private fun openProfile(selectedUser: SubscriptionAdapterModel?) {
        context?.hideKeyboard(requireView())
        val userId: Long? = selectedUser?.user?.userId
        if (userId == null) {
            throw NoUserIdException()
        } else {
            act.addFragment(
                UserInfoFragment(),
                Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.FOLLOWERS.property)
            )
        }
    }

    private fun requestSubscribers() {
        subscribersViewModel.requestSubscribers(
            userId = subscribersViewModel.getUserUid(),
            limit = REQUEST_LIMIT,
            offset = subscribersAdapter.itemCount,
        )
    }


    /**
     * Show confirm dialog and delete user from subscribers list
     * */
    private fun showConfirmDialogUnsubscribe(subscription: SubscriptionAdapterModel) {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.remove_subscriber_dialog_header))
            .setDescription(getString(R.string.remove_subscriber_dialog_description))
            .setHorizontal(true)
            .setTopBtnText(getString(R.string.remove_subscriber_dialog_delete))
            .setMiddleBtnText(getString(R.string.remove_subscriber_dialog_delete_remove))
            .setBottomBtnText(getString(R.string.remove_subscriber_dialog_cancel))
            .setTopClickListener {
                subscribersViewModel.deleteFromSubscribers(subscription.user.userId)
            }
            .setMiddleClickListener {
                subscribersViewModel.deleteAndBlock(subscribersViewModel.getUserUid(), subscription.user.userId)
            }
            .show(childFragmentManager)
    }

    //show placeholder
    private fun showEmptySubscriptions() {
        binding?.placeholderEmptyList?.llEmptyListContainer?.visible()
        binding?.rvSubscription?.gone()
    }

    //hide placeholder
    private fun hideEmptySubscriptions() {
        binding?.placeholderEmptyList?.llEmptyListContainer?.gone()
        binding?.rvSubscription?.visible()
    }


    /**
     * This method will be called when user pushed back btn
     * */
    private fun initCommonMode() {
        isSearchMode = false
        binding?.etSearchSubscription?.gone()
        binding?.ivBackArrow?.setImageResource(R.drawable.arrowback)
        binding?.ivBackArrow?.setPadding(8.dp)
        binding?.ivSearch?.setImageResource(R.drawable.ic_search_black_nomera)
        binding?.ivSearch?.visible()
        binding?.tvToolbarTitleSubscr?.visible()
        context?.hideKeyboard(requireView())
        attachSubscribersAdapter()
    }


    /**
     * This method will be called when user pushed search btn
     * */
    private fun initSearchMode() {
        isSearchMode = true
        binding?.etSearchSubscription?.visible()
        binding?.etSearchSubscription?.setText("")
        binding?.ivBackArrow?.setImageResource(R.drawable.ic_arrow_back_noomeera)
        binding?.ivBackArrow?.setPadding(2.dp)
        binding?.ivSearch?.setImageResource(R.drawable.ic_close_noomeera)
        binding?.ivSearch?.gone()
        binding?.tvToolbarTitleSubscr?.gone()
        attachSearchAdapter()
        binding?.etSearchSubscription?.requestFocus()
        binding?.etSearchSubscription?.showKeyboard()
    }


    private fun attachSearchAdapter() {
        searchAdapter.clearData()
        binding?.rvSubscription?.adapter = searchAdapter
    }


    private fun attachSubscribersAdapter() {
        subscribersAdapter.clearData()
        binding?.rvSubscription?.adapter = subscribersAdapter
        requestSubscribers()
    }

    /**
     * Swipe to refresh listener
     * */
    override fun onRefresh() {
        if (!isSearchMode) {
            subscribersAdapter.clearData()
            requestSubscribers()
        } else {
            searchAdapter.clearData()
            requestSearch(binding?.etSearchSubscription?.text.toString())
        }
    }

    override fun onBackPressed(): Boolean {
        if (isSearchMode) initCommonMode()
        else exitScreen()
        return true
    }


    private fun exitScreen() {
        act.isSubscribeFloorFragment = true
        act.navigatorViewPager.setCurrentItem(act.navigatorViewPager.currentItem - 1, true)
    }

    /**
     * init rx textChanged listener
     * */
    private fun initRx() {
        binding?.etSearchSubscription?.let { et ->
            val d = RxTextView.textChanges(et)
                .map { text -> text.toString().lowercase(Locale.getDefault()).trim() }
                .debounce(INPUT_DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ text ->
                    binding?.ivSearch?.isGone =
                        text.isNullOrEmpty() && binding?.etSearchSubscription?.visibility == View.VISIBLE
                    searchAdapter.clearData()
                    if (text.isEmpty()) {
                        requestFreshSubscriptions()
                    } else {
                        requestSearch(text)
                    }
                }, { error ->
                    Timber.e("ERROR: Observe text changes: $error")
                })
            disposables.add(d)
        }
    }

    private fun requestFreshSubscriptions() {
        subscribersViewModel.requestSubscribers(
            userId = subscribersViewModel.getUserUid(),
            limit = REQUEST_LIMIT,
            offset = 0,
        )
    }

    /**
     * Search subscriptions
     * */
    private fun requestSearch(text: String) {
        subscribersViewModel.subscribersSearch(
            userId =  subscribersViewModel.getUserUid(),
            limit = REQUEST_LIMIT,
            offset = searchAdapter.itemCount,
            text = text,
        )
    }
}

package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.isGone
import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
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
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentSubscriptionsListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesCommunitiesContainerFragment
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.SubscriptionAdapter
import com.numplates.nomera3.presentation.view.adapter.SubscriptionAdapter.SubscriptionAdapterModel
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showGroupChatTooltip
import com.numplates.nomera3.presentation.viewmodel.SubscriptionViewModel
import com.numplates.nomera3.presentation.viewmodel.exception.NoUserIdException
import com.numplates.nomera3.presentation.viewmodel.viewevents.SubscriptionViewEvent
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val REFERRAL_TOOLTIP_OFFSET_Y = 18
private const val REFERRAL_TOOLTIP_OFFSET_X = 2
private const val REQUEST_LIMIT = 50
private const val INPUT_DEBOUNCE_DELAY = 200L

// подписки
class SubscriptionsListFragment : BaseFragmentNew<FragmentSubscriptionsListBinding>(),
    SwipeRefreshLayout.OnRefreshListener,
    IOnBackPressed {

    private var subscriptionsAdapter = SubscriptionAdapter() // list of subscription
    private var searchAdapter = SubscriptionAdapter() //search subscription
    private var isSearchMode = false
    private var referralToolTipJob: Job? = null

    private val subscriptionViewModel by viewModels<SubscriptionViewModel> {
        App.component.getViewModelFactory()
    }
    private val disposables = CompositeDisposable()
    private val createReferralToolTip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_referral_friends)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSubscriptionsListBinding
        get() = FragmentSubscriptionsListBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.srLayoutSubscriptions?.setOnRefreshListener(this)

        //back btn
        binding?.ivBackArrow?.setOnClickListener {
            if (isSearchMode) initCommonMode()
            else exitScreen()
        }

        //search btn
        binding?.ivSearch?.setOnClickListener {
            if (!isSearchMode) initSearchMode()
            else binding?.etSearchSubscription?.setText("")
        }

        binding?.ivSubscribersOpenReferralScreen?.setOnClickListener {
            subscriptionViewModel.logSubscriptionsPeopleSelected()
            add(
                PeoplesCommunitiesContainerFragment(),
                Act.LIGHT_STATUSBAR
            )
        }

        configurePlaceHolder()
        initRecycler()
        initObservers()
    }

    private fun initObservers() {
        subscriptionViewModel.liveSubscriptions.observe(viewLifecycleOwner, Observer {
            binding?.srLayoutSubscriptions?.isRefreshing = false
            handleSubscriptions(it)
        })

        subscriptionViewModel.liveViewEvent.observe(viewLifecycleOwner, Observer {
            when (it) {
                is SubscriptionViewEvent.ErrorWhileRequestingSubscriptions -> {
                    showListError()
                }

                is SubscriptionViewEvent.SuccessDeleteFromSubscription -> {
                    if (!isSearchMode)
                        subscriptionsAdapter.removeItem(it.deletedUser)
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
            .text(getString(R.string.error_while_getting_subscriptions_list))
            .show()
    }

    override fun onPause() {
        super.onPause()
        createReferralToolTip?.dismiss()
    }

    /**
     * handle result from server. Add data to adapter
     * */
    private fun handleSubscriptions(subscriptions: List<UserSimple?>?) {
        //accumulate data to res list
        val res = mutableListOf<SubscriptionAdapterModel>()
        subscriptions?.forEach { userSimple ->
            userSimple?.let {
                res.add(SubscriptionAdapterModel(userSimple, true))
            }
        }

        //if not in search mode add data to subscriptionsAdapter else to searchAdapter
        if (!isSearchMode) {
            if (subscriptionsAdapter.itemCount == 0 && res.size == 0)
                showEmptySubscriptions()
            else {
                hideEmptySubscriptions()
                subscriptionsAdapter.addData(res)
            }
        } else {
            if (subscriptionsAdapter.itemCount == 0 && res.size == 0)
                showEmptySubscriptions() //todo emptySearchPlaceholder
            else {
                hideEmptySubscriptions()
                searchAdapter.addDataSearch(res)
            }
        }
    }

    private fun showEmptySubscriptions() {
        binding?.placeholderEmptyList?.llEmptyListContainer?.visible()
        binding?.rvSubscription?.gone()
    }

    private fun hideEmptySubscriptions() {
        binding?.placeholderEmptyList?.llEmptyListContainer?.gone()
        binding?.rvSubscription?.visible()
    }

    private fun initRecycler() {
        binding?.srLayoutSubscriptions?.isRefreshing = true
        val linearLayoutManager = LinearLayoutManager(context)
        binding?.rvSubscription?.layoutManager = linearLayoutManager
        binding?.rvSubscription?.adapter = subscriptionsAdapter
        /*
        * https://nomera.atlassian.net/browse/BR-3306 фикс по задаче, где
        * экран подписок открывается на той же позиции, которая сохранилась
        * при закрытии экрана подписчиков и наоборот. По возможности нужно
        * решить проблему по-другому.
        * */
        subscriptionsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    linearLayoutManager.scrollToPosition(0)
                }
            }
        })

        //delete from subscriptions callback
        subscriptionsAdapter.onActionBtnClicked = { subscription ->
            showConfirmDialogUnsubscribe(subscription)
        }

        searchAdapter.onActionBtnClicked = { subscription ->
            showConfirmDialogUnsubscribe(subscription)
        }

        subscriptionsAdapter.onProfileAreaClickCallback = { selectedUser: SubscriptionAdapterModel ->
            try {
                openProfile(selectedUser)
            } catch (e: NoUserIdException) {
                Timber.e(e)
                // not sure that user id always != null
            }
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
                    subscriptionViewModel.onLastSubscription()
                else subscriptionViewModel.onLastSubscriptionSearch()
            },
            isLoading = {
                if (!isSearchMode)
                    subscriptionViewModel.onLoadingSubscription()
                else subscriptionViewModel.onLoadingSubscriptionSearch()
            },
            loadMore = {
                if (!isSearchMode)
                    requestSubscriptions()
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
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.FOLLOWS.property)
            )
        }
    }

    private fun requestFreshSubscriptions() {
        subscriptionViewModel.requestSubscriptions(
            userId = subscriptionViewModel.getUserUid(),
            limit = REQUEST_LIMIT,
        )
    }

    /**
     * Request subscriptions list from backend
     * */
    private fun requestSubscriptions() {
        subscriptionViewModel.requestSubscriptions(
            userId = subscriptionViewModel.getUserUid(),
            limit = REQUEST_LIMIT,
            offset = subscriptionsAdapter.itemCount,
        )
    }

    /**
     * Search subscriptions
     * */
    private fun requestSearch(text: String) {
        subscriptionViewModel.subscriptionsSearch(
            userId = subscriptionViewModel.getUserUid(),
            limit = REQUEST_LIMIT,
            offset = searchAdapter.itemCount,
            text = text,
        )
    }

    /**
     * Init rx TextChanged listener
     */
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


    /**
     * Configure empty subscription list placeholder
     * */
    private fun configurePlaceHolder() {
        binding?.placeholderEmptyList?.ivEmptyList?.setImageResource(R.drawable.ic_unsubscribe_gray)
        binding?.placeholderEmptyList?.tvEmptyList?.text = getString(R.string.no_subscription_header)
        binding?.placeholderEmptyList?.tvButtonEmptyList?.text = getString(R.string.find_new_subscriptions)
        binding?.placeholderEmptyList?.tvButtonEmptyList?.visible()
        binding?.placeholderEmptyList?.tvButtonEmptyList?.setOnClickListener {
            add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
        }
    }

    /**
     * Show confirm dialog and delete user from subscription
     * */
    private fun showConfirmDialogUnsubscribe(subscription: SubscriptionAdapterModel) {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_unsub_dialog_header))
            .setDescription(getString(R.string.unsubscribe_dialog_description))
            .setLeftBtnText(getString(R.string.unsubscribe_dialog_close))
            .setRightBtnText(getString(R.string.user_info_unsub_dialog_action))
            .setLeftClickListener {

            }
            .setRightClickListener {
                subscriptionViewModel.deleteFromSubscription(subscription.user.userId)
            }
            .show(childFragmentManager)
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

        attachSearchAddapter()
        binding?.etSearchSubscription?.requestFocus()
        binding?.etSearchSubscription?.showKeyboard()
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

        attachSubscriptionAdapter()
    }

    private fun attachSubscriptionAdapter() {
        subscriptionsAdapter.clearData()
        binding?.rvSubscription?.adapter = subscriptionsAdapter
        requestSubscriptions()
    }

    private fun attachSearchAddapter() {
        searchAdapter.clearData()
        binding?.rvSubscription?.adapter = searchAdapter
    }


    override fun onStart() {
        super.onStart()
        initRx()
        showToolTip()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
        referralToolTipJob?.cancel()
        createReferralToolTip?.dismiss()
    }


    /**
     * Swipe to refresh listener
     * */
    override fun onRefresh() {
        if (!isSearchMode) {
            subscriptionsAdapter.clearData()
            requestSubscriptions()
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


    private fun showToolTip() {
        if (subscriptionViewModel.isNeedToShowReferralTooltip()) {
            referralToolTipJob = lifecycleScope.launch {
                delay(TooltipDuration.COMMON_START_DELAY)
                binding?.ivSubscribersOpenReferralScreen?.let { image ->
                    createReferralToolTip?.showGroupChatTooltip(
                        fragment = this@SubscriptionsListFragment,
                        view = image,
                        offsetY = -(REFERRAL_TOOLTIP_OFFSET_Y.dp),
                        offsetX = REFERRAL_TOOLTIP_OFFSET_X.dp
                    )
                    subscriptionViewModel.referralToolTipShowed()
                    delay(TooltipDuration.CREATE_GROUP_CHAT)
                    createReferralToolTip?.dismiss()
                }
            }
        }
    }

    private fun exitScreen() {
        act.isSubscribeFloorFragment = true
        act.navigatorViewPager.setCurrentItem(act.navigatorViewPager.currentItem - 1, true)
    }
}

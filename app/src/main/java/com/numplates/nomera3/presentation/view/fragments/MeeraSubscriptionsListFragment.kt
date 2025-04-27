package com.numplates.nomera3.presentation.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.PopupWindow
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.common.DEFAULT_KEYBOARD_HEIGHT_PX
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraSubscriptionsListFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.MeeraSubscriptionAction
import com.numplates.nomera3.presentation.view.adapter.MeeraSubscriptionsAdapter
import com.numplates.nomera3.presentation.view.adapter.SubscriptionAdapterModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.INPUT_TIMEOUT
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraCellShimmerAdapter
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.viewmodel.SubscriptionViewModel
import com.numplates.nomera3.presentation.viewmodel.exception.NoUserIdException
import com.numplates.nomera3.presentation.viewmodel.viewevents.SubscriptionViewEvent
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val COUNT_SHIMMER_ITEM = 8

class MeeraSubscriptionsListFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_subscriptions_list_fragment, behaviourConfigState = ScreenBehaviourState.Full
), MeeraPullToRefreshLayout.OnRefreshListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraSubscriptionsListFragmentBinding::bind)

    private var subscriptionsAdapter: MeeraSubscriptionsAdapter? = null

    private val shimmerAdapter = MeeraCellShimmerAdapter()
    private var isSearchMode = false
    private var referralToolTipJob: Job? = null
    private var isNeedUpdateUserList = true

    private val subscriptionViewModel by viewModels<SubscriptionViewModel> {
        App.component.getViewModelFactory()
    }
    private val createReferralToolTip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_referral_friends)
    }
    private val listShimmer = List(COUNT_SHIMMER_ITEM) { "" }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscriptionsAdapter = MeeraSubscriptionsAdapter(::initMeeraSubscriptionAction)

        initView()
        configurePlaceHolder()
        initRecycler()
        initObservers()
    }

    private fun initView() {
        binding?.srLayoutSubscriptions?.setOnRefreshListener(this)
        binding?.srLayoutSubscriptions?.setRefreshEnable(true)
        binding?.vNavView?.backButtonClickListener = {
            findNavController().popBackStack()
        }
        binding?.vNavView?.title = getString(R.string.my_subscriptions)
        binding?.ivUserAdd?.setThrottledClickListener {
            subscriptionViewModel.logSubscriptionsPeopleSelected()
            findNavController().safeNavigate(
                R.id.action_meeraSubscriptionsListFragment_to_peoplesFragment
            )
        }
    }

    private fun initObservers() {
        subscriptionViewModel.liveSubscriptions.observe(viewLifecycleOwner) {
            handleSubscriptions(it)
        }

        subscriptionViewModel.liveViewEvent.observe(viewLifecycleOwner) { action ->
            when (action) {
                is SubscriptionViewEvent.ErrorWhileRequestingSubscriptions -> {
                    showListError()
                }

                is SubscriptionViewEvent.SuccessDeleteFromSubscription -> {
                    val list = subscriptionsAdapter?.currentList?.toMutableList()
                    list?.removeIf { user ->
                        user.user.userId == action.deletedUser
                    }
                    subscriptionsAdapter?.submitList(list)
                    binding.srLayoutSubscriptions.setRefreshing(false)
                }

                is SubscriptionViewEvent.ErrorWhileSearchSubscriptions -> {
                    showListError()
                }

                is SubscriptionViewEvent.LoadFromSubscription -> {
                    if (isNeedUpdateUserList) {
                        hideEmptySubscriptions()
                        binding?.rvSubscription?.gone()
                        binding?.rvShimmerSubscription?.visible()
                    }
                }

                is SubscriptionViewEvent.SuccessLoadFromSubscription -> {
                    binding.srLayoutSubscriptions.setRefreshing(false)
                    if (isNeedUpdateUserList) {
                        binding?.rvSubscription?.visible()
                        binding?.rvShimmerSubscription?.gone()
                    }
                }

                else -> Unit
            }
        }
    }

    private fun showListError() {
        binding.srLayoutSubscriptions.setRefreshing(false)
        com.meera.core.utils.showCommonError(getText(R.string.error_while_getting_subscriptions_list), requireView())
    }

    override fun onPause() {
        super.onPause()
        createReferralToolTip?.dismiss()
    }

    /**
     * handle result from server. Add data to adapter
     * */
    private fun handleSubscriptions(subscriptions: List<UserSimple?>?) {
        val res = mutableListOf<SubscriptionAdapterModel>()
        subscriptions?.forEach { userSimple ->
            userSimple?.let {
                res.add(SubscriptionAdapterModel(userSimple, userSimple.settingsFlags?.subscription_on.toBoolean()))
            }
        }
        if (subscriptionsAdapter?.itemCount == 0 && res.size == 0) {
            showEmptySubscriptions()
        } else {
            hideEmptySubscriptions()
            subscriptionsAdapter?.submitList(res)
        }
    }

    private fun showEmptySubscriptions() {
        binding.apply {
            val imgResId = if (isSearchMode) R.drawable.ic_search_people_empty else R.drawable.ic_i_dont_know
            ivEmptyList.setImageResource(imgResId)
            val hintId = if (isSearchMode) R.string.meera_settings_empty_state else R.string.no_subscription_header
            tvButtonEmptyList.setText(hintId)

            vSearchGroupBtn.isGone = isSearchMode
            vSearchGroupBtn.buttonType = ButtonType.FILLED
            meeraPlaceholderEmptyList.setMargins(bottom = if (isSearchMode) DEFAULT_KEYBOARD_HEIGHT_PX else 0)
        }

        binding?.meeraPlaceholderEmptyList?.visible()
        binding?.rvSubscription?.gone()
    }

    private fun hideEmptySubscriptions() {
        binding?.meeraPlaceholderEmptyList?.gone()
        binding?.rvSubscription?.visible()
    }

    private fun initRecycler() {
        val linearLayoutManager = LinearLayoutManager(context)
        binding?.rvSubscription?.apply {
            layoutManager = linearLayoutManager
            adapter = subscriptionsAdapter
        }
        requestSubscriptions()

        binding?.rvShimmerSubscription?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = shimmerAdapter
            shimmerAdapter.submitList(listShimmer)
        }/*
        * https://nomera.atlassian.net/browse/BR-3306 фикс по задаче, где
        * экран подписок открывается на той же позиции, которая сохранилась
        * при закрытии экрана подписчиков и наоборот. По возможности нужно
        * решить проблему по-другому.
        * */
        subscriptionsAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    linearLayoutManager.scrollToPosition(0)
                }
            }
        })

        binding?.rvSubscription?.let { recycler ->
            RecyclerViewPaginator(
                recyclerView = recycler,
                onLast = {
                    if (!isSearchMode) subscriptionViewModel.onLastSubscription()
                    else subscriptionViewModel.onLastSubscriptionSearch()
                },
                isLoading = {
                    if (!isSearchMode) subscriptionViewModel.onLoadingSubscription()
                    else subscriptionViewModel.onLoadingSubscriptionSearch()
                },
                loadMore = { page ->
                    if (!isSearchMode) requestSubscriptions()
                    else requestSearch(
                        binding?.vSearch?.searchInputText ?: "",
                        offset = subscriptionsAdapter?.itemCount ?: 0
                    )
                })
        }
    }

    private fun initMeeraSubscriptionAction(action: MeeraSubscriptionAction) {
        when (action) {
            is MeeraSubscriptionAction.ProfileAreaClick -> {
                try {
                    openProfile(action.model)
                } catch (e: NoUserIdException) {
                    Timber.d(e)
                }
            }

            is MeeraSubscriptionAction.UnsubscribeBtnClick -> {
                showConfirmDialogUnsubscribe(action.model)
            }

            is MeeraSubscriptionAction.SubscribeBtnClick -> {
                subscribeToUser(action.model)
            }

            else -> Unit
        }
    }

    private fun openProfile(selectedUser: SubscriptionAdapterModel?) {
        context?.hideKeyboard(requireView())
        val userId: Long? = selectedUser?.user?.userId
        if (userId == null) {
            throw NoUserIdException()
        } else {
            findNavController().safeNavigate(
                resId = R.id.action_meeraSubscriptionsListFragment_to_userInfoFragment, bundle = bundleOf(
                    IArgContainer.ARG_USER_ID to userId,
                    IArgContainer.ARG_TRANSIT_FROM to AmplitudePropertyWhere.FOLLOWS.property
                )
            )
        }
    }

    private fun requestFreshSubscriptions() {
        isSearchMode = false
        hideEmptySubscriptions()
        subscriptionViewModel.requestSubscriptions(
            userId = subscriptionViewModel.getUserUid(),
            limit = SubscriptionViewModel.REQUEST_LIMIT,
        )
    }

    /**
     * Request subscriptions list from backend
     * */
    private fun requestSubscriptions() {
        isSearchMode = false
        hideEmptySubscriptions()
        subscriptionViewModel.requestSubscriptions(
            offset = subscriptionsAdapter?.itemCount ?: 0,
            userId = subscriptionViewModel.getUserUid(),
            limit = SubscriptionViewModel.REQUEST_LIMIT,
        )
    }

    /**
     * Search subscriptions
     * */
    private fun requestSearch(text: String, offset: Int = 0) {
        isSearchMode = true
        hideEmptySubscriptions()
        subscriptionViewModel.subscriptionsSearch(
            offset = offset,
            userId = subscriptionViewModel.getUserUid(),
            limit = SubscriptionViewModel.REQUEST_LIMIT,
            text = text,
        )
    }

    /**
     * Init rx TextChanged listener
     */
    @SuppressLint("CheckResult")
    private fun initRx() {
        binding?.vSearch?.doAfterSearchTextChanged { userName ->
            Observable.just(userName).debounce(INPUT_TIMEOUT, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { name ->
                    if (name.isEmpty()) {
                        requestFreshSubscriptions()
                    } else {
                        requestSearch(name)
                    }
                }
        }
    }


    /**
     * Configure empty subscription list placeholder
     * */
    private fun configurePlaceHolder() {
        binding?.tvButtonEmptyList?.text = getString(R.string.no_subscription_header)
        binding?.vSearchGroupBtn?.text = getString(R.string.subscription_list_find_new)
        binding?.vSearchGroupBtn?.setThrottledClickListener {
//            add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
        }
    }

    /**
     * Show confirm dialog and delete user from subscription
     * */
    private fun showConfirmDialogUnsubscribe(subscription: SubscriptionAdapterModel) {
        MeeraConfirmDialogBuilder().setHeader(R.string.user_info_unsub_dialog_header)
            .setDescription(R.string.meera_unsubscribe_dialog_description).setTopBtnText(R.string.unsubscribe)
            .setTopBtnType(ButtonType.FILLED).setTopClickListener {
                isNeedUpdateUserList = false
                subscriptionViewModel.deleteFromSubscription(subscription.user.userId)
            }.setBottomBtnText(R.string.cancel).show(childFragmentManager)
    }

    private fun subscribeToUser(subscription: SubscriptionAdapterModel) {
        lifecycleScope.launch {
            isNeedUpdateUserList = false
            subscriptionViewModel.addSubscription(subscription.user.userId)
        }
    }

    override fun onStart() {
        super.onStart()
        initRx()
    }

    override fun onStop() {
        super.onStop()
        referralToolTipJob?.cancel()
        createReferralToolTip?.dismiss()
    }


    /**
     * Swipe to refresh listener
     * */
    override fun onRefresh() {
        isNeedUpdateUserList = false
        if (!isSearchMode) {
            subscriptionsAdapter?.submitList(emptyList())
            requestSubscriptions()
        }
    }
}

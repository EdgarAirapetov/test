package com.numplates.nomera3.presentation.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
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
import com.numplates.nomera3.presentation.view.adapter.MeeraSubscriberAdapter
import com.numplates.nomera3.presentation.view.adapter.MeeraSubscriptionAction
import com.numplates.nomera3.presentation.view.adapter.SubscriptionAdapterModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.INPUT_TIMEOUT
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraCellShimmerAdapter
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.viewmodel.SubscribersViewModel
import com.numplates.nomera3.presentation.viewmodel.exception.NoUserIdException
import com.numplates.nomera3.presentation.viewmodel.viewevents.SubscriptionViewEvent
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val REQUEST_LIMIT = 50
private const val COUNT_SHIMMER_ITEM = 8

class MeeraSubscribersListFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_subscriptions_list_fragment,
    behaviourConfigState = ScreenBehaviourState.Full
), MeeraPullToRefreshLayout.OnRefreshListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraSubscriptionsListFragmentBinding::bind)
    private var subscribersAdapter: MeeraSubscriberAdapter? = null
    private val shimmerAdapter = MeeraCellShimmerAdapter()
    private val subscribersViewModel by viewModels<SubscribersViewModel> {
        App.component.getViewModelFactory()
    }
    private var isSearchMode = false
    private val listShimmer = List(COUNT_SHIMMER_ITEM) { "" }
    private var isNeedUpdateUserList = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribersAdapter = MeeraSubscriberAdapter(::initMeeraSubscriptionAction)
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

    private fun initViews() {
        binding?.vSearch?.forceShowBtnClose = true
    }

    private fun initListeners() {
        binding?.srLayoutSubscriptions?.setOnRefreshListener(this)
        binding?.srLayoutSubscriptions?.setRefreshEnable(true)
        binding?.vNavView?.backButtonClickListener = {
            findNavController().popBackStack()
        }
        binding.ivUserAdd.setThrottledClickListener {
            findNavController().safeNavigate(
                R.id.action_meeraSubscribersListFragment_to_peoplesFragment
            )
        }
    }

    /**
     * Configure empty subscription list placeholder
     * */
    private fun configurePlaceHolder() {
        binding.tvButtonEmptyList.text =
            getString(R.string.subscribers_list_empty)
        binding.vSearchGroupBtn.gone()
    }

    private fun initObservers() {
        subscribersViewModel.liveSubscribers.observe(viewLifecycleOwner) {
            handleSubscriptions(it)
        }

        subscribersViewModel.liveViewEvent.observe(viewLifecycleOwner) { action ->
            when (action) {
                is SubscriptionViewEvent.ErrorWhileRequestingSubscriptions -> {
                    showListError()
                }

                is SubscriptionViewEvent.SuccessDeleteFromSubscription -> {
                    isSearchMode = true
                    val list = subscribersAdapter?.currentList?.toMutableList()
                    list?.removeIf { user ->
                        user.user.userId == action.deletedUser
                    }
                    subscribersAdapter?.submitList(list)
                    binding.srLayoutSubscriptions.setRefreshing(false)
                }

                is SubscriptionViewEvent.ErrorWhileSearchSubscriptions -> {
                    showListError()
                }

                is SubscriptionViewEvent.LoadFromSubscription -> {
                    if (isNeedUpdateUserList) {
                        binding.rvSubscription.gone()
                        binding.rvShimmerSubscription.visible()
                    }
                }

                is SubscriptionViewEvent.SuccessLoadFromSubscription -> {
                    if (isNeedUpdateUserList) {
                        binding.rvSubscription.visible()
                        binding.rvShimmerSubscription.gone()
                        binding.srLayoutSubscriptions.setRefreshing(false)
                    }
                }

                else -> Unit
            }
        }
    }

    private fun showListError() {
        binding.srLayoutSubscriptions.setRefreshing(false)
        com.meera.core.utils.showCommonError(getText(R.string.error_while_getting_subscribers_list), requireView())
    }


    /**
     * handle result from server. Add data to adapter
     * */
    private fun handleSubscriptions(subscriptions: List<UserSimple?>?) {
        val res = mutableListOf<SubscriptionAdapterModel>()
        subscriptions?.forEach { userSimple ->
            userSimple?.let {
                res.add(
                    SubscriptionAdapterModel(
                        userSimple, userSimple.settingsFlags?.subscription_on.toBoolean()
                    )
                )
            }
        }

        if (subscribersAdapter?.itemCount == 0 && res.size == 0)
            showEmptySubscriptions()
        else {
            hideEmptySubscriptions()
            subscribersAdapter?.submitList(res)
        }
    }

    private fun initRecycler() {
        val linearLayoutManager = LinearLayoutManager(context)
        binding?.rvSubscription?.apply {
            layoutManager = linearLayoutManager
            adapter = subscribersAdapter
        }
        requestSubscribers()

        binding?.rvShimmerSubscription?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = shimmerAdapter
            shimmerAdapter.submitList(listShimmer)
        }

        /*
        * https://nomera.atlassian.net/browse/BR-3306 фикс по задаче, где
        * экран подписок открывается на той же позиции, которая сохранилась
        * при закрытии экрана подписчиков и наоборот. По возможности нужно
        * решить проблему по-другому.
        * */
        subscribersAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
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
                    isNeedUpdateUserList = false
                    if (!isSearchMode)
                        requestSubscribers()
                    else requestSearch(
                        binding?.vSearch?.searchInputText ?: "",
                        offset = subscribersAdapter?.itemCount ?: 0
                    )
                }
            )
        }
    }

    private fun initMeeraSubscriptionAction(action: MeeraSubscriptionAction) {
        when (action) {
            is MeeraSubscriptionAction.DeleteBtnClick -> {
                showConfirmDialogDeleteSubscriber(action.model)
            }

            is MeeraSubscriptionAction.ProfileAreaClick -> {
                try {
                    openProfile(action.model)
                } catch (e: NoUserIdException) {
                    Timber.d(e)
                }
            }

            is MeeraSubscriptionAction.UnsubscribeBtnClick -> {
                showConfirmDialogDeleteUnsubscribe(action.model)
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
                resId = R.id.action_meeraSubscribersListFragment_to_userInfoFragment,
                bundle = bundleOf(
                    IArgContainer.ARG_USER_ID to userId,
                    IArgContainer.ARG_TRANSIT_FROM to AmplitudePropertyWhere.FOLLOWERS.property
                )
            )
        }
    }

    private fun requestSubscribers() {
        isSearchMode = false
        subscribersViewModel.requestSubscribers(
            userId = subscribersViewModel.getUserUid(),
            limit = REQUEST_LIMIT,
            offset = subscribersAdapter?.itemCount ?: 0,
        )
    }


    /**
     * Show confirm dialog and delete user from subscribers list
     * */
    private fun showConfirmDialogDeleteSubscriber(subscription: SubscriptionAdapterModel) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.delete_subscriber)
            .setDescription(R.string.remove_subscriber_dialog_description)
            .setTopBtnText(R.string.general_delete)
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnType(ButtonType.OUTLINE)
            .setTopClickListener { subscribersViewModel.deleteFromSubscribers(subscription.user.userId) }
            .setBottomBtnText(R.string.meera_remove_subscriber_dialog_delete_remove)
            .setBottomClickListener {
                subscribersViewModel.deleteAndBlock(
                    subscribersViewModel.getUserUid(),
                    subscription.user.userId
                )
            }
            .show(childFragmentManager)
    }

    private fun showConfirmDialogDeleteUnsubscribe(subscription: SubscriptionAdapterModel) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.user_info_unsub_dialog_header)
            .setDescription(R.string.meera_unsubscribe_dialog_description)
            .setTopBtnText(R.string.unsubscribe)
            .setTopBtnType(ButtonType.FILLED)
            .setTopClickListener {
                isNeedUpdateUserList = false
                subscribersViewModel.deleteFromSubscription(subscription.user.userId)
            }
            .setBottomBtnText(R.string.cancel)
            .show(childFragmentManager)
    }

    private fun subscribeToUser(subscription: SubscriptionAdapterModel) {
        lifecycleScope.launch {
            isNeedUpdateUserList = false
            subscribersViewModel.addSubscription(subscription.user.userId)
        }
    }

    //show placeholder
    private fun showEmptySubscriptions() {
        binding.apply {
            val imgResId = if (isSearchMode) R.drawable.ic_search_people_empty else R.drawable.ic_i_dont_know
            ivEmptyList.setImageResource(imgResId)
            val hintId = if (isSearchMode) R.string.meera_settings_empty_state else R.string.subscibers_list_is_empty
            tvButtonEmptyList.setText(hintId)
            meeraPlaceholderEmptyList.setMargins(bottom = if (isSearchMode) DEFAULT_KEYBOARD_HEIGHT_PX else 0)
        }
        binding?.meeraPlaceholderEmptyList?.visible()
        binding?.rvSubscription?.gone()
    }

    //hide placeholder
    private fun hideEmptySubscriptions() {
        binding?.meeraPlaceholderEmptyList?.gone()
        binding?.rvSubscription?.visible()
    }

    /**
     * Swipe to refresh listener
     * */
    override fun onRefresh() {
        isNeedUpdateUserList = true
        if (!isSearchMode) {
            subscribersAdapter?.submitList(mutableListOf())
            requestSubscribers()
        }
    }

    /**
     * init rx textChanged listener
     * */
    @SuppressLint("CheckResult")
    private fun initRx() {
        binding?.vSearch?.doAfterSearchTextChanged { userName ->
            Observable.just(userName)
                .debounce(INPUT_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { name ->
                    hideEmptySubscriptions()
                    if (name.isEmpty()) {
                        requestFreshSubscriptions()
                    } else {
                        requestSearch(name)
                    }
                }
        }
    }

    private fun requestFreshSubscriptions() {
        isSearchMode = false
        subscribersViewModel.requestSubscribers(
            userId = subscribersViewModel.getUserUid(),
            limit = REQUEST_LIMIT,
            offset = 0,
        )
    }

    /**
     * Search subscriptions
     * */
    private fun requestSearch(text: String, offset: Int = 0) {
        isSearchMode = true
        subscribersViewModel.subscribersSearch(
            offset = offset,
            userId = subscribersViewModel.getUserUid(),
            limit = REQUEST_LIMIT,
            text = text,
        )
    }
}

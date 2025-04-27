package com.numplates.nomera3.presentation.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.textColor
import com.meera.core.extensions.visible
import com.meera.core.network.utils.LocaleManager
import com.noomeera.nmrmediatools.extensions.hideKeyboard
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.FriendListHiWay
import com.numplates.nomera3.databinding.MeeraFriendsHostFragmentBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.INPUT_TIMEOUT
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsHostViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val PAGER_OFFSCREEN_LIMIT: Int = 4
private const val DEFAULT_PAGER_POSITION = 0
private const val DELAY_SCROLL_POSITION = 50L
private const val SECOND_PAGER_POSITION = 1
private const val OWN_FRIENDS_TABS_COUNT = 2

class MeeraFriendsHostFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_friends_host_fragment, behaviourConfigState = ScreenBehaviourState.Full
), MeeraMyFriendListFragment.IOnMyFriendsInteractor, MeeraIncomingFriendListFragment.IOnIncomingFriendsInteractor,
    MeeraUserSubscriptionsFriendsInfoFragment.IOnSubscribersFriendsInteractor {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    enum class SelectedPage(val position: Int) {
        FRIENDS(0),
        INCOMING_REQUESTS(1),
        OUTGOING_REQUESTS(2),


        /**
         * Тип, когда юзер открыл экран у другого юзера "Друзья"
         */
        TAB_USER_FRIENDS(3),

        /**
         * Тип, когда юзер открыл экран у другого юзера "Подписчики"
         */
        TAB_USER_FOLLOWERS(4),

        /**
         * Тип, когда юзер открыл экран у другого юзера "Подписки"
         */
        TAB_USER_FOLLOWING(5),

        /**
         * Тип, когда юзер открыл экран у другого юзера "Общие"
         */
        TAB_USER_MUTUAL(6)
    }

    private val binding by viewBinding(MeeraFriendsHostFragmentBinding::bind)

    private lateinit var pagerAdapter: MeeraFriendPagerAdapter
    private var tvInComingCount: TextView? = null
    private var tvOutgoingCount: TextView? = null
    private var tvFriendsCount: TextView? = null
    private var tvSubscribersCount: TextView? = null

    private val viewModel by viewModels<FriendsHostViewModel> {
        App.component.getViewModelFactory()
    }
    private var userId: Long? = null
    private var incomingCount: Int = 0
    private var userName: String? = null
    private var selectedTabPosition: Int? = null
    private var isMyFriendsTabSelected = false
    private var selectedPage = SelectedPage.FRIENDS
    private var showType: SelectedPage? = null
    private lateinit var fragmentFriends: MeeraMyFriendListFragment
    private lateinit var fragmentIncoming: MeeraIncomingFriendListFragment
    private lateinit var fragmentOutcoming: MeeraOutgoingFriendListFragment
    private var userSubscribersFragment: MeeraUserSubscriptionsFriendsInfoFragment? = null
    private var userFriendsFragment: MeeraUserSubscriptionsFriendsInfoFragment? = null
    private var userSubscriptionFragment: MeeraUserSubscriptionsFriendsInfoFragment? = null
    private var userMutualSubscriptionFragment: MeeraUserMutualSubscriptionFragment? = null

    @Inject
    lateinit var localeManager: LocaleManager

    private var referralToolTipJob: Job? = null
    private val createReferralTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_referral_friends)
    }

    private val disposables = CompositeDisposable()

    private val openedType: FriendsHostOpenedType by lazy {
        val arg = arguments?.get(IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM) as? FriendsHostOpenedType
        arg ?: FriendsHostOpenedType.OTHER
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        arguments?.let {
            userId = it.getLong(IArgContainer.ARG_USER_ID, viewModel.getUserUid())
            incomingCount = it.getInt(IArgContainer.ARG_USER_INCOMING_COUNT, 0)
            userName = it.getString(IArgContainer.ARG_USER_NAME)
            showType = it.getSerializable(IArgContainer.ARG_TYPE_FOLLOWING) as? SelectedPage
            showType?.let { selectedPage ->
                this.selectedPage = selectedPage
            }
            viewModel.getUserInfo(userId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initViewPager()
        initClickListeners()
        initObservers()
    }

    private fun initObservers() {
        viewModel.friendsCountLiveData.observe(viewLifecycleOwner) {
            onAllFriendsCount(it)
        }
    }

    private fun initClickListeners() {
        binding?.ivOpenFindFriends?.setThrottledClickListener {
            viewModel.logPeopleSelected()
            findNavController().safeNavigate(
                R.id.action_meeraFriendsHostFragment_to_meeraPeoplesFragment
            )
        }
    }


    override fun onStart() {
        super.onStart()
        initRx()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
        referralToolTipJob?.cancel()
        createReferralTooltip?.dismiss()
    }

    private fun initToolbar() {
        if (viewModel.isMe(userId)) {
            binding?.vNavView?.title = getString(R.string.friends_friends)
        } else {
            binding?.vNavView?.title = userName
        }
        binding?.vNavView?.backButtonClickListener = {
            findNavController().popBackStack()
        }
        binding?.ivOpenFindFriends?.isVisible = viewModel.isMe(userId)

        binding.vSearch.setButtonText(getString(R.string.cancel))
    }

    private fun initViewPager() {
        initPagerFragments()
        initViewPagerAdapterState()
        binding?.vpFriendsContainer?.adapter = pagerAdapter
        TabLayoutMediator(binding.tabs, binding.vpFriendsContainer) { tab, position ->
            tab.text = pagerAdapter.getPageTitle(position)
        }.attach()
        binding?.tabs?.isTabIndicatorFullWidth = false
        initCustomTabs(pagerAdapter)
        initPagerOffsetLimit()

        initPagerListener()
    }

    private fun initPagerListener() {
        binding.vpFriendsContainer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectedTabPosition?.let { tabPosition ->
                    (pagerAdapter.getItem(tabPosition) as? FriendsSubscribersActionCallback)
                        ?.dismissSuccessSnackBar()
                    sendMutualFriendsAmplitude()
                }
                selectedTabPosition = position
            }
        })
    }

    private fun initScrollPagerPositionByState(isFirstHidden: Boolean) {
        doDelayed(DELAY_SCROLL_POSITION) {
            var position = when (showType?.name) {
                SelectedPage.INCOMING_REQUESTS.name -> SECOND_PAGER_POSITION
                SelectedPage.TAB_USER_FRIENDS.name -> MODE_SHOW_USER_FRIENDS
                SelectedPage.TAB_USER_FOLLOWERS.name -> MODE_SHOW_USER_SUBSCRIBERS
                SelectedPage.TAB_USER_FOLLOWING.name -> MODE_SHOW_USER_SUBSCRIPTIONS
                else -> DEFAULT_PAGER_POSITION
            }
            if (viewModel.isMe(userId).not() && isFirstHidden) position--

            binding.vpFriendsContainer.setCurrentItem(position, false)
            selectedTabPosition = binding.vpFriendsContainer?.currentItem ?: 0
        }
    }

    private fun initPagerFragments() {
        if (viewModel.isMe(userId)) {
            userId?.let {
                fragmentFriends =
                    MeeraMyFriendListFragment.newInstance(it, openedType)
                fragmentIncoming =
                    MeeraIncomingFriendListFragment.newInstance(FriendListHiWay.INCOMING, it, openedType)
                fragmentOutcoming =
                    MeeraOutgoingFriendListFragment.newInstance(FriendListHiWay.OUTCOMING, it, openedType)

                fragmentFriends.friendInteractor = this
                fragmentIncoming.friendInteractor = this
            }
        } else {
            userFriendsFragment = MeeraUserSubscriptionsFriendsInfoFragment.create(
                userId = userId ?: 0,
                actionMode = MODE_SHOW_USER_FRIENDS
            )
            userSubscribersFragment = MeeraUserSubscriptionsFriendsInfoFragment.create(
                userId = userId ?: 0,
                actionMode = MODE_SHOW_USER_SUBSCRIBERS
            )
            userSubscriptionFragment = MeeraUserSubscriptionsFriendsInfoFragment.create(
                userId = userId ?: 0,
                actionMode = MODE_SHOW_USER_SUBSCRIPTIONS
            )
            userSubscriptionFragment?.friendInteractor = this

            userMutualSubscriptionFragment = MeeraUserMutualSubscriptionFragment.create(
                userId = userId ?: 0
            )
        }
    }

    private fun initPagerOffsetLimit() {
        if (!viewModel.isMe(userId)) {
            binding.vpFriendsContainer.offscreenPageLimit = PAGER_OFFSCREEN_LIMIT
        }
    }

    /**
     * Устанавливаем состояние ViewPager в зависимости от типа юзера
     */
    private fun initViewPagerAdapterState() {
        val currentUserId = userId ?: 0
        if (viewModel.isMe(currentUserId)) {
            isMyFriendsTabSelected = true
            pagerAdapter = MeeraFriendPagerAdapter(
                this,
                mutableListOf(fragmentFriends, fragmentIncoming, fragmentOutcoming),
                mutableListOf(
                    getString(R.string.friends_my),
                    getString(R.string.friends_incoming),
                    getString(R.string.friends_new_requests)
                )
            )
        } else {
            isMyFriendsTabSelected = false
            pagerAdapter = MeeraFriendPagerAdapter(
                this, mutableListOf(
                    userMutualSubscriptionFragment ?: return,
                    userFriendsFragment ?: return,
                    userSubscribersFragment ?: return,
                    userSubscriptionFragment ?: return
                ), mutableListOf(
                    resources.getString(R.string.mutual),
                    resources.getString(R.string.friends),
                    resources.getString(R.string.followers),
                    resources.getString(R.string.following)
                )
            )
        }
    }

    @SuppressLint("CutPasteId")
    private fun initCustomTabs(pagerAdapter: MeeraFriendPagerAdapter) {
        binding?.tabs?.let { tabs ->
            for (i in 0 until tabs.tabCount) {
                tabs.tabMode = getTabMode(tabs.tabCount)
                val requestView = LayoutInflater.from(context)
                    .inflate(R.layout.meera_tab_item_community_members_container, null, false)
                val friendsView =
                    LayoutInflater.from(context).inflate(R.layout.meera_tab_item_friends_members_container, null, false)

                val titleRequest = requestView.findViewById<TextView>(R.id.tab_title)
                titleRequest.text = pagerAdapter.getPageTitle(i)
                val titleFriends = friendsView.findViewById<TextView>(R.id.tab_title)
                titleFriends.text = pagerAdapter.getPageTitle(i)
                when (i) {
                    SelectedPage.FRIENDS.position -> {
                        tvFriendsCount = friendsView.findViewById(R.id.tab_count)
                        tabs.getTabAt(i)?.customView = friendsView
                    }

                    SelectedPage.INCOMING_REQUESTS.position -> {
                        tvInComingCount = requestView.findViewById(R.id.tab_count)
                        tabs.getTabAt(i)?.customView = requestView
                    }

                    SelectedPage.OUTGOING_REQUESTS.position -> {
                        tvOutgoingCount = requestView.findViewById(R.id.tab_count)
                        tabs.getTabAt(i)?.customView = requestView
                    }

                    SelectedPage.TAB_USER_FRIENDS.position -> {
                        tvSubscribersCount = requestView.findViewById(R.id.tab_count)
                        tabs.getTabAt(i)?.customView = requestView
                    }
                }

            }
            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab?.position == SelectedPage.FRIENDS.position) {
                        if (isMyFriendsTabSelected) {
                            selectedPage = SelectedPage.FRIENDS
                        } else {
                            selectedPage = SelectedPage.TAB_USER_MUTUAL
                        }
                    }
                    if (tab?.position == SelectedPage.INCOMING_REQUESTS.position) {
                        tabSelected(tvInComingCount)
                        if (isMyFriendsTabSelected) {
                            selectedPage = SelectedPage.INCOMING_REQUESTS
                        } else {
                            selectedPage = SelectedPage.TAB_USER_FRIENDS
                        }
                    }
                    if (tab?.position == SelectedPage.OUTGOING_REQUESTS.position) {
                        tabSelected(tvOutgoingCount)
                        if (isMyFriendsTabSelected) {
                            selectedPage = SelectedPage.OUTGOING_REQUESTS
                        } else {
                            selectedPage = SelectedPage.TAB_USER_FOLLOWERS
                        }
                    }
                    if (tab?.position == SelectedPage.TAB_USER_FRIENDS.position) {
                        tabSelected(tvSubscribersCount)
                        selectedPage = SelectedPage.TAB_USER_FOLLOWING
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    if (tab?.position == SelectedPage.FRIENDS.position) {
                        tabUnSelected(tvInComingCount)
                        binding?.vSearch?.clear()
                        binding?.vSearch?.clearFocus()
                    }
                    if (tab?.position == SelectedPage.INCOMING_REQUESTS.position) {
                        tabUnSelected(tvInComingCount)
                        binding?.vSearch?.clear()
                        binding?.vSearch?.clearFocus()
                    }
                    if (tab?.position == SelectedPage.OUTGOING_REQUESTS.position) {
                        tabUnSelected(tvOutgoingCount)
                        binding?.vSearch?.clear()
                        binding?.vSearch?.clearFocus()
                    }
                    if (tab?.position == SelectedPage.TAB_USER_FRIENDS.position) {
                        tabUnSelected(tvSubscribersCount)
                        binding?.vSearch?.clear()
                        binding?.vSearch?.clearFocus()
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            })
        }
    }

    private fun tabSelected(textView: TextView?) {
        textView?.setBackgroundResource(
            R.drawable.meera_circle_tab_bg
        )
        textView?.textColor(R.color.uiKitColorForegroundPrimary)
    }

    private fun tabUnSelected(textView: TextView?) {
        textView?.setBackgroundResource(
            R.drawable.meera_circle_unselected_count_tab_bg
        )
        textView?.textColor(R.color.uiKitColorForegroundSecondary)
    }

    @SuppressLint("CheckResult")
    private fun initRx() {
        binding?.vSearch?.doAfterSearchTextChanged { userName ->
            Observable.just(userName)
                .debounce(INPUT_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { name ->
                    when (selectedPage) {
                        SelectedPage.FRIENDS -> {
                            fragmentFriends.search(name)
                        }

                        SelectedPage.OUTGOING_REQUESTS -> {
                            fragmentOutcoming.search(name)
                        }

                        SelectedPage.INCOMING_REQUESTS -> {
                            fragmentIncoming.search(name)
                        }

                        SelectedPage.TAB_USER_MUTUAL,
                        SelectedPage.TAB_USER_FRIENDS,
                        SelectedPage.TAB_USER_FOLLOWERS,
                        SelectedPage.TAB_USER_FOLLOWING -> {
                            val currentPagerFragment = getCurrentPagerFragment()
                            if (currentPagerFragment is FriendsSubscribersActionCallback) {
                                currentPagerFragment.search(name)
                            }
                        }

                        else -> Unit
                    }
                }
        }
        binding.vSearch.setCloseButtonClickedListener {
            pagerAdapter?.onCloseSearch()
            hideKeyboard()
        }
    }

    private fun getCurrentPagerFragment(): Fragment {
        return pagerAdapter.getItem(
            binding?.vpFriendsContainer?.currentItem ?: 0
        )
    }

    private fun getTabMode(tabCount: Int): Int {
        return when {
            tabCount == OWN_FRIENDS_TABS_COUNT -> TabLayout.MODE_FIXED
            else -> TabLayout.MODE_SCROLLABLE
        }
    }

    private fun sendMutualFriendsAmplitude() {
        val currentFragment = getCurrentPagerFragment()
        if (currentFragment is FriendsSubscribersActionCallback) {
            currentFragment.logMutualFriendsAmplitude()
        }
    }

    override fun onInComing(count: Int) {
        tvInComingCount?.let {
            if (count > 0) {
                it.text = String.format("+%s", count.toString())
                it.visible()
            } else it.gone()
        }
    }

    override fun onSubscribersCount(count: Int) {
        tvSubscribersCount?.let {
            if (count > 0) {
                it.text = String.format("%s", count.toString())
                it.visible()
            } else it.gone()
        }
    }

    override fun updateFriendsCount() {
        viewModel.getUserInfo(userId)
    }

    private fun onAllFriendsCount(count: Int) {
        tvFriendsCount?.let {
            if (count > 0) {
                it.text = String.format("%s", count.toString())
                it.visible()
            } else {
                it.gone()
            }

        }
        if (viewModel.isMe(userId).not()) {
            pagerAdapter.hideMutualFriends(count == 0)
            initCustomTabs(pagerAdapter)
        }
        initScrollPagerPositionByState(count == 0)
    }

    override fun onNewFriend() {
        fragmentFriends.onRefresh()
    }


    class MeeraFriendPagerAdapter(
        fragment: Fragment,
        private val fragments: List<MeeraBaseFragment>,
        private val titles: List<String>
    ) : FragmentStateAdapter(fragment.childFragmentManager, fragment.lifecycle) {

        private var hideFirst = false

        override fun getItemCount(): Int =
            if (hideFirst) fragments.size - 1 else fragments.size

        override fun createFragment(position: Int): Fragment =
            if (hideFirst) fragments.drop(1)[position]
            else fragments[position]

        fun getPageTitle(position: Int): String =
            if (hideFirst) titles.drop(1)[position]
            else titles[position]

        fun hideMutualFriends(hide: Boolean) {
            hideFirst = hide
            notifyDataSetChanged()
        }

        override fun getItemId(position: Int): Long {
            return if (hideFirst) fragments.drop(1)[position].hashCode().toLong()
            else fragments[position].hashCode().toLong()
        }

        override fun containsItem(itemId: Long): Boolean {
            return fragments.any { it.hashCode().toLong() == itemId }
        }

        fun getItem(position: Int): MeeraBaseFragment {
            return fragments[position]
        }

        fun onCloseSearch() {
            (fragments as? MeeraFriendsHostCallbacks)?.onCloseSearch()
        }
    }

    companion object {
        const val KEY_IS_ME = "isMe"
    }
}

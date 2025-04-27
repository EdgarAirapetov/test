package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.base.BaseFragment
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.core.network.utils.LocaleManager
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.FriendListHiWay
import com.numplates.nomera3.databinding.FragmentFriendsHostBinding
import com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesCommunitiesContainerFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FRIEND_LIST_MODE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_GOTO_INCOMING
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_INCOMING_COUNT
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_NAME
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showGroupChatTooltip
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsHostViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject


private const val REFERRAL_TOOLTIP_OFFSET_Y = 18
private const val REFERRAL_TOOLTIP_OFFSET_X = 2
private const val DEFAULT_PAGER_OFFSCREEN_LIMIT = 4
private const val DEFAULT_PAGER_POSITION = 0
private const val DELAY_SCROLL_POSITION = 50L
private const val DEFAULT_TOOL_BAR_MARGIN = 26
private const val SECOND_PAGER_POSITION = 1
private const val INPUT_DEBOUNCE_DELAY = 200L
private const val OWN_FRIENDS_TABS_COUNT = 2

class FriendsHostFragmentNew : BaseFragmentNew<FragmentFriendsHostBinding>(),
    MyFriendListFragmentNew.IOnMyFriendsInteractor {

    enum class SelectedPage {
        FRIENDS,
        OUTGOING_REQUESTS,

        /**
         * Тип, когда юзер открыл экран у другого юзера "Друзья"
         */
        TAB_USER_FRIENDS,

        /**
         * Тип, когда юзер открыл экран у другого юзера "Подписчики"
         */
        TAB_USER_FOLLOWERS,

        /**
         * Тип, когда юзер открыл экран у другого юзера "Подписки"
         */
        TAB_USER_FOLLOWING,

        /**
         * Тип, когда юзер открыл экран у другого юзера "Общие"
         */
        TAB_USER_MUTUAL
    }

    private lateinit var pagerAdapter: FriendPagerAdapter
    private var tvInComingCount: TextView? = null

    private val viewModel by viewModels<FriendsHostViewModel> {
        App.component.getViewModelFactory()
    }

    private var userId: Long? = null
    private var incomingCount: Int = 0
    private var gotoIncoming = false
    private var userName: String? = null
    private var selectedTabPosition: Int? = null

    private var selectedPage = SelectedPage.FRIENDS

    private lateinit var fragmentFriends: MyFriendListFragmentNew
    private lateinit var fragmentIncoming: MyFriendListFragmentNew
    private lateinit var fragmentBlockUsers: FriendsListFragmentNew
    private lateinit var fragmentOutgoingFriends: OutgoingFriendshipRequestListFragment
    private var userSubscribersFragment: UserSubscriptionsFriendsInfoFragment? = null
    private var userFriendsFragment: UserSubscriptionsFriendsInfoFragment? = null
    private var userSubscriptionFragment: UserSubscriptionsFriendsInfoFragment? = null
    private var userMutualSubscriptionFragment: UserMutualSubscriptionFragment? = null

    @Inject
    lateinit var localeManager: LocaleManager

    private var referralToolTipJob: Job? = null
    private val createReferralTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_referral_friends)
    }

    private val disposables = CompositeDisposable()

    private val openedType: FriendsHostOpenedType by lazy {
        val arg = arguments?.get(ARG_FRIENDS_HOST_OPENED_FROM) as? FriendsHostOpenedType
        arg ?: FriendsHostOpenedType.OTHER
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        arguments?.let {
            userId = it.getLong(ARG_USER_ID, viewModel.getUserUid())
            incomingCount = it.getInt(ARG_USER_INCOMING_COUNT, 0)
            gotoIncoming = it.getBoolean(ARG_IS_GOTO_INCOMING)
            userName = it.getString(ARG_USER_NAME)
            val showType = it[IArgContainer.ARG_TYPE_FOLLOWING] as? SelectedPage
            showType?.let { selectedPage ->
                this.selectedPage = selectedPage
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initViewPager()
        initSearchMenu()
        initClickListeners()
        Timber.e("onCreated HOST")
        binding?.statusBarFriendsHost?.layoutParams?.height = context.getStatusBarHeight()
        binding?.statusBarFriendsHost?.requestLayout()
    }

    private fun initClickListeners() {
        binding?.ivOpenFindFriends?.setOnClickListener {
            viewModel.logPeopleSelected()
            add(PeoplesCommunitiesContainerFragment(), Act.LIGHT_STATUSBAR)
        }

        binding?.friendsMenuItem?.vShadow?.setOnClickListener {
            if (viewModel.isMe(userId)) closeMenu()
        }

        binding?.toolbarTitle?.setOnClickListener {
            if (viewModel.isMe(userId)) {
                when (binding?.friendsMenuItem?.clMenuFriends?.isVisible) {
                    true -> closeMenu()
                    else -> openMenu()
                }
            }
        }

        binding?.friendsMenuItem?.llFriendsList?.setOnClickListener {
            selectedPage = SelectedPage.FRIENDS
            closeMenu()
            binding?.tvTextToolbar?.setText(R.string.profile_friend)
            binding?.ivSearch?.visible()
            binding?.tabs?.visible()
            binding?.flFriendsContainer?.gone()
            binding?.vpFriendsContainer?.visible()
        }

        binding?.friendsMenuItem?.llBlackList?.setOnClickListener {
            closeMenu()
            initBlockUsersFragment()
            binding?.tvTextToolbar?.setText(R.string.friends_black_list)
            binding?.ivSearch?.gone()
            binding?.tabs?.gone()
            binding?.flFriendsContainer?.visible()
            binding?.vpFriendsContainer?.gone()
        }

        binding?.friendsMenuItem?.llOutcomingRequests?.setOnClickListener {
            selectedPage = SelectedPage.OUTGOING_REQUESTS
            closeMenu()
            initOutgoingFriendsFragment()
            binding?.tvTextToolbar?.setText(R.string.friends_new_requests)
            //iv_search.gone()
            binding?.tabs?.gone()
            binding?.flFriendsContainer?.visible()
            binding?.vpFriendsContainer?.gone()
        }
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
        createReferralTooltip?.dismiss()
    }

    private fun showToolTip() {
        if (viewModel.isNeedToShowTooltip() && viewModel.isMe(userId)) {
            referralToolTipJob = lifecycleScope.launch {
                delay(TooltipDuration.COMMON_START_DELAY)
                binding?.ivOpenFindFriends?.let { image ->
                    createReferralTooltip?.showGroupChatTooltip(
                        fragment = this@FriendsHostFragmentNew,
                        view = image,
                        offsetY = -(REFERRAL_TOOLTIP_OFFSET_Y.dp),
                        offsetX = REFERRAL_TOOLTIP_OFFSET_X.dp
                    )
                    viewModel.toolTipShowed()
                    delay(TooltipDuration.CREATE_GROUP_CHAT)
                    createReferralTooltip?.dismiss()
                }
            }
        }
    }

    private fun initToolbar() {
        if (viewModel.isMe(userId)) {
            binding?.tvTextToolbar?.setText(R.string.friends_friends)
            // TODO: 26.07.2022 BR-16227 Пока сделано через отступ. Через релиз планируется рефакторинг верстки
            binding?.toolbarTitle?.setMargins(
                start = DEFAULT_TOOL_BAR_MARGIN.dp,
            )
        } else {
            binding?.tvTextToolbar?.text = userName
            binding?.ivFriendsHostArrowShow?.gone()
        }
        binding?.ivBackArrow?.setOnClickListener {
            act.onBackPressed()
        }
        binding?.ivOpenFindFriends?.isVisible = viewModel.isMe(userId)
    }

    private fun closeMenu() {
        binding?.friendsMenuItem?.clMenuFriends?.gone()
        binding?.vSpace?.gone()
        binding?.tabs?.visible()
        act.setLightStatusBar()
    }

    private fun openMenu() {
        binding?.vSpace?.layoutParams?.height = binding?.tabs?.height
        binding?.tabs?.gone()
        binding?.vSpace?.visible()
        binding?.friendsMenuItem?.clMenuFriends?.visibleAppearAnimate()
        act.setDialogColorStatusBar()
    }

    private fun initViewPager() {
        Timber.i(" FRIENDS_HOST userId: $userId")
        initPagerFragments()
        initViewPagerAdapterState()
        binding?.vpFriendsContainer?.adapter = pagerAdapter
        binding?.tabs?.setupWithViewPager(binding?.vpFriendsContainer)

        initCustomTabs(pagerAdapter)
        initPagerOffsetLimit()

        // Scroll to incoming screen if transit from notifications
        initScrollPagerPositionByState()
        initPagerListener()
    }

    private fun initPagerListener() {
        binding?.vpFriendsContainer?.addOnPageChangeListener(
            object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    selectedTabPosition?.let { tabPosition ->
                        (pagerAdapter.getItem(tabPosition) as? FriendsSubscribersActionCallback)
                            ?.dismissSuccessSnackBar()
                        sendMutualFriendsAmplitude()
                    }
                    selectedTabPosition = position
                }
            }
        )
    }

    private fun initScrollPagerPositionByState() {
        doDelayed(DELAY_SCROLL_POSITION) {
            when {
                gotoIncoming -> {
                    binding?.vpFriendsContainer?.setCurrentItem(
                        SECOND_PAGER_POSITION,
                        false
                    )
                }

                selectedPage.name == SelectedPage.TAB_USER_FRIENDS.name -> {
                    binding?.vpFriendsContainer?.setCurrentItem(
                        MODE_SHOW_USER_FRIENDS,
                        false
                    )
                }

                selectedPage.name == SelectedPage.TAB_USER_FOLLOWERS.name -> {
                    binding?.vpFriendsContainer?.setCurrentItem(
                        MODE_SHOW_USER_SUBSCRIBERS,
                        false
                    )
                }

                selectedPage.name == SelectedPage.TAB_USER_FOLLOWING.name -> {
                    binding?.vpFriendsContainer?.setCurrentItem(
                        MODE_SHOW_USER_SUBSCRIPTIONS,
                        false
                    )
                }

                else -> binding?.vpFriendsContainer?.setCurrentItem(
                    DEFAULT_PAGER_POSITION,
                    false
                )
            }
            selectedTabPosition = binding?.vpFriendsContainer?.currentItem ?: 0
        }
    }

    private fun initPagerFragments() {
        if (viewModel.isMe(userId)) {
            userId?.let {
                fragmentFriends = MyFriendListFragmentNew.newInstance(FriendListHiWay.FRIENDS, it, openedType)
                fragmentIncoming = MyFriendListFragmentNew.newInstance(FriendListHiWay.INCOMING, it, openedType)
                fragmentIncoming.friendInteractor = this
            }
        } else {
            userFriendsFragment = UserSubscriptionsFriendsInfoFragment.create(
                userId = userId ?: 0,
                actionMode = MODE_SHOW_USER_FRIENDS
            )
            userSubscribersFragment = UserSubscriptionsFriendsInfoFragment.create(
                userId = userId ?: 0,
                actionMode = MODE_SHOW_USER_SUBSCRIBERS
            )
            userSubscriptionFragment = UserSubscriptionsFriendsInfoFragment.create(
                userId = userId ?: 0,
                actionMode = MODE_SHOW_USER_SUBSCRIPTIONS
            )
            userMutualSubscriptionFragment = UserMutualSubscriptionFragment.create(
                userId = userId ?: 0
            )
        }
    }

    private fun initPagerOffsetLimit() {
        if (!viewModel.isMe(userId)) {
            binding?.vpFriendsContainer?.offscreenPageLimit = DEFAULT_PAGER_OFFSCREEN_LIMIT
        }
    }

    /**
     * Устанавливаем состояние ViewPager в зависимости от типа юзера
     */
    private fun initViewPagerAdapterState() {
        val currentUserId = userId ?: 0
        if (viewModel.isMe(currentUserId)) {
            pagerAdapter = FriendPagerAdapter(
                childFragmentManager,
                mutableListOf(fragmentFriends, fragmentIncoming),
                mutableListOf(
                    "         ${resources.getString(R.string.friends_my)}         ",
                    resources.getString(R.string.friends_incoming)
                )
            )
        } else {
            pagerAdapter = FriendPagerAdapter(
                childFragmentManager,
                mutableListOf(
                    userMutualSubscriptionFragment ?: return,
                    userFriendsFragment ?: return,
                    userSubscribersFragment ?: return,
                    userSubscriptionFragment ?: return
                ),
                mutableListOf(
                    resources.getString(R.string.mutual),
                    resources.getString(R.string.friends),
                    resources.getString(R.string.followers),
                    resources.getString(R.string.following)
                )
            )
        }
    }

    private fun initBlockUsersFragment() {
        val args = Bundle()
        args.putInt(ARG_FRIEND_LIST_MODE, FriendListHiWay.BLACKLIST)
        fragmentBlockUsers = FriendsListFragmentNew()
        fragmentBlockUsers.arguments = args
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_friends_container, fragmentBlockUsers)
            .commit()
    }

    private fun initOutgoingFriendsFragment() {
        val args = Bundle()
        args.putInt(ARG_FRIEND_LIST_MODE, FriendListHiWay.OUTCOMING)
        fragmentOutgoingFriends = OutgoingFriendshipRequestListFragment()
        fragmentOutgoingFriends.arguments = args
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_friends_container, fragmentOutgoingFriends)
            .commit()
    }


    private fun initCustomTabs(pagerAdapter: FriendPagerAdapter) {
        binding?.tabs?.let { tabs ->
            for (i in 0 until tabs.tabCount) {
                tabs.tabMode = getTabMode(tabs.tabCount)
                val v = LayoutInflater.from(context).inflate(getTabLayout(), null, false)
                val title = v.findViewById<TextView>(R.id.tabTitle)
                title.text = pagerAdapter.getPageTitle(i)

                if (i == 1) tvInComingCount = v.findViewById(R.id.tabCount)

                tabs.getTabAt(i)?.customView = v
            }
        }
    }

    private fun initRx() {
        binding?.etSearchGroup?.let { etSearchGroup ->
            val d = RxTextView.textChanges(etSearchGroup)
                .map { text -> text.toString().lowercase(Locale.getDefault()).trim() }
                .debounce(INPUT_DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
                .skip(1) // todo пропуск инициализирующего значения, должно быть после debounce в rx2 заменить на skipInitial
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ text ->
                    if (text.isNullOrEmpty() && etSearchGroup.visibility == View.VISIBLE) {
                        binding?.ivSearch?.gone()
                    } else {
                        binding?.ivSearch?.visible()
                    }
                    // todo сделать абстрактные классы вместо MyFriendListFragmentNew
                    // костыль чтоб определить в какой фрагмент передавать поисковый запрос
                    // SelectedPage.FRIENDS - по дефолту экран Друзья
                    // SelectedPage.OUTGOING_REQUESTS - экран исходящие заявки
                    when (selectedPage) {
                        SelectedPage.FRIENDS -> {
                            val fragment = pagerAdapter.getItem(binding?.vpFriendsContainer?.currentItem ?: 0)
                            if (fragment is MyFriendListFragmentNew) {
                                if (text.isNotEmpty()) fragment.search(text)
                                else fragment.search("")
                            }
                        }

                        SelectedPage.OUTGOING_REQUESTS -> {
                            //fragmentOutgoingFriends.turnOnSearchMode()
                            fragmentOutgoingFriends.searchOutgoingFriendshipRequests(text)
                        }

                        SelectedPage.TAB_USER_MUTUAL,
                        SelectedPage.TAB_USER_FRIENDS,
                        SelectedPage.TAB_USER_FOLLOWERS,
                        SelectedPage.TAB_USER_FOLLOWING -> {
                            val currentPagerFragment = getCurrentPagerFragment()
                            if (currentPagerFragment is FriendsSubscribersActionCallback) {
                                currentPagerFragment.search(text)
                            }
                        }
                    }
                }, { error ->
                    Timber.e("ERROR: Observe text changes: $error")
                })
            disposables.add(d)
        }
    }

    private fun initSearchMenu() {
        binding?.ivSearch?.setOnClickListener {
            closeMenu()
            showSearch()
        }
    }

    private fun showSearch() {
        setUpSearchListeners()
        binding?.toolbarTitle?.gone()
        binding?.tabs?.gone()
        binding?.ivSearch?.gone()
        binding?.etSearchGroup?.visible()
        binding?.etSearchGroup?.setText("")
        binding?.etSearchGroup?.showKeyboard()

        when (selectedPage) {
            SelectedPage.FRIENDS -> {
                binding?.vpFriendsContainer?.setPagingEnabled(false)
                fragmentFriends.onStartSearch()
                fragmentIncoming.onStartSearch()
                if (binding?.vpFriendsContainer?.currentItem == 0) {
                    binding?.etSearchGroup?.hint = getString(R.string.enter_friend_name)
                } else if (binding?.vpFriendsContainer?.currentItem == 1) {
                    binding?.etSearchGroup?.hint = getString(R.string.enter_name_txt)
                }
            }

            SelectedPage.TAB_USER_FOLLOWING,
            SelectedPage.TAB_USER_FOLLOWERS,
            SelectedPage.TAB_USER_FRIENDS -> {
                binding?.vpFriendsContainer?.setPagingEnabled(false)
                binding?.etSearchGroup?.hint = getString(R.string.general_search)
                updateUserFriendsSwipeEnabled(false)
                pushSearchStatusCurrentFragment(true)
            }

            SelectedPage.TAB_USER_MUTUAL -> {
                binding?.vpFriendsContainer?.setPagingEnabled(false)
                binding?.etSearchGroup?.hint = getString(R.string.general_search)
                userMutualSubscriptionFragment?.setSwipeRefreshEnabled(false)
                pushSearchStatusCurrentFragment(true)
            }

            else -> {
                binding?.etSearchGroup?.hint = getString(R.string.enter_name_txt)
                fragmentOutgoingFriends.turnOnSearchMode()
            }
        }
    }

    private fun pushSearchStatusCurrentFragment(isSearchOpen: Boolean) {
        val currentFragment = pagerAdapter.getItem(
            binding?.vpFriendsContainer?.currentItem ?: 0
        )
        when (currentFragment) {
            is UserSubscriptionsFriendsInfoFragment -> {
                pushIsSearchOpenSubscriptionFragment(
                    isSearchOpen = isSearchOpen,
                    fragment = currentFragment
                )
            }

            is UserMutualSubscriptionFragment -> {
                pushIsSearchOpenMutualFragment(
                    isSearchOpen = isSearchOpen,
                    fragment = currentFragment
                )
            }
        }
    }

    private fun pushIsSearchOpenMutualFragment(
        isSearchOpen: Boolean,
        fragment: UserMutualSubscriptionFragment
    ) {
        if (isSearchOpen) fragment.searchOpen() else fragment.searchClosed()
    }

    private fun pushIsSearchOpenSubscriptionFragment(
        isSearchOpen: Boolean,
        fragment: UserSubscriptionsFriendsInfoFragment
    ) {
        if (isSearchOpen) fragment.searchOpen() else fragment.searchClosed()
    }

    private fun getCurrentPagerFragment(): Fragment {
        return pagerAdapter.getItem(
            binding?.vpFriendsContainer?.currentItem ?: 0
        )
    }

    private fun updateUserFriendsSwipeEnabled(isEnabled: Boolean) {
        userFriendsFragment?.setSwipeRefreshEnabled(isEnabled)
        userSubscriptionFragment?.setSwipeRefreshEnabled(isEnabled)
        userSubscribersFragment?.setSwipeRefreshEnabled(isEnabled)
    }

    private fun closeSearch() {
        setUpCommonListeners()
        binding?.ivSearch?.visible()
        binding?.toolbarTitle?.visible()
        binding?.tabs?.visible()
        binding?.vpFriendsContainer?.setPagingEnabled(true)
        binding?.etSearchGroup?.gone()
        binding?.etSearchGroup?.hideKeyboard()
        if (viewModel.isMe(userId)) {
            fragmentIncoming.onCloseSearch()
            fragmentFriends.onCloseSearch()
        } else {
            updateUserFriendsSwipeEnabled(true)
            pushSearchStatusCurrentFragment(false)
        }
    }

    private fun setUpSearchListeners() {
        binding?.ivBackArrow?.setImageResource(R.drawable.ic_arrow_back_noomeera)
        binding?.ivSearch?.setImageResource(R.drawable.ic_close_noomeera)

        binding?.ivBackArrow?.setPadding(
            dpToPx(5),
            dpToPx(5),
            dpToPx(5),
            dpToPx(5)
        )

        binding?.ivSearch?.setPadding(
            dpToPx(9),
            dpToPx(9),
            dpToPx(9),
            dpToPx(9)
        )

        binding?.ivBackArrow?.setOnClickListener {
            if (selectedPage == SelectedPage.OUTGOING_REQUESTS && ::fragmentOutgoingFriends.isInitialized) {
                fragmentOutgoingFriends.turnOffSearchUIMode()
                setUpCommonListeners()
                binding?.ivSearch?.visible()
                binding?.toolbarTitle?.visible()
                binding?.etSearchGroup?.gone()
                binding?.etSearchGroup?.hideKeyboard()
            } else {
                closeSearch()
            }
        }

        binding?.ivSearch?.setOnClickListener {
            binding?.etSearchGroup?.setText("")

            if (selectedPage == SelectedPage.OUTGOING_REQUESTS && ::fragmentOutgoingFriends.isInitialized) {
                fragmentOutgoingFriends.resetSearch()
            }
        }

    }

    /**
     * tab_item_friends_host - Используется для анг локали чтобы табы скролились/ Не влазит текст на английском/
     * Task - BR-28365
     * Аналогично и для метода getTabMode()
     * */
    private fun getTabLayout() = if (localeManager.isRusLanguage()) R.layout.tab_item
    else R.layout.tab_item_friends_host

    private fun getTabMode(tabCount: Int): Int {
        return when {
            tabCount == OWN_FRIENDS_TABS_COUNT -> TabLayout.MODE_FIXED
            localeManager.isRusLanguage() -> TabLayout.MODE_FIXED
            else -> TabLayout.MODE_SCROLLABLE
        }
    }


    private fun setUpCommonListeners() {
        binding?.ivBackArrow?.setImageResource(R.drawable.arrowback)
        binding?.ivSearch?.setImageResource(R.drawable.ic_search_black_nomera)


        binding?.ivBackArrow?.setPadding(
            dpToPx(8),
            dpToPx(8),
            dpToPx(8),
            dpToPx(8)
        )

        binding?.ivSearch?.setPadding(
            dpToPx(8),
            dpToPx(8),
            dpToPx(8),
            dpToPx(8)
        )

        binding?.ivBackArrow?.setOnClickListener {
            act.onBackPressed()
        }

        binding?.ivSearch?.setOnClickListener {
            closeMenu()
            showSearch()
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
                it.text = count.toString()
                it.visible()
            } else
                it.gone()
        }
    }

    override fun onNewFriend() {
        fragmentFriends.onRefresh()
    }


    class FriendPagerAdapter(
        val fragmentManager: FragmentManager,
        private val fragments: List<BaseFragment>,
        private val titles: List<String>
    ) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun saveState(): Parcelable? {
            return null // не трогать
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFriendsHostBinding
        get() = FragmentFriendsHostBinding::inflate
}
